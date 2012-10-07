/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  John Lawson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You may obtain a copy of the GNU General Public License at 
 * <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package org.nof1trial.nof1.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.activities.AccountsActivity;
import org.nof1trial.nof1.app.Util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * Service to handle registering accounts. Can register an account for the first
 * time and refresh the saved auth cookie for the account.
 * 
 * Handled actions:
 * org.nof1trial.nof1.REFRESH_CREDENTIALS
 * org.nof1trial.nof1.REGISTER_ACCOUNT
 * 
 * @author John Lawson
 * 
 */
public class AccountService extends IntentService {

	private static final String TAG = "AccountService";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	/** Cookie name for authorisation. */
	private static final String AUTH_COOKIE_NAME = "SACSID";

	private final Context mContext = this;

	public AccountService() {
		this("AccountService");
	}

	public AccountService(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (DEBUG) Log.d(TAG, "Account service started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "Account service stopped");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Note intent could be null
		if (DEBUG) Log.d(TAG, "Handling new intent");

		if (Keys.ACTION_REFRESH.equals(intent.getAction())) {
			// Refresh the saved auth cookie
			if (DEBUG) Log.d(TAG, "Refreshing account cookie");

			refreshAuthCookie();

		} else if (Keys.ACTION_REGISTER.equals(intent.getAction())) {
			String account = intent.getStringExtra(Keys.INTENT_ACCOUNT);
			if (DEBUG) Log.d(TAG, "Registering account: " + account);

			register(account);

		} else {
			Log.w(TAG, "IntentService started with unrecognised action");
		}

	}

	/**
	 * Gets the auth cookie from AccountManager and saves to shared prefs
	 * 
	 * @param accountName
	 *            a String containing a Google account name
	 */
	private void register(final String accountName) {
		// Store the account name in shared preferences
		final SharedPreferences prefs = Util.getSharedPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.ACCOUNT_NAME, accountName);
		editor.putString(Util.AUTH_COOKIE, null);
		editor.commit();

		if (Util.isDebug(mContext)) {
			// Use a fake cookie for the dev mode app engine server
			// The cookie has the form email:isAdmin:userId
			// We set the userId to be the same as the account name
			String authCookie = "dev_appserver_login=" + accountName + ":false:" + accountName;
			prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
		}

		// Obtain an auth token and register
		AccountManager mgr = AccountManager.get(mContext);
		Account[] accts = mgr.getAccountsByType("com.google");
		for (Account acct : accts) {
			if (acct.name.equals(accountName)) {
				if (Util.isDebug(mContext)) {
					// Use a fake cookie for the dev mode app engine server
					// The cookie has the form email:isAdmin:userId
					// We set the userId to be the same as the account name
					String authCookie = "dev_appserver_login=" + accountName + ":false:"
							+ accountName;
					prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
				} else {
					// Get the auth token from the AccountManager and convert
					// it into a cookie for the appengine server
					mgr.getAuthToken(acct, "ah", null, new AccountsActivity(),
							new AccountManagerCallback<Bundle>() {
								@Override
								public void run(AccountManagerFuture<Bundle> future) {
									try {
										Bundle authTokenBundle = future.getResult();
										String authToken = authTokenBundle.get(
												AccountManager.KEY_AUTHTOKEN).toString();

										new CookieSaver().execute(authToken);

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
	 * Refresh the auth cookie stored in shared prefs.
	 * 
	 * Will call invlidateAuthToken in account manager, so the next authToken is
	 * new, then save the new cookie to prefs
	 */
	private void refreshAuthCookie() {
		// Get account name from prefs
		final SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String accountName = prefs.getString(Util.ACCOUNT_NAME, "error");

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.AUTH_COOKIE, null);
		editor.commit();

		if (Util.isDebug(mContext)) {
			// Use a fake cookie for the dev mode app engine server
			// The cookie has the form email:isAdmin:userId
			// We set the userId to be the same as the account name
			String authCookie = "dev_appserver_login=" + accountName + ":false:" + accountName;
			prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
			// Broadcast the change to receiver
			Intent broadcast = new Intent(Keys.ACTION_COMPLETE);
			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
			manager.sendBroadcast(broadcast);
		}

		// Obtain an auth token and register
		final AccountManager mgr = AccountManager.get(mContext);
		Account[] accts = mgr.getAccountsByType("com.google");
		for (final Account acct : accts) {
			if (acct.name.equals(accountName)) {
				if (Util.isDebug(mContext)) {
					// Use a fake cookie for the dev mode app engine server
					// The cookie has the form email:isAdmin:userId
					// We set the userId to be the same as the account name
					String authCookie = "dev_appserver_login=" + accountName + ":false:"
							+ accountName;
					prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
				} else {
					// Get the auth token from the AccountManager and convert
					// it into a cookie for the appengine server

					// This bit is a bit of a hack, as need to ask
					// AccountManager for the old authToken, then invalidate
					// it before requesting a new authToken.
					// Unfortunately this leads to nested callbacks.
					final AccountsActivity act = new AccountsActivity();
					// 'ah' signifies app engine auth, rather than 'android' etc
					mgr.getAuthToken(acct, "ah", null, act, new AccountManagerCallback<Bundle>() {
						@Override
						public void run(AccountManagerFuture<Bundle> future) {
							try {
								Bundle authTokenBundle = future.getResult();
								String authToken = authTokenBundle
										.get(AccountManager.KEY_AUTHTOKEN).toString();

								// Invalidate auth token, so next time Account
								// Manager requests a new one
								mgr.invalidateAuthToken("com.google", authToken);

								// Request new one
								mgr.getAuthToken(acct, "ah", null, act,
										new AccountManagerCallback<Bundle>() {
											@Override
											public void run(AccountManagerFuture<Bundle> future) {
												try {
													Bundle authTokenBundle = future.getResult();
													String authToken = authTokenBundle.get(
															AccountManager.KEY_AUTHTOKEN)
															.toString();

													new CookieSaver().execute(authToken);

												} catch (AuthenticatorException e) {
													Log.w(TAG, "Got AuthenticatorException " + e);
													Log.w(TAG, Log.getStackTraceString(e));
												} catch (IOException e) {
													Log.w(TAG,
															"Got IOException "
																	+ Log.getStackTraceString(e));
													Log.w(TAG, Log.getStackTraceString(e));
												} catch (OperationCanceledException e) {
													Log.w(TAG, "Got OperationCanceledException "
															+ e);
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
	 * Retrieves the authorization cookie associated with the given token. This
	 * method should only be used when running against a production appengine
	 * backend (as opposed to a dev mode server).
	 */
	private String getAuthCookie(String authToken) {
		try {
			// Get SACSID cookie
			DefaultHttpClient client = new DefaultHttpClient();
			String continueURL = Util.PROD_URL;
			URI uri = new URI(Util.PROD_URL + "/_ah/login?continue="
					+ URLEncoder.encode(continueURL, "UTF-8") + "&auth=" + authToken);
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
	 * Because the AccountManagerFuture is given a runnable, that does not get
	 * run in the IntentService HandleThread but
	 * rather the main thread annoyingly. Need to use an async task (or any
	 * other thread) to get the auth cookie, as
	 * this uses network
	 */
	private class CookieSaver extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... authToken) {
			return getAuthCookie(authToken[0]);

		}

		@Override
		protected void onPostExecute(String authCookie) {

			final SharedPreferences prefs = Util.getSharedPreferences(mContext);

			prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();

			if (DEBUG) Log.d(TAG, "Cookie refreshed. Sending broadcast");

			// Broadcast the change to receiver
			Intent broadcast = new Intent(Keys.ACTION_COMPLETE);
			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
			manager.sendBroadcast(broadcast);
		}

	}

}
