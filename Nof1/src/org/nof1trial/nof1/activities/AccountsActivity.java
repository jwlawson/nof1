/*
 * Copyright 2010 Google Inc.
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
package org.nof1trial.nof1.activities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.app.Util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Account selections activity - handles device registration and unregistration.
 */
public class AccountsActivity extends SherlockActivity {
	/**
	 * Tag for logging.
	 */
	private static final String TAG = "AccountsActivity";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	/**
	 * Cookie name for authorization.
	 */
	private static final String AUTH_COOKIE_NAME = "SACSID";

	/**
	 * The selected position in the ListView of accounts.
	 */
	private int mAccountSelectedPosition = 0;

	/**
	 * The current context.
	 */
	private Context mContext = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String account = prefs.getString(Util.ACCOUNT_NAME, null);
		if (account == null) {
			// Show the 'connect' screen if we are not connected
			setScreenContent(R.layout.account_connect);
		} else {
			// Show the 'disconnect' screen if we are connected
			setScreenContent(R.layout.account_disconnect);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// Manage UI Screens

	/**
	 * Sets up the 'connect' screen content.
	 */
	private void setConnectScreenContent() {
		List<String> accounts = getGoogleAccounts();
		if (Util.isDebug(mContext)) {
			// Use debug account
			register("android@jwlawson.co.uk");
			if (DEBUG) Log.d(TAG, "Using debug account android@jwlawson.co.uk");
			finish();
			return;
		}
		if (accounts.size() == 0) {
			// Show a dialog and invoke the "Add Account" activity if requested
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(R.string.needs_account);
			builder.setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
				}
			});
			builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					AccountsActivity.this.setResult(RESULT_CANCELED);
					finish();
				}
			});
			builder.setIcon(android.R.drawable.stat_sys_warning);
			builder.setTitle(R.string.attention);
			builder.show();
		} else {
			final ListView listView = (ListView) findViewById(R.id.select_account);
			listView.setAdapter(new ArrayAdapter<String>(mContext, R.layout.account_list_item, accounts));
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setItemChecked(mAccountSelectedPosition, true);

			final Button connectButton = (Button) findViewById(R.id.connect);
			connectButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Register in the background and terminate the activity
					mAccountSelectedPosition = listView.getCheckedItemPosition();
					TextView account = (TextView) listView.getChildAt(mAccountSelectedPosition);
					register((String) account.getText());
					AccountsActivity.this.setResult(RESULT_OK);
					finish();
				}
			});

			final Button exitButton = (Button) findViewById(R.id.exit);
			exitButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					AccountsActivity.this.setResult(RESULT_CANCELED);
					finish();
				}
			});
		}
	}

	/**
	 * Sets up the 'disconnected' screen.
	 */
	private void setDisconnectScreenContent() {
		final SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String accountName = prefs.getString(Util.ACCOUNT_NAME, "error");

		// Format the disconnect message with the currently connected account
		// name
		TextView disconnectText = (TextView) findViewById(R.id.disconnect_text);
		String message = getResources().getString(R.string.disconnect_text);
		String formatted = String.format(message, accountName);
		disconnectText.setText(formatted);

		Button disconnectButton = (Button) findViewById(R.id.disconnect);
		disconnectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Delete the current account from shared preferences
				Editor editor = prefs.edit();
				editor.putString(Util.AUTH_COOKIE, null);
				editor.commit();

				// Unregister in the background and terminate the activity
				finish();
			}
		});

		Button exitButton = (Button) findViewById(R.id.exit);
		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Sets the screen content based on the screen id.
	 */
	private void setScreenContent(int screenId) {
		setContentView(screenId);
		switch (screenId) {
		case R.layout.account_disconnect:
			setDisconnectScreenContent();
			break;
		case R.layout.account_connect:
			setConnectScreenContent();
			break;
		}
	}

	// Register and Unregister

	/**
	 * Gets the auth cookie from AccountManager and saves to shared prefs
	 * 
	 * @param accountName a String containing a Google account name
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
					String authCookie = "dev_appserver_login=" + accountName + ":false:" + accountName;
					prefs.edit().putString(Util.AUTH_COOKIE, authCookie).commit();
				} else {
					// Get the auth token from the AccountManager and convert
					// it into a cookie for the appengine server
					mgr.getAuthToken(acct, "ah", null, this, new AccountManagerCallback<Bundle>() {
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
				}
				break;
			}
		}
	}

	// Utility Methods

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
	 * Returns a list of registered Google account names. If no Google accounts
	 * are registered on the device, a zero-length list is returned.
	 */
	private List<String> getGoogleAccounts() {
		ArrayList<String> result = new ArrayList<String>();
		Account[] accounts = AccountManager.get(mContext).getAccounts();
		for (Account account : accounts) {
			if (account.type.equals("com.google")) {
				result.add(account.name);
			}
		}

		return result;
	}
}
