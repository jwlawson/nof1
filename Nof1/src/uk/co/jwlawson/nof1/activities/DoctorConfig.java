/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  WMG, University of Warwick
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
package uk.co.jwlawson.nof1.activities;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import android.app.AlertDialog;
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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Configuration activity that starts with a login for the doctor, then allows
 * changes to treatment schedule and questions.
 * 
 * @author John Lawson
 * 
 */
public class DoctorConfig extends SherlockFragmentActivity {

	private static final String TAG = "DoctorConfig";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	/** EditText with doctor's email */
	private EditText mDocEmail;

	/** EditText with pharmacist's email */
	private EditText mPharmEmail;

	/** EditText with patient name */
	private EditText mPatientName;

	/** EditText with number of days in treatment period */
	private EditText mPeriodLength;

	/** EditText with number of treatment periods */
	private EditText mPeriodNumber;

	public DoctorConfig() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_doctor);

		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

		Intent i = getIntent();
		String email = i.getStringExtra(Keys.INTENT_EMAIL);

		mDocEmail = (EditText) findViewById(R.id.config_doctor_details_edit_doc_email);
		mDocEmail.setText(email);

		mPharmEmail = (EditText) findViewById(R.id.config_doctor_details_edit_pharm_email);

		mPatientName = (EditText) findViewById(R.id.config_doctor_details_edit_pharm_email);
		mPatientName.setText(sp.getString(Keys.CONFIG_PATIENT_NAME, ""));

		mPeriodLength = (EditText) findViewById(R.id.config_timescale_edit_period);
		mPeriodLength.setText(sp.getString(Keys.CONFIG_PERIOD_LENGTH, ""));

		mPeriodNumber = (EditText) findViewById(R.id.config_timescale_edit_number_periods);
		mPeriodNumber.setText(sp.getString(Keys.CONFIG_NUMBER_PERIODS, ""));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_doctor_config, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());
		switch (item.getItemId()) {
		case R.id.menu_doctor_config_done:
			// Config done. Save data and email stuff away.
			save();
			makeTreatmentPlan();
			email();
			return true;
		case R.id.menu_doctor_config_login:
			// Change login details
			changeLogin();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	/** Create a random treatment plan, which must stay hidden but sent to the pharmacist. */
	private void makeTreatmentPlan() {
		// TODO
	}

	/** Save the data to file. */
	private void save() {
		// Put config data into shared preferences editor
		SharedPreferences.Editor editor = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE).edit();
		editor.putString(Keys.CONFIG_PATIENT_NAME, mPatientName.getText().toString());
		editor.putString(Keys.CONFIG_NUMBER_PERIODS, mPeriodNumber.getText().toString());
		editor.putString(Keys.CONFIG_PERIOD_LENGTH, mPeriodLength.getText().toString());

		// Save changes
		editor.commit();
	}

	/** email the information to the doctor and pharmacist */
	private void email() {
		// TODO
		String pharmEmail = mPharmEmail.getText().toString();
		String docEmail = mDocEmail.getText().toString();
	}

	/** Loads AlertDialog to change the login details of doctor */
	private void changeLogin() {

		final SharedPreferences sharedPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.dialog_theme));

		View view = getInflater().inflate(R.layout.config_change_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_change_login_edit_cur_email);
		email.setText(mDocEmail.getText().toString());

		final EditText newEmail = (EditText) view.findViewById(R.id.config_change_login_edit_new_email);

		final EditText pass = (EditText) view.findViewById(R.id.config_change_login_edit_cur_password);

		final EditText newPass = (EditText) view.findViewById(R.id.config_change_login_edit_new_password);
		final EditText newPass2 = (EditText) view.findViewById(R.id.config_change_login_edit_new_pass2);

		builder.setView(view).setTitle(R.string.change_login_details).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String emailHash = new String(Hex.encodeHex(DigestUtils.sha512(email.getText().toString())));
				String passHash = new String(Hex.encodeHex(DigestUtils.sha512(pass.getText().toString())));

				if (emailHash.equals(sharedPrefs.getString(Keys.CONFIG_EMAIL, null))
						&& passHash.equals(sharedPrefs.getString(Keys.CONFIG_PASS, null))) {
					// Login correct

					String passStr = newPass.getText().toString();
					SharedPreferences.Editor editor = sharedPrefs.edit();

					if (passStr != "" && passStr != null && passStr.equals(newPass2.getText().toString())) {
						// Change password

						editor.putString(Keys.CONFIG_PASS, new String(Hex.encodeHex(DigestUtils.sha512(passStr))));

					}

					String newEmailStr = newEmail.getText().toString();
					if (newEmailStr != null && newEmailStr != "") {
						// Change email
						editor.putString(Keys.CONFIG_EMAIL, new String(Hex.encodeHex(DigestUtils.sha512(newEmailStr))));
						setEmail(newEmailStr);
					}
					// Save changes
					editor.commit();
				} else {
					// Incorrect login
					Toast.makeText(getApplicationContext(), "Incorrect login details", Toast.LENGTH_SHORT).show();
					changeLogin();
				}
			}
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Close dialog
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/** Used to set the email field in EditText from an onClick call */
	private void setEmail(String email) {
		mDocEmail.setText(email);
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
}
