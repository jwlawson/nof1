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
package org.nof1trial.nof1;

import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.ConfigProxy;
import org.nof1trial.nof1.shared.ConfigRequest;
import org.nof1trial.nof1.shared.MyRequestFactory;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * @author John Lawson
 * 
 */
public class Saver extends IntentService {

	/**
	 * @param name
	 */
	public Saver(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (Keys.ACTION_SAVE_CONFIG.equals(intent.getAction())) {
			// SAve config data to disk and online

			// get values from the intent
			String patientName = intent.getStringExtra(Keys.CONFIG_PATIENT_NAME);
			String doctorName = intent.getStringExtra(Keys.CONFIG_DOCTOR_NAME);
			final String doctorEmail = intent.getStringExtra(Keys.CONFIG_DOC);
			String pharmEmail = intent.getStringExtra(Keys.CONFIG_PHARM);
			int numberPeriods = intent.getIntExtra(Keys.CONFIG_NUMBER_PERIODS, 0);
			int periodLength = intent.getIntExtra(Keys.CONFIG_PERIOD_LENGTH, 0);
			String startDate = intent.getStringExtra(Keys.CONFIG_START);
			String treatmentA = intent.getStringExtra(Keys.CONFIG_TREATMENT_A);
			String treatmentB = intent.getStringExtra(Keys.CONFIG_TREATMENT_B);
			String treatmentNotes = intent.getStringExtra(Keys.CONFIG_TREATMENT_NOTES);
			boolean formBuilt = intent.getBooleanExtra(Keys.CONFIG_BUILT, false);

			// Save to file
			SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Keys.CONFIG_PATIENT_NAME, patientName);
			editor.putString(Keys.CONFIG_DOCTOR_NAME, doctorName);
			editor.putString(Keys.CONFIG_DOC, doctorEmail);
			editor.putInt(Keys.CONFIG_NUMBER_PERIODS, numberPeriods);
			editor.putInt(Keys.CONFIG_PERIOD_LENGTH, periodLength);
			editor.putBoolean(Keys.CONFIG_BUILT, formBuilt);
			editor.putString(Keys.CONFIG_START, startDate);

			for (int i = 0; intent.hasExtra(Keys.CONFIG_TIME + i); i++) {
				editor.putString(Keys.CONFIG_TIME + i, intent.getStringExtra(Keys.CONFIG_TIME + i));
			}

			editor.putString(Keys.CONFIG_TREATMENT_A, treatmentA);
			editor.putString(Keys.CONFIG_TREATMENT_B, treatmentB);
			editor.putString(Keys.CONFIG_TREATMENT_NOTES, treatmentNotes);

			for (int i = 0; intent.hasExtra(Keys.CONFIG_DAY + i); i++) {
				editor.putBoolean(Keys.CONFIG_DAY + i, intent.getBooleanExtra(Keys.CONFIG_DAY + i, false));
			}
			editor.commit();

			// Request backup
			backup();

			// Save online
			// Get request factory
			MyRequestFactory factory = (MyRequestFactory) Util.getRequestFactory(Saver.this, MyRequestFactory.class);
			ConfigRequest request = factory.configRequest();

			// Build config
			ConfigProxy conf = request.create(ConfigProxy.class);
			conf.setDoctorEmail(doctorEmail);
			conf.setDoctorName(doctorName);
			conf.setPatientName(patientName);
			conf.setPharmEmail(pharmEmail);
			conf.setStartDate(startDate);
			conf.setLengthPeriods((long) periodLength);
			conf.setNumberPeriods((long) numberPeriods);
			conf.setTreatmentA(treatmentA);
			conf.setTreatmentB(treatmentB);
			conf.setTreatmentNotes(treatmentNotes);

			// Update online
			request.update(conf).fire();

		}
	}

	@TargetApi(8)
	private void backup() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			BackupManager backup = new BackupManager(this);
			backup.dataChanged();
		}
	}
}
