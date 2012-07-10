/*******************************************************************************
 * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package uk.co.jwlawson.nof1.activities;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import uk.co.jwlawson.nof1.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Activity launcher for doctor to login to to get access to doctor config
 * utility.
 * 
 * @author John Lawson
 * 
 */
public class DoctorLogin extends SherlockActivity {

	private static final int THEME = R.style.dialog_theme;

	public DoctorLogin() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPrefs = getSharedPreferences("config", MODE_PRIVATE);

		if (sharedPrefs.getBoolean("first_run", true)) {

			firstLogin(sharedPrefs, null);

		} else {

			login(sharedPrefs, null);

		}

	}

	private void firstLogin(final SharedPreferences sharedPrefs, String emailStr) {

		// First time the app has been run. Set up doctor login.
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, THEME));
		View view = getLayoutInflater().inflate(R.layout.config_doctor_first_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_doctor_first_edit_email);
		email.setText(emailStr);

		final EditText pass = (EditText) view.findViewById(R.id.config_doctor_first_edit_pass);

		final EditText pass2 = (EditText) view.findViewById(R.id.config_doctor_first_edit_pass2);

		builder.setTitle("Set up login details").setView(view)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String passStr = pass.getText().toString();
						String pass2Str = pass2.getText().toString();
						String emailStr1 = email.getText().toString();

						if (passStr.equals(pass2Str)) {

							// Hash the email and password, then add to
							// sharedpreferences.
							// This will be what we check against at further
							// logins.
							SharedPreferences.Editor editor = sharedPrefs.edit();
							editor.putString("email_hash",
									new String(Hex.encodeHex(DigestUtils.sha512(emailStr1))));
							editor.putString("pass_hash",
									new String(Hex.encodeHex(DigestUtils.sha512(passStr))));
							editor.putBoolean("first_run", false);
							editor.commit();

							launch();
						} else {
							// Password fields don't match.
							Toast.makeText(getBaseContext(), "Passwords don't match",
									Toast.LENGTH_SHORT).show();
							firstLogin(sharedPrefs, emailStr1);
						}
					}
				});
		builder.create().show();
	}

	private void login(final SharedPreferences sharedPrefs, String emailStr) {
		// Not the first time run, check login against sharedprefs.
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, THEME));

		View view = getLayoutInflater().inflate(R.layout.config_doctor_login, null, false);

		final EditText email = (EditText) view.findViewById(R.id.config_doctor_login_edit_email);
		email.setText(emailStr);

		final EditText pass = (EditText) view.findViewById(R.id.config_doctor_login_edit_pass);

		builder.setView(view).setTitle("Login")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String emailStr = email.getText().toString();
						String emailHash = new String(Hex.encodeHex(DigestUtils.sha512(emailStr)));
						String passHash = new String(Hex.encodeHex(DigestUtils.sha512(pass
								.getText().toString())));

						if (emailHash.equals(sharedPrefs.getString("email_hash", null))
								&& passHash.equals(sharedPrefs.getString("pass_hash", null))) {
							// Login correct
							launch();
						} else {
							// Incorrect login
							Toast.makeText(getApplicationContext(), "Login failed",
									Toast.LENGTH_SHORT).show();
							login(sharedPrefs, emailStr);
						}
					}
				});
		builder.create().show();
	}

	private void launch() {
		// Launch DoctorConfig activity
		Intent i = new Intent(this, DoctorConfig.class);
		startActivity(i);
		finish();
	}
}
