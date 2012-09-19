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
package org.nof1trial.nof1.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.DataSource;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.NetworkChangeReceiver;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.ConfigProxy;
import org.nof1trial.nof1.shared.ConfigRequest;
import org.nof1trial.nof1.shared.MyRequestFactory;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * Service to save a copy of the results as a .csv file
 * 
 * Actions:
 * org.nof1trial.nof1.TRIAL_COMPLETE
 * android.net.conn.CONNECTIVITY_CHANGE
 * org.nof1trial.nof1.DOWNLOAD_SCHEDULE
 * org.nof1trial.nof1.MAKE_FILE
 * 
 * @author John Lawson
 * 
 */
public class FinishedService extends IntentService {

	private static final String TAG = "FinishedService";
	private static final boolean DEBUG = BuildConfig.DEBUG;
	public static final String CVS_FILE = "results.csv";

	/** Current context */
	private final Context mContext = this;

	public FinishedService() {
		this("FinishedService");
	}

	public FinishedService(String name) {
		super(name);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DEBUG) Log.d(TAG, "New finished service started");
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressLint("WorldReadableFiles")
	@TargetApi(8)
	private boolean createCVS(Cursor cursor) {
		/*
		 * We want to write the schedule file to external storage, as that way
		 * we know the email app will be able to
		 * find it.
		 * However if it cannot, use internal storage and a world readable file.
		 * This may or may not work depending on
		 * the email client used.
		 */
		// Check state of external storage
		boolean storageWriteable = false;
		boolean storageInternal = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			storageWriteable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			storageWriteable = false;
		}
		File dir = null;

		if (storageWriteable && Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			// Eclair has no support for getExternalCacheDir()
			// Save file to /Android/data/org.nof1trial.nof1/cache/
			File temp = Environment.getExternalStorageDirectory();

			dir = new File(temp.getAbsoluteFile() + "/Android/data/org.nof1trial.nof1/files");

		} else if (storageWriteable) {

			dir = getExternalFilesDir(null);

		} else {
			// Toast.makeText(this,
			// "No external storage found. Using internal storage which may not work.",
			// Toast.LENGTH_LONG).show();
			storageInternal = true;
			dir = getFilesDir();
		}
		File file = new File(dir, CVS_FILE);
		if (DEBUG) Log.d(TAG, "Saving csv to file: " + file.getAbsolutePath());

		if (storageInternal) {
			try {
				// File should be world readable so that GMail (or whatever the
				// user uses) can read it
				FileOutputStream fos = openFileOutput(CVS_FILE, MODE_WORLD_READABLE);
				fos.write(getCVSString(cursor).getBytes());
				fos.close();

			} catch (IOException e) {
				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
				return false;
			}

		} else {
			try {
				// Write file to external storage
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(getCVSString(cursor));
				writer.close();

			} catch (IOException e) {
				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		if (DEBUG) Log.d(TAG, "csv file saved to disk");
		return true;

	}

	private String getCVSString(Cursor cursor) {
		StringBuilder sb = new StringBuilder();

		cursor.moveToFirst();
		String[] headers = cursor.getColumnNames();
		int size = headers.length;

		// Add column headers
		for (int i = 0; i < size; i++) {
			sb.append(headers[i]).append(", ");
		}
		sb.append("\n");

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

		// Add data
		while (!cursor.isAfterLast()) {
			for (int i = 0; i < size; i++) {
				if (i == 2) {
					// Want to convert time to readable format
					sb.append(df.format(new Date(cursor.getLong(i)))).append(", ");

				} else {
					sb.append(cursor.getString(i)).append(", ");
				}
			}
			sb.append("\n");

			cursor.moveToNext();
		}
		if (DEBUG) Log.d(TAG, "csv string built");
		return sb.toString();

	}

	/**
	 * Try to download the config file for this patient from server. Saves
	 * schedule to the supplied shared prefs.
	 * 
	 * Internet connection should be checked before running this.
	 * 
	 * @param sp
	 */
	private void downloadSchedule(final SharedPreferences sp) {
		if (DEBUG) Log.d(TAG, "Trying to download schedule");

		// Download config file and save schedule data to prefs
		MyRequestFactory factory = Util.getRequestFactory(FinishedService.this, MyRequestFactory.class);
		ConfigRequest request = factory.configRequest();

		request.findAllConfigs().fire(new Receiver<List<ConfigProxy>>() {

			@Override
			public void onSuccess(List<ConfigProxy> list) {
				if (list.size() == 0) {
					Log.e(TAG, "No config file found on server. Please contact the pharmacist for treatment schedule.");

				} else {
					// get most recent config. Generally I would only expect a
					// patient to have one.
					ConfigProxy conf = list.get(list.size() - 1);
					// Save schedule to prefs
					sp.edit().putString(Keys.CONFIG_SCHEDULE, conf.getSchedule()).commit();
					if (DEBUG) Log.d(TAG, "Saved schedule: " + conf.getSchedule());
				}

			}

			@Override
			public void onFailure(ServerFailure error) {
				// TODO retry download
			}

		});
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (Keys.ACTION_COMPLETE.equals(intent.getAction())) {

			DataSource data = new DataSource(FinishedService.this);
			data.open();

			Cursor cursor = data.getAllColumns();

			createCVS(cursor);

			cursor.close();
			data.close();

			// Cancel medicine alarms
			final SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			for (int i = 0; sp.contains(Keys.CONFIG_TIME + i); i++) {

				Intent receiver = new Intent(FinishedService.this, Receiver.class);
				receiver.putExtra(Keys.INTENT_MEDICINE, true);

				// Make sure each medicine notification gets a different request
				// id
				PendingIntent pi = PendingIntent.getBroadcast(FinishedService.this, 1 + i, receiver, PendingIntent.FLAG_CANCEL_CURRENT);

				alarmManager.cancel(pi);
			}

			// If no internet, set flag to download and register broadcast
			// receiver
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());

			if (isConnected) {

				downloadSchedule(sp);

			} else {
				if (DEBUG) Log.d(TAG, "Not connected to internet, starting network change listener");
				// enable network change broadcast receiver
				PackageManager pm = getPackageManager();
				ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
				pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			}

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			// Should now have internet
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());

			if (isConnected) {
				SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

				if (!sp.contains(Keys.CONFIG_SCHEDULE)) {
					// Try to download
					downloadSchedule(sp);
				}

				PackageManager pm = getPackageManager();
				ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
				pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

			} else {
				if (DEBUG) Log.e(TAG, "Got Connectivity changed, but not connected");
			}

		} else if (Keys.ACTION_DOWNLOAD_SCHEDULE.equals(intent.getAction())) {

			// If no internet, set flag to download and register broadcast
			// receiver
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());

