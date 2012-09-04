/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson
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
package org.nof1trial.nof1.activities;

import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Activity launcher for doctor to login to to get access to doctor config utility.
 * 
 * @author John Lawson
 * 
 */
public class DoctorLogin extends SherlockActivity implements DialogInterface.OnCancelListener {

	private static final String TAG = "DoctorLogin";
	private static final boolean DEBUG = false;

	private static final int THEME = R.style.dialog_theme;

	private static final int REQUEST_CONFIG = 13;

	/** Dialog shown for login details */
	private Dialog mDialog;

	/** Manager that handles backing up data */
	private BackupManager mBackupManager;

	public DoctorLogin() {
	}

	@TargetApi(8)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager = new BackupManager(this);
		}

		if (sharedPrefs.getBoolean(Keys.CONFIG_FIRST, true)) {

			// First time the app has been run.
			firstLogin(sharedPrefs, null);

		} else {

			// Not the first time, so login already made
			login(sharedPrefs, null);

		}

	}

	private void firstLogin(final SharedPreferences sharedPrefs, String emailStr) {

		// First time the app has been run. Set up doctor login.
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, THEME));
		View view = getInflater().inflate(R.layout.config_doctor_first_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_doctor_first_edit_email);
		email.setText(emailStr);

		final EditText pass = (EditText) view.findViewById(R.id.config_doctor_first_edit_pass);

		final EditText pass2 = (EditText) view.findViewById(R.id.config_doctor_first_edit_pass2);

		builder.setTitle(R.string.new_login_details).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String passStr = pass.getText().toString();
				String pass2Str = pass2.getText().toString();
				String emailStr1 = email.getText().toString();
				if (DEBUG) Log.d(TAG, "Email entered: " + emailStr1);

				// Check fields aren't empty
				if (passStr != "" && emailStr1 != "") {

					if (passStr.equals(pass2Str)) {

						// generate unique ID
						UUID uniqueId = UUID.randomUUID();
						String uuidStr = uniqueId.toString();

						// Hash and salt the email and password, then add to shared preferences.
						// This will be what we check against at further logins.
						SharedPreferences.Editor editor = sharedPrefs.edit();
						editor.putString(Keys.CONFIG_UUID, uuidStr);
						editor.putString(Keys.CONFIG_EMAIL, new String(Hex.encodeHex(DigestUtils.sha512(emailStr1 + uuidStr))));
						editor.putString(Keys.CONFIG_PASS, new String(Hex.encodeHex(DigestUtils.sha512(passStr + uuidStr))));
						editor.putBoolean(Keys.CONFIG_FIRST, false);
						editor.commit();

						backup();

						launch(emailStr1);
					} else {
						// Password fields don't match.
						Toast.makeText(getBaseContext(), R.string.passwords_not_equal, Toast.LENGTH_SHORT).show();
						firstLogin(sharedPrefs, emailStr1);
					}
				} else {
					// Empty email and password
					Toast.makeText(getApplicationContext(), R.string.enter_login_details, Toast.LENGTH_SHORT).show();
					firstLogin(sharedPrefs, emailStr1);
				}
			}
		});
		builder.setOnCancelListener(this);
		mDialog = builder.create();
		mDialog.show();
	}

	private void login(final SharedPreferences sharedPrefs, String emailStr) {
		// Not the first time run, check login against sharedprefs.
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, THEME));

		View view = getInflater().inflate(R.layout.config_doctor_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_doctor_login_edit_email);
		email.setText(emailStr);

		final EditText pass = (EditText) view.findViewById(R.id.config_doctor_login_edit_pass);

		builder.setView(view).setTitle(R.string.login).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String uuidStr = sharedPrefs.getString(Keys.CONFIG_UUID, "");

				String emailStr = email.getText().toString();
				// Hash email and password
				String emailHash = new String(Hex.encodeHex(DigestUtils.sha512(emailStr + uuidStr)));
				String passHash = new String(Hex.encodeHex(DigestUtils.sha512(pass.getText().toString() + uuidStr)));

				// Compare hashes
				if (emailHash.equals(sharedPrefs.getString(Keys.CONFIG_EMAIL, null))
						&& passHash.equals(sharedPrefs.getString(Keys.CONFIG_PASS, null))) {
					// Login correct
					launch(emailStr);
				} else {
					// Incorrect login
					Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
					login(sharedPrefs, emailStr);
				}
			}
		});
		builder.setOnCancelListener(this);
		mDialog = builder.create();
		mDialog.show();
	}

	private void launch(String emailStr) {
		// Launch DoctorConfig activity
		Intent i = new Intent(this, DoctorConfig.class);
		i.putExtra(Keys.INTENT_EMAIL, emailStr);
		startActivityForResult(i, REQUEST_CONFIG);
		finish();
	}

	@TargetApi(8)
	private void backup() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager.dataChanged();
		}
	}

	/** Nasty hack to ensure text in alertdialog is readable */
	private LayoutInflater getInflater() {
		LayoutInflater inflater;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Old devices use nasty dark AlertDialog theme, so inflater needs to make text white.
			// ApplicationContext for some reason doesn't have the light theme applied, so will do nicely.
			inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} else {
			// Newer devices use shiny holo light dialogs. Easy.
			inflater = this.getLayoutInflater();
		}
		return inflater;
	}

	@Override
	protected void onDestroy() {
		// Close dialog if open to prevent leak
		if (mDialog != null) {
			mDialog.dismiss();
		}
		if (DEBUG) Log.d(TAG, "Activity destroyed");
		super.onDestroy();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// When a dialog is closed, finish the activity.
		// Otherwise users are left with a blank screen
		dialog.dismiss();
		setResult(RESULT_CANCELED);
		this.finish();
	}
}
