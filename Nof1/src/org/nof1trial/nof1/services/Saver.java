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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.DataSource;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.NetworkChangeReceiver;
import org.nof1trial.nof1.SQLite;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.ConfigProxy;
import org.nof1trial.nof1.shared.ConfigRequest;
import org.nof1trial.nof1.shared.DataProxy;
import org.nof1trial.nof1.shared.DataRequest;
import org.nof1trial.nof1.shared.MyRequestFactory;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * @author John Lawson
 * 
 */
public class Saver extends IntentService {

	private static final String TAG = "Saver";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	/** Shared Preferences file for storing cached data to be sent to server */
	private static final String CACHE = "config";
	private static final String NUM_DATA = "num_data_cache";
	private static final String BOOL_CONFIG = "bool_config_cache";

	/** Current context */
	private final Context mContext = this;

	public Saver() {
		this("Saver");
	}

	public Saver(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		if (DEBUG) Log.d(TAG, "Saver service started");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG, "Saver service destroyed");
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (DEBUG) Log.d(TAG, "Handling new intent");

		if (Keys.ACTION_SAVE_CONFIG.equals(intent.getAction())) {
			// SAve config data to disk and online
			if (DEBUG) Log.d(TAG, "Saving config to disk");

			saveConfig(intent);

		} else if (Keys.ACTION_SAVE_DATA.equals(intent.getAction())) {
			// Save patient inputted data from the intent
			if (DEBUG) Log.d(TAG, "Savng data to disk");

			saveData(intent);

		} else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			// Phone has booted, check to see whether need to upload anything
			// If so either do so, or enable Network change listener
			if (DEBUG) Log.d(TAG, "Checking for data to upload");

			boolean startListener = false;

			boolean isConnected = isConnected();

			SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);
			if (sp.getInt(NUM_DATA, 0) > 0) {
				// Have data to upload

				if (isConnected) {
					// Have internet so upload data

					for (int count = sp.getInt(NUM_DATA, 0); count > 0; count--) {

						uploadCachedDataAt(count, sp);
					}

				} else {
					// Not connected, so want to start listener
					startListener = true;
				}
			}

			if (sp.getBoolean(BOOL_CONFIG, false)) {
				// Have config data to upload
				if (isConnected) {
					uploadConfigFromPrefs(sp);
				} else {
					// Not connected, so want to start listener
					startListener = true;
				}
			}
			if (startListener) {
				enableNetworkChangeReceiver();
			}

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			// Connectivity changed, should mean we are now connected
			if (DEBUG) Log.d(TAG, "Uploading previously saved data");

			boolean uploadedData = false;

			SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);
			if (sp.getInt(NUM_DATA, 0) > 0) {
				// Have data to upload
				if (DEBUG) Log.d(TAG, "Have data to upload");

				for (int count = sp.getInt(NUM_DATA, 0); count > 0; count--) {

					uploadCachedDataAt(count, sp);

				}
				uploadedData = true;
			}

			if (sp.getBoolean(BOOL_CONFIG, false)) {
				// Have config data to upload
				// Note, if config data not uploaded at start, there is no way
				// that the schedule can be made or emails
				// sent
				if (DEBUG) Log.d(TAG, "Have config to send");

				uploadConfigFromPrefs(sp);

				uploadedData = true;
			}

			if (uploadedData) {
				// Disable network change listener, as not needed
				disableNetworkChangeReceiver();
			}

		} else if (Keys.ACTION_UPLOAD_ALL.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Uploading all saved data");

			// get config from prefs and upload
			SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			String patientName = prefs.getString(Keys.CONFIG_PATIENT_NAME, "");
			String doctorName = prefs.getString(Keys.CONFIG_DOCTOR_NAME, "");
			final String doctorEmail = prefs.getString(Keys.CONFIG_DOC, "");
			String pharmEmail = prefs.getString(Keys.CONFIG_PHARM, "");
			int numberPeriods = prefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0);
			int periodLength = prefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0);
			String startDate = prefs.getString(Keys.CONFIG_START, "");
			String treatmentA = prefs.getString(Keys.CONFIG_TREATMENT_A, "");
			String treatmentB = prefs.getString(Keys.CONFIG_TREATMENT_B, "");
			String treatmentNotes = prefs.getString(Keys.CONFIG_TREATMENT_NOTES, "");

			ArrayList<String> quesList = new ArrayList<String>();
			SharedPreferences ques = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
			for (int i = 0; ques.contains(Keys.QUES_TEXT + i); i++) {
				quesList.add(ques.getString(Keys.QUES_TEXT + i, ""));
			}

			uploadConfig(doctorEmail, doctorName, patientName, pharmEmail, startDate, periodLength, numberPeriods, treatmentA, treatmentB,
					treatmentNotes, quesList);

			if (quesList.size() >= 1) {
				// Query database for all saved data
				DataSource datasource = new DataSource(mContext);
				datasource.open();
				Cursor cursor = datasource.getAllColumns();
				cursor.moveToFirst();

				// Upload all data from the database
				while (!cursor.isAfterLast()) {

					uploadDataFromCursor(cursor);

					cursor.moveToNext();
				}
				cursor.close();
				datasource.close();
			}
		} else {
			Log.w(TAG, "IntentService started with unrecognised action");
		}

	}

	private void uploadDataFromCursor(Cursor cursor) {
		int dayCol = cursor.getColumnIndex(SQLite.COLUMN_DAY);
		int timeCol = cursor.getColumnIndex(SQLite.COLUMN_TIME);
		int commentCol = cursor.getColumnIndex(SQLite.COLUMN_COMMENT);

		int day = cursor.getInt(dayCol);
		long time = cursor.getLong(timeCol);
		String comment = cursor.getString(commentCol);
		int size = commentCol - timeCol - 1;
		int[] data = new int[size];

		// Iterate over to get
		for (int i = 0; i < size; i++) {
			data[i] = cursor.getInt(timeCol + 1 + i);
		}

		uploadData(day, time, data, comment);
	}

	private void saveData(Intent intent) {
		int day = intent.getIntExtra(Keys.DATA_DAY, 0);
		long time = intent.getLongExtra(Keys.DATA_TIME, 0);
		String comment = intent.getStringExtra(Keys.DATA_COMMENT);
		int[] data = intent.getIntArrayExtra(Keys.DATA_LIST);

		// Open database
		DataSource source = new DataSource(this);
		source.open();
		// Save data
		source.saveData(day, time, data, comment);

		source.close();

		// Request backup
		backup();

		boolean isConnected = isConnected();

		if (isConnected) {
			if (DEBUG) Log.d(TAG, "Connected to internet, sending data to server");

			uploadData(day, time, data, comment);

		} else {
			// Not connected to internet
			saveDataForLater(day, time, comment, data);

			enableNetworkChangeReceiver();
		}
	}

	private void saveConfig(Intent intent) {
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
		ArrayList<String> quesList = intent.getStringArrayListExtra(Keys.CONFIG_QUESTION_LIST);

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
		editor.putString(Keys.CONFIG_PHARM, pharmEmail);

		for (int i = 0; intent.hasExtra(Keys.CONFIG_TIME + i); i++) {
			editor.putString(Keys.CONFIG_TIME + i, intent.getStringExtra(Keys.CONFIG_TIME + i));
		}

		editor.putString(Keys.CONFIG_TREATMENT_A, treatmentA);
		editor.putString(Keys.CONFIG_TREATMENT_B, treatmentB);
		editor.putString(Keys.CONFIG_TREATMENT_NOTES, treatmentNotes);

		for (int i = 1; intent.hasExtra(Keys.CONFIG_DAY + i); i++) {
			editor.putBoolean(Keys.CONFIG_DAY + i, intent.getBooleanExtra(Keys.CONFIG_DAY + i, false));
		}
		editor.commit();
		backup();

		if (isConnected()) {
			uploadConfig(doctorEmail, doctorName, patientName, pharmEmail, startDate, periodLength, numberPeriods, treatmentA, treatmentB,
					treatmentNotes, quesList);

		} else {
			// No internet, so set flag to upload later
			prefs.edit().putBoolean(BOOL_CONFIG, true).commit();

			enableNetworkChangeReceiver();
		}
	}

	private void uploadCachedDataAt(int id, SharedPreferences cachPrefs) {
		int day = cachPrefs.getInt(Keys.DATA_DAY + id, 0);
		long time = cachPrefs.getLong(Keys.DATA_TIME + id, 0);
		String comment = cachPrefs.getString(Keys.DATA_COMMENT + id, "");
		String dataStr = cachPrefs.getString(Keys.DATA_LIST + id, "");

		int[] data = null;
		if (dataStr.length() != 0) {
			String[] dataStrArr = dataStr.split(",");
			data = new int[dataStrArr.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = Integer.parseInt(dataStrArr[i]);
			}
		}

		// Remove data from prefs
		SharedPreferences.Editor editor = cachPrefs.edit();
		editor.putString(Keys.DATA_DAY + id, null);
		editor.putString(Keys.DATA_TIME + id, null);
		editor.putString(Keys.DATA_COMMENT + id, null);
		editor.putString(Keys.DATA_LIST + id, null);

		// decrement counter in prefs
		editor.putInt(NUM_DATA, id - 1).commit();

		uploadData(day, time, data, comment);
	}

	private void uploadConfigFromPrefs(SharedPreferences sp) {
		// get config from prefs and upload
		SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
		String patientName = prefs.getString(Keys.CONFIG_PATIENT_NAME, "");
		String doctorName = prefs.getString(Keys.CONFIG_DOCTOR_NAME, "");
		final String doctorEmail = prefs.getString(Keys.CONFIG_DOC, "");
		String pharmEmail = prefs.getString(Keys.CONFIG_PHARM, "");
		int numberPeriods = prefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0);
		int periodLength = prefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0);
		String startDate = prefs.getString(Keys.CONFIG_START, "");
		String treatmentA = prefs.getString(Keys.CONFIG_TREATMENT_A, "");
		String treatmentB = prefs.getString(Keys.CONFIG_TREATMENT_B, "");
		String treatmentNotes = prefs.getString(Keys.CONFIG_TREATMENT_NOTES, "");

		ArrayList<String> quesList = new ArrayList<String>();
		SharedPreferences ques = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
		for (int i = 0; ques.contains(Keys.QUES_TEXT + i); i++) {
			quesList.add(ques.getString(Keys.QUES_TEXT + i, ""));
		}
		// remove flag in prefs
		sp.edit().putBoolean(BOOL_CONFIG, false).commit();

		uploadConfig(doctorEmail, doctorName, patientName, pharmEmail, startDate, periodLength, numberPeriods, treatmentA, treatmentB,
				treatmentNotes, quesList);
	}

	private void uploadConfig(final String doctorEmail, final String doctorName, final String patientName, final String pharmEmail,
			final String startDate, final long periodLength, final long numberPeriods, final String treatmentA, final String treatmentB,
			final String treatmentNotes, final List<String> quesList) {
		// Get request factory
		MyRequestFactory factory = Util.getRequestFactory(Saver.this, MyRequestFactory.class);
		ConfigRequest request = factory.configRequest();

		// Parse start date
		String[] arr = startDate.split(":");
		int[] date = new int[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]) };
		Calendar cal = Calendar.getInstance();
		cal.set(date[2], date[1], date[0], 23, 59);
		cal.add(Calendar.DAY_OF_MONTH, (int) (2 * periodLength * numberPeriods));

		// Build config
		ConfigProxy conf = request.create(ConfigProxy.class);
		conf.setDocEmail(doctorEmail);
		conf.setDoctorName(doctorName);
		conf.setPatientName(patientName);
		conf.setPharmEmail(pharmEmail);
		conf.setStartDate(startDate);
		conf.setLengthPeriods(periodLength);
		conf.setNumberPeriods(numberPeriods);
		conf.setTreatmentA(treatmentA);
		conf.setTreatmentB(treatmentB);
		conf.setTreatmentNotes(treatmentNotes);
		conf.setQuestionList(quesList);
		conf.setEndDate(cal.getTimeInMillis());

		// Update online
		if (DEBUG) Log.d(TAG, "RequestFactory Config update sent");
		request.update(conf).fire(new Receiver<ConfigProxy>() {

			@Override
			public void onSuccess(ConfigProxy response) {
				if (DEBUG) Log.d(TAG, "Config request successful");
			}

			@Override
			public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
				for (ConstraintViolation<?> con : violations) {
					Log.e(TAG, con.getMessage());
					// TODO Ask user to check config info
				}
			}

			@Override
			public void onFailure(ServerFailure error) {
				Log.e(TAG, "Config not saved");
				Log.e(TAG, error.getMessage());

				// TODO Only refresh cookie when error is an auth error
				// TODO Only allow looping a certain number of times
				// Try refreshing auth cookie
				Intent intent = new Intent(mContext, AccountService.class);
				intent.setAction(Keys.ACTION_REFRESH);
				startService(intent);

				// Save for later
				getSharedPreferences(CACHE, MODE_PRIVATE).edit().putBoolean(BOOL_CONFIG, true).commit();

				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
				manager.registerReceiver(new CookieReceiver(), new IntentFilter(Keys.ACTION_COMPLETE));

			}

		});
	}

	/**
	 * Upload data to server.
	 * 
	 * Should check whether internet is available before trying to call this.
	 * 
	 * @param day
	 * @param time
	 * @param data
	 * @param comment
	 */
	private void uploadData(final int day, final long time, final int[] data, final String comment) {
		// Send the data file to server
		MyRequestFactory requestFactory = Util.getRequestFactory(this, MyRequestFactory.class);
		DataRequest request = requestFactory.dataRequest();

		DataProxy proxy = request.create(DataProxy.class);
		proxy.setDay(day);
		proxy.setTime(time);
		proxy.setComment(comment);

		ArrayList<Integer> list = new ArrayList<Integer>();
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				list.add(data[i]);
			}
		}
		proxy.setQuestionData(list);

		request.save(proxy).fire(new Receiver<DataProxy>() {

			@Override
			public void onSuccess(DataProxy response) {
				if (DEBUG) Log.d(TAG, "Data saved successfully");
			}

			@Override
			public void onFailure(ServerFailure error) {
				// super.onFailure(error);
				Log.e(TAG, "Data not saved");
				Log.e(TAG, error.getMessage());

				// TODO Only refresh cookie when error is an auth error
				// TODO Only allow looping a certain number of times
				// Try refreshing auth cookie
				Intent intent = new Intent(mContext, AccountService.class);
				intent.setAction(Keys.ACTION_REFRESH);
				startService(intent);

				// Save to upload later
				saveDataForLater(day, time, comment, data);

				// Register receiver to get callback from the AccountService
				// when cookie refreshed
				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
				manager.registerReceiver(new CookieReceiver(), new IntentFilter(Keys.ACTION_COMPLETE));

			}

		});
	}

	private void saveDataForLater(int day, long time, String comment, int[] data) {
		SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();

		int count = sp.getInt(NUM_DATA, 0) + 1;
		// Increment counter for number of cached data sets
		edit.putInt(NUM_DATA, count);

		edit.putInt(Keys.DATA_DAY + count, day);
		edit.putLong(Keys.DATA_TIME + count, time);
		edit.putString(Keys.DATA_COMMENT + count, comment);

		if (data != null) {
			// Convert int array to string, to allow storage in shared_prefs
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				sb.append(data[i]).append(",");
			}
			// Remove trailing comma
			sb.deleteCharAt(sb.length() - 1);
			edit.putString(Keys.DATA_LIST + count, sb.toString());
		}

		edit.commit();
	}

	private void enableNetworkChangeReceiver() {
		PackageManager pm = getPackageManager();
		ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
		pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	private void disableNetworkChangeReceiver() {
		PackageManager pm = getPackageManager();
		ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
		pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());
		return isConnected;
	}

	@TargetApi(8)
	private void backup() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			BackupManager backup = new BackupManager(this);
			backup.dataChanged();
		}
	}

	private class CookieReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) Log.d(TAG, "Received broadcast");

			// Pass connectivity change to saver
			Intent saver = new Intent(context, Saver.class);
			saver.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
			context.startService(saver);
			if (DEBUG) Log.d(TAG, "Saver started from CookieReceiver");

			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
			manager.unregisterReceiver(this);
		}

	}
}
