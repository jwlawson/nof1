/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.nof1trial.nof1.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.activities.AccountsActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;

/**
 * Utility methods for getting the base URL for client-server communication and
 * retrieving shared preferences.
 */
public class Util {

	/**
	 * Tag for logging.
	 */
	private static final String TAG = "Util";
	private static final boolean DEBUG = true;

	// Shared constants

	/**
	 * Key for account name in shared preferences.
	 */
	public static final String ACCOUNT_NAME = "accountName";

	/**
	 * Key for auth cookie name in shared preferences.
	 */
	public static final String AUTH_COOKIE = "authCookie";

	/**
	 * Cookie name for authorization.
	 */
	private static final String AUTH_COOKIE_NAME = "SACSID";

	/**
	 * The AppEngine app name, used to construct the production service URL
	 * below.
	 */
	private static final String APP_NAME = "nof1trial";

	/**
	 * The URL of the production service.
	 */
	public static final String PROD_URL = "https://" + APP_NAME + ".appspot.com";

	/**
	 * URL suffix for the RequestFactory servlet.
	 */
	public static final String RF_METHOD = "/gwtRequest";

	/**
	 * An intent name for receiving registration/unregistration status.
	 */
	public static final String UPDATE_UI_INTENT = getPackageName() + ".UPDATE_UI";

	// End shared constants

	/**
	 * Key for shared preferences.
	 */
	private static final String SHARED_PREFS = Keys.CONFIG_NAME;

	/**
	 * Cache containing the base URL for a given context.
	 */
	private static final Map<Context, String> URL_MAP = new HashMap<Context, String>();

	/**
	 * Returns the (debug or production) URL associated with the registration
	 * service.
	 */
	public static String getBaseUrl(Context context) {
		String url = URL_MAP.get(context);
		if (url == null) {
			// if a debug_url raw resource exists, use its contents as the url
			if (DEBUG) Log.d(TAG, "Getting debug url");
			url = getDebugUrl(context);
			// otherwise, use the production url
			if (url == null) {
				if (DEBUG) Log.d(TAG, "Using production url");
				url = Util.PROD_URL;
			}
			URL_MAP.put(context, url);
		}
		return url;
	}

	/**
	 * Creates and returns an initialized {@link RequestFactory} of the given
	 * type.
	 */
	public static <T extends RequestFactory> T getRequestFactory(Context context, Class<T> factoryClass) {
		T requestFactory = RequestFactorySource.create(factoryClass);

		SharedPreferences prefs = getSharedPreferences(context);
		String authCookie = prefs.getString(Util.AUTH_COOKIE, null);

		String uriString = Util.getBaseUrl(context) + RF_METHOD;
		URI uri;
		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			Log.w(TAG, "Bad URI: " + uriString, e);
			return null;
		}
		if (DEBUG) Log.d(TAG, "Using uri: " + uri);
		requestFactory.initialize(new SimpleEventBus(), new AndroidRequestTransport(uri, authCookie));
		if (DEBUG) Log.d(TAG, "RequestFactory initialized");