			if (isConnected) {

				SharedPreferences prefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
				if (prefs.getBoolean(Keys.SCHED_FINISHED, false)) {

					if (DEBUG) Log.d(TAG, "Trying to download schedule");

					final SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

					// Download config file and save schedule data to prefs
					MyRequestFactory factory = Util.getRequestFactory(FinishedService.this, MyRequestFactory.class);
					ConfigRequest request = factory.configRequest();

					request.findAllConfigs().fire(new Receiver<List<ConfigProxy>>() {

						@Override
						public void onSuccess(List<ConfigProxy> list) {
							if (list.size() == 0) {
								Log.e(TAG, "No config file found on server. Please contact the pharmacist for treatment schedule.");

							} else {
								// get most recent config. Generally I would
								// only expect a patient to have one.
								ConfigProxy conf = list.get(list.size() - 1);
								// Save schedule to prefs
								sp.edit().putString(Keys.CONFIG_SCHEDULE, conf.getSchedule()).commit();
								if (DEBUG) Log.d(TAG, "Saved schedule: " + conf.getSchedule());

								// Send local broadcast to say the download is
								// complete
								Intent broadcast = new Intent(Keys.ACTION_DOWNLOAD_SCHEDULE);
								LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
								manager.sendBroadcast(broadcast);

							}

						}

						@Override
						public void onFailure(ServerFailure error) {
							super.onFailure(error);
							// Broadcast error
							Intent broadcast = new Intent(Keys.ACTION_ERROR);
							LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
							manager.sendBroadcast(broadcast);
						}

					});
				}

			} else {
				if (DEBUG) Log.d(TAG, "Not connected to internet, starting network change listener");
				// enable network change broadcast receiver
				PackageManager pm = getPackageManager();
				ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
				pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			}

		} else if (Keys.ACTION_MAKE_FILE.equals(intent.getAction())) {
			// Make the csv file
			DataSource data = new DataSource(FinishedService.this);
			data.open();

			Cursor cursor = data.getAllColumns();

			boolean done = createCVS(cursor);

			cursor.close();
			data.close();

			if (done) {

				// Send local broadcast to say the download is complete
				Intent broadcast = new Intent(Keys.ACTION_MAKE_FILE);
				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
				manager.sendBroadcast(broadcast);

			} else {
				// Send local broadcast to say the download is complete
				Intent broadcast = new Intent(Keys.ACTION_ERROR);
				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
				manager.sendBroadcast(broadcast);
			}

		} else {
			Log.w(TAG, "Intent Service started with unrecognisd action");
		}
	}
}
