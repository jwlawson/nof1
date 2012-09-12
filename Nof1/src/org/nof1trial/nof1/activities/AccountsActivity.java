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

import java.util.ArrayList;
import java.util.List;

import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.services.AccountService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Account selections activity - offloads connecting to AccountService
 */
public class AccountsActivity extends SherlockActivity {

	/** Tag for logging. */
	private static final String TAG = "AccountsActivity";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	/** The selected position in the ListView of accounts. */
	private int mAccountSelectedPosition = 0;

	/** The current context. */
	private final Context mContext = this;

	private Dialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
	protected void onDestroy() {
		super.onDestroy();

		// Dismiss dialog on destroy to prevent window leaking
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// Home / up button
			Intent upIntent = new Intent(this, HomeScreen.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so create a new task
				// with a synthesised back stack.
				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Manage UI Screens

	/**
	 * Sets up the 'connect' screen content.
	 */
	private void setConnectScreenContent() {
		List<String> accounts = getGoogleAccounts();
		if (Util.isDebug(mContext)) {
			// Use debug account
			Intent intent = new Intent(mContext, AccountService.class);
			intent.putExtra(Keys.INTENT_ACCOUNT, "android@jwlawson.co.uk");
			startService(intent);

			if (DEBUG) Log.d(TAG, "Using debug account android@jwlawson.co.uk");
			finish();
			return;
		}
		if (accounts.size() == 0) {
			// Show a dialog and invoke the "Add Account" activity if requested
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(R.string.needs_account);
			builder.setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
				}
			});
			builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Show some warning that the app needs an account?
					AccountsActivity.this.setResult(RESULT_CANCELED);
					finish();
				}
			});
			builder.setIcon(android.R.drawable.stat_sys_warning);
			builder.setTitle(R.string.attention);
			mDialog = builder.create();
			mDialog.show();

		} else {
			final ListView listView = (ListView) findViewById(R.id.select_account);
			listView.setAdapter(new ArrayAdapter<String>(mContext, R.layout.account_list_item, accounts));
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setItemChecked(mAccountSelectedPosition, true);

			final Button connectButton = (Button) findViewById(R.id.connect);
			connectButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Register in the background and terminate the activity
					mAccountSelectedPosition = listView.getCheckedItemPosition();
					TextView account = (TextView) listView.getChildAt(mAccountSelectedPosition);

					// Offload registering to background service
					Intent intent = new Intent(mContext, AccountService.class);
					intent.setAction(Keys.ACTION_REGISTER);
					intent.putExtra(Keys.INTENT_ACCOUNT, account.getText().toString());
					startService(intent);
					if (DEBUG) Log.d(TAG, "Account service intent fired ");

					AccountsActivity.this.setResult(RESULT_OK);
					finish();
				}
			});

			final Button exitButton = (Button) findViewById(R.id.exit);
			exitButton.setOnClickListener(new OnClickListener() {
				@Override
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

	// Utility Methods

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
