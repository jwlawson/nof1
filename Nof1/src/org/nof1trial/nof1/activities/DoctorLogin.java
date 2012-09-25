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
import org.apache.commons.lang3.RandomStringUtils;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.MyRequestFactory;
import org.nof1trial.nof1.shared.PassResetProxy;
import org.nof1trial.nof1.shared.PassResetRequest;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * Activity launcher for doctor to login to to get access to doctor config utility.
 * 
 * @author John Lawson
 * 
 */
public class DoctorLogin extends SherlockActivity implements DialogInterface.OnCancelListener, OnSharedPreferenceChangeListener {

	private static final String TAG = "DoctorLogin";
	private static final boolean DEBUG = false;

	private static final int THEME = R.style.dialog_theme;

	private static final int REQUEST_CONFIG = 13;

	private Context mContext = this;

	/** Dialog shown for login details */
	private Dialog mDialog;

	private Dialog mSecondDialog;

	/** Manager that handles backing up data */
	private BackupManager mBackupManager;

	private SharedPreferences sharedPrefs;

	private String passHash;
	private String emailHash;
	private String uuidStr;

	@TargetApi(8)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
		passHash = sharedPrefs.getString(Keys.CONFIG_PASS, "");
		emailHash = sharedPrefs.getString(Keys.CONFIG_EMAIL, "");
		uuidStr = sharedPrefs.getString(Keys.CONFIG_UUID, "");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager = new BackupManager(mContext);
		}

		if (sharedPrefs.getBoolean(Keys.CONFIG_FIRST, true)) {
			firstLogin(null);
		} else {
			login(null);
		}

	}

	private void firstLogin(String emailStr) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, THEME));
		final View view = getInflater().inflate(R.layout.config_doctor_first_login, null, false);

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

						saveLoginDetails(passStr, emailStr1);

						launchConfig(emailStr1);
					} else {
						// Password fields don't match.
						Toast.makeText(getBaseContext(), R.string.passwords_not_equal, Toast.LENGTH_SHORT).show();
						firstLogin(emailStr1);
					}
				} else {
					// Empty email and password
					Toast.makeText(getApplicationContext(), R.string.enter_login_details, Toast.LENGTH_SHORT).show();
					firstLogin(emailStr1);
				}
			}
		});
		builder.setOnCancelListener(this);
		mDialog = builder.create();
		mDialog.show();
	}

	private void login(String emailStr) {
		// Not the first time run, check login against sharedprefs.
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, THEME));

		View view = getInflater().inflate(R.layout.config_doctor_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_doctor_login_edit_email);
		email.setText(emailStr);

		final EditText pass = (EditText) view.findViewById(R.id.config_doctor_login_edit_pass);

		final TextView reset = (TextView) view.findViewById(R.id.config_doctor_login_reset);
		reset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String emailStr = email.getText().toString();
				if (emailHashCorrect(hash(emailStr, uuidStr))) {
					resetPassword(emailStr);
				} else {
					Toast.makeText(mContext, R.string.enter_email, Toast.LENGTH_SHORT).show();
				}
			}
		});

		builder.setView(view).setTitle(R.string.login).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				String emailStr = email.getText().toString();

				if (loginCorrect(emailStr, pass.getText().toString())) {
					launchConfig(emailStr);
				} else {
					// Incorrect login
					Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
					login(emailStr);
				}
			}
		});
		builder.setOnCancelListener(this);
		mDialog = builder.create();
		mDialog.show();
	}

	private String hash(String string, String salt) {
		return new String(Hex.encodeHex(DigestUtils.sha512(salt + string)));
	}

	private boolean loginCorrect(String email, String pass) {
		String emailHash = hash(email, uuidStr);
		String passHash = hash(pass, uuidStr);

		return loginHashCorrect(emailHash, passHash);
	}

	private boolean loginHashCorrect(String emailHash, String passHash) {
		return emailHashCorrect(emailHash) && passHashCorrect(passHash);
	}

	private boolean passHashCorrect(String passHash) {
		return passHash.equals(this.passHash);
	}

	private boolean emailHashCorrect(String emailHash) {
		return emailHash.equals(this.emailHash);
	}

	private void launchConfig(String emailStr) {
		// Launch DoctorConfig activity
		Intent i = new Intent(mContext, DoctorConfig.class);
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
		if (mSecondDialog != null) {
			mSecondDialog.dismiss();
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

	private void saveLoginDetails(String passStr, String emailStr) {
		// generate unique ID
		UUID uniqueId = UUID.randomUUID();
		String uuidStr = uniqueId.toString();

		// Hash and salt the email and password, then add to shared preferences.
		// This will be what we check against at further logins.
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Keys.CONFIG_UUID, uuidStr);
		editor.putString(Keys.CONFIG_EMAIL, hash(emailStr, uuidStr));
		editor.putString(Keys.CONFIG_PASS, hash(passStr, uuidStr));
		editor.putBoolean(Keys.CONFIG_FIRST, false);
		editor.commit();

		backup();
	}

	private void resetPassword(String email) {
		String pass = RandomStringUtils.random(6, "123456789abcdefghijklmnopqrstuvwxyz");

		final ProgressDialog dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(true);
		dialog.setMessage(getText(R.string.reset_password));
		dialog.setCancelable(false);
		dialog.show();

		mSecondDialog = dialog;

		ResetTask task = new ResetTask();
		task.setEmail(email);
		task.setPass(pass);
		task.setDialog(dialog);
		task.execute();

		Log.d(TAG, "New password: " + pass);

	}

	private class ResetTask extends AsyncTask<Void, Void, Void> {

		private String email;
		private String pass;
		private ProgressDialog dialog;

		public void setEmail(String email) {
			this.email = email;
		}

		public void setPass(String pass) {
			this.pass = pass;
		}

		public void setDialog(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		protected Void doInBackground(Void... params) {
			MyRequestFactory factory = Util.getRequestFactory(mContext, MyRequestFactory.class);
			PassResetRequest request = factory.passResetRequest();

			PassResetProxy passReset = request.create(PassResetProxy.class);
			passReset.setDocEmail(email);
			passReset.setPass(pass);

			request.reset(passReset).fire(new Receiver<PassResetProxy>() {

				@Override
				public void onSuccess(PassResetProxy response) {

					saveLoginDetails(response.getPass(), response.getDocEmail());
					Log.d(TAG, "Saving new pass: " + response.getPass() + " email: " + response.getDocEmail());

					setDialogDoneWithMessageOnUiThread(R.string.reset_complete);
				}

				@Override
				public void onFailure(ServerFailure error) {
					setDialogDoneWithMessageOnUiThread(R.string.reset_error);
				}

			});
			return null;
		}

		private void setDialogDoneWithMessageOnUiThread(final int stringRes) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					setDialogDoneWithMessage(dialog, stringRes);
				}
			});
		}

	}

	private void setDialogDoneWithMessage(final ProgressDialog dialog, int stringRes) {
		dialog.dismiss();

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(getText(stringRes));
		builder.setCancelable(true);
		builder.setNeutralButton(getText(R.string.ok), new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});
		mSecondDialog = builder.create();
		mSecondDialog.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (Keys.CONFIG_EMAIL.equals(key)) {
			emailHash = sharedPreferences.getString(key, "");
		} else if (Keys.CONFIG_PASS.equals(key)) {
			passHash = sharedPreferences.getString(key, "");
		} else if (Keys.CONFIG_UUID.equals(key)) {
			uuidStr = sharedPreferences.getString(key, "");
		}
	}

}