		return requestFactory;
	}

	/**
	 * Helper method to get a SharedPreferences instance.
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
	}

	/**
	 * Refresh the auth cookie stored in shared prefs.Will then call invlidateAuthToken in account manager, so
	 * 
	 * @param context
	 */
	public static void refreshAuthCookie(Context context) {
		// Get account name from prefs
		final SharedPreferences prefs = Util.getSharedPreferences(context);
		String accountName = prefs.getString(Util.ACCOUNT_NAME, "error");

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.AUTH_COOKIE, null);
		editor.commit();

		if (Util.isDebug(context)) {
			// Use a fake cookie for the dev mode app engine server
			// The cookie has the form email:isAdmin:userId
			// We set the userId to be the same as the account name
			String authCookie = "dev_appserver_login=" + accountName + ":false:" + accountName;
			prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
		}

		// Obtain an auth token and register
		final AccountManager mgr = AccountManager.get(context);
		Account[] accts = mgr.getAccountsByType("com.google");
		for (final Account acct : accts) {
			if (acct.name.equals(accountName)) {
				if (Util.isDebug(context)) {
					// Use a fake cookie for the dev mode app engine server
					// The cookie has the form email:isAdmin:userId
					// We set the userId to be the same as the account name
					String authCookie = "dev_appserver_login=" + accountName + ":false:" + accountName;
					prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
				} else {
					// Get the auth token from the AccountManager and convert
					// it into a cookie for the appengine server

					// This bit is a bit of a hack, as need to ask AccountManager for the old authToken, then invalidate
					// it before requesting a new authToken.
					// Unfortunately this leads to nested callbacks.
					final AccountsActivity act = new AccountsActivity();
					// TODO Check that 'ah' is the correct auth type, might be 'android'
					mgr.getAuthToken(acct, "ah", null, act, new AccountManagerCallback<Bundle>() {
						public void run(AccountManagerFuture<Bundle> future) {
							try {
								Bundle authTokenBundle = future.getResult();
								String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();

								// Invalidate auth token, so next time Account Manager requests a new one
								mgr.invalidateAuthToken("com.google", authToken);

								// Request new one
								mgr.getAuthToken(acct, "ah", null, act, new AccountManagerCallback<Bundle>() {
									public void run(AccountManagerFuture<Bundle> future) {
										try {
											Bundle authTokenBundle = future.getResult();
											String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
											String authCookie = getAuthCookie(authToken);
											prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();

										} catch (AuthenticatorException e) {
											Log.w(TAG, "Got AuthenticatorException " + e);
											Log.w(TAG, Log.getStackTraceString(e));
										} catch (IOException e) {
											Log.w(TAG, "Got IOException " + Log.getStackTraceString(e));
											Log.w(TAG, Log.getStackTraceString(e));
										} catch (OperationCanceledException e) {
											Log.w(TAG, "Got OperationCanceledException " + e);
											Log.w(TAG, Log.getStackTraceString(e));
										}
									}
								}, null);

							} catch (AuthenticatorException e) {
								Log.w(TAG, "Got AuthenticatorException " + e);
								Log.w(TAG, Log.getStackTraceString(e));
							} catch (IOException e) {
								Log.w(TAG, "Got IOException " + Log.getStackTraceString(e));
								Log.w(TAG, Log.getStackTraceString(e));
							} catch (OperationCanceledException e) {
								Log.w(TAG, "Got OperationCanceledException " + e);
								Log.w(TAG, Log.getStackTraceString(e));
							}
						}
					}, null);
				}
				break;
			}
		}
	}

	/**
	 * Returns true if we are running against a dev mode appengine instance.
	 */
	public static boolean isDebug(Context context) {
		// Although this is a bit roundabout, it has the nice side effect
		// of caching the result.
		return !Util.PROD_URL.equals(getBaseUrl(context));
	}

	/**
	 * Returns a debug url, or null. To set the url, create a file {@code assets/debugging_prefs.properties} with a line
	 * of the form
	 * 'url=http:/<ip address>:<port>'. A numeric IP address may be required in
	 * situations where the device or emulator will not be able to resolve the
	 * hostname for the dev mode server.
	 */
	private static String getDebugUrl(Context context) {
		BufferedReader reader = null;
		String url = null;
		try {
			AssetManager assetManager = context.getAssets();
			InputStream is = assetManager.open("debugging_prefs.properties");
			reader = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String s = reader.readLine();
				if (s == null) {
					break;
				}
				if (s.startsWith("url=")) {
					url = s.substring(4).trim();
					if (DEBUG) Log.d(TAG, "Debug url: " + url);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			// O.K., we will use the production server
			return null;
		} catch (Exception e) {
			Log.w(TAG, "Got exception " + e);
			Log.w(TAG, Log.getStackTraceString(e));
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.w(TAG, "Got exception " + e);
					Log.w(TAG, Log.getStackTraceString(e));
				}
			}
		}

		return url;
	}

	/**
	 * Retrieves the authorization cookie associated with the given token. This
	 * method should only be used when running against a production appengine
	 * backend (as opposed to a dev mode server).
	 */
	private static String getAuthCookie(String authToken) {
		try {
			// Get SACSID cookie
			DefaultHttpClient client = new DefaultHttpClient();
			String continueURL = Util.PROD_URL;
			URI uri = new URI(Util.PROD_URL + "/_ah/login?continue=" + URLEncoder.encode(continueURL, "UTF-8") + "&auth=" + authToken);
			HttpGet method = new HttpGet(uri);
			final HttpParams getParams = new BasicHttpParams();
			HttpClientParams.setRedirecting(getParams, false);
			method.setParams(getParams);

			HttpResponse res = client.execute(method);
			Header[] headers = res.getHeaders("Set-Cookie");
			if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
				return null;
			}

			for (Cookie cookie : client.getCookieStore().getCookies()) {
				if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
					return AUTH_COOKIE_NAME + "=" + cookie.getValue();
				}
			}
		} catch (IOException e) {
			Log.w(TAG, "Got IOException " + e);
			Log.w(TAG, Log.getStackTraceString(e));
		} catch (URISyntaxException e) {
			Log.w(TAG, "Got URISyntaxException " + e);
			Log.w(TAG, Log.getStackTraceString(e));
		}

		return null;
	}

	/**
	 * Returns the package name of this class.
	 */
	private static String getPackageName() {
		return Util.class.getPackage().getName();
	}
}
