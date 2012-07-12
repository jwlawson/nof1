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

import uk.co.jwlawson.nof1.R;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

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

		Intent i = getIntent();
		String email = i.getStringExtra("email");

		mDocEmail = (EditText) findViewById(R.id.config_doctor_details_edit_doc_email);
		mDocEmail.setText(email);

		mPharmEmail = (EditText) findViewById(R.id.config_doctor_details_edit_pharm_email);
		mPatientName = (EditText) findViewById(R.id.config_doctor_details_edit_pharm_email);

		mPeriodLength = (EditText) findViewById(R.id.config_timescale_edit_period);
		mPeriodNumber = (EditText) findViewById(R.id.config_timescale_edit_number_periods);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_doctor_config, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

}
