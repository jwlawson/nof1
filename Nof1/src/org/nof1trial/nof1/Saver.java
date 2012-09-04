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

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

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
			/*
			 * // Put config data into shared preferences editor
			 * SharedPreferences.Editor editor = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE).edit();
			 * 
			 * Intent intent = new Intent(Keys.ACTION_SAVE_CONFIG);
			 * 
			 * int number = 0;
			 * int length = 0;
			 * 
			 * // Names
			 * editor.putString(Keys.CONFIG_PATIENT_NAME, mPatientName.getText().toString());
			 * editor.putString(Keys.CONFIG_DOCTOR_NAME, mDocName.getText().toString());
			 * editor.putString(Keys.CONFIG_DOC, mDocEmail.getText().toString());
			 * 
			 * // Period number
			 * if (mPeriodNumber.getVisibility() == View.VISIBLE) {
			 * try {
			 * number = Integer.parseInt(mPeriodNumber.getText().toString());
			 * } catch (NumberFormatException e) {
			 * Toast.makeText(this, R.string.invalid_period_input, Toast.LENGTH_LONG).show();
			 * result = false;
			 * }
			 * } else {
			 * number = mIntPeriodNumber;
			 * }
			 * editor.putInt(Keys.CONFIG_NUMBER_PERIODS, number);
			 * 
			 * // Period length
			 * if (mPeriodLength.getVisibility() == View.VISIBLE) {
			 * try {
			 * length = Integer.parseInt(mPeriodLength.getText().toString());
			 * } catch (NumberFormatException e) {
			 * Toast.makeText(this, R.string.invalid_length_input, Toast.LENGTH_LONG).show();
			 * result = false;
			 * }
			 * } else {
			 * length = mIntPeriodLength;
			 * }
			 * editor.putInt(Keys.CONFIG_PERIOD_LENGTH, length);
			 * 
			 * editor.putBoolean(Keys.CONFIG_BUILT, mFormBuilt);
			 * 
			 * // Start date
			 * editor.putString(Keys.CONFIG_START, mDate.getDate());
			 * 
			 * // Save times to remind to take medicine
			 * String[] times = mTimeSetter.getTimes();
			 * for (int i = 0; i < times.length; i++) {
			 * editor.putString(Keys.CONFIG_TIME + i, times[i]);
			 * }
			 * 
			 * editor.putString(Keys.CONFIG_TREATMENT_A, mTreatmentA.getText().toString());
			 * editor.putString(Keys.CONFIG_TREATMENT_B, mTreatmentB.getText().toString());
			 * editor.putString(Keys.CONFIG_TREATMENT_NOTES, mAnyNotes.getText().toString());
			 * 
			 * // save checked boxes
			 * int[] arr = mArray.getSelected();
			 * for (int j = 1; j < length + 1; j++) {
			 * boolean contains = false;
			 * for (int k = 0; k < arr.length; k++) {
			 * if (j == arr[k]) contains = true;
			 * }
			 * editor.putBoolean(Keys.CONFIG_DAY + j, contains);
			 * }
			 * 
			 * // Save changes
			 * result &= editor.commit();
			 * // Ask for backup
			 * backup();
			 */
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... arg0) {
					MyRequestFactory factory = (MyRequestFactory) Util.getRequestFactory(Saver.this, MyRequestFactory.class);
					ConfigRequest request = factory.configRequest();

					ConfigProxy conf = request.create(ConfigProxy.class);

					request.update(conf).fire();

					return null;
				}

			}.execute();
		}
	}

}
