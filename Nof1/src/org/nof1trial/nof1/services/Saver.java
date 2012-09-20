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

import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.DataSource;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.NetworkChangeReceiver;
import org.nof1trial.nof1.SQLite;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.containers.ConfigData;
import org.nof1trial.nof1.containers.ConfigData.Factory;
import org.nof1trial.nof1.shared.ConfigProxy;
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
public class Saver extends IntentService implements ConfigData.OnConfigRequestListener {

	private static final String TAG = "Saver";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	/** Shared Preferences file for storing cached data to be sent to server */
	private static final String CACHE = "config";
	private static final String NUM_DATA = "num_data_cache";
	private static final String BOOL_CONFIG = "bool_config_cache";

	private final Context mContext = this;

	public Saver() {
		super("Saver");
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
			if (DEBUG) Log.d(TAG, "Saving config to disk");
			saveConfig(intent);

		} else if (Keys.ACTION_SAVE_DATA.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Saving data to disk");
			saveData(intent);

		} else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Checking for data to upload");

			if (isConnected()) {
				SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);

				if (sp.getInt(NUM_DATA, 0) > 0) {
					uploadAllCachedData(sp);
				}
				if (sp.getBoolean(BOOL_CONFIG, false)) {
					uploadConfigFromPrefs();
				}

			} else {
				enableNetworkChangeReceiver();
			}

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

			if (isConnected()) {
				if (DEBUG) Log.d(TAG, "Uploading previously saved data");
				boolean uploadedData = false;
				SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);
				if (sp.getInt(NUM_DATA, 0) > 0) {
					if (DEBUG) Log.d(TAG, "Have data to upload");
					uploadAllCachedData(sp);
					uploadedData = true;
				}
				if (sp.getBoolean(BOOL_CONFIG, false)) {
					if (DEBUG) Log.d(TAG, "Have config to send");
					uploadConfigFromPrefs();
					uploadedData = true;
				}
				if (uploadedData) {
					disableNetworkChangeReceiver();
				}
			}

		} else if (Keys.ACTION_UPLOAD_ALL.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Uploading all saved data");

			uploadConfigFromPrefs();

			if (isDatabaseInitialised()) {
				uploadAllDataFromDatabase();
			}
		} else {
			Log.w(TAG, "IntentService started with unrecognised action");
		}

	}

	private void uploadAllDataFromDatabase() {
		DataSource datasource = new DataSource(mContext);
		datasource.open();
		Cursor cursor = datasource.getAllColumns();
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			uploadDataFromCursor(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		datasource.close();
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

		saveDataToDatabase(day, time, comment, data);

		if (isConnected()) {
			if (DEBUG) Log.d(TAG, "Connected to internet, sending data to server");
			uploadData(day, time, data, comment);
		} else {
			saveDataForLater(day, time, comment, data);
			enableNetworkChangeReceiver();
		}
	}

	private void saveDataToDatabase(int day, long time, String comment, int[] data) {
		DataSource source = new DataSource(this);
		source.open();
		source.saveData(day, time, data, comment);
		source.close();
		backup();
	}

	/**
	 * Upload data to server. Should check whether internet is available before trying to call this.
	 */
	private void uploadData(final int day, final long time, final int[] data, final String comment) {
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

	private void saveConfig(Intent intent) {

		ConfigData.Factory factory = new Factory(this);
		ConfigData config = factory.generateFromIntent(intent);

		SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
		config.saveToPrefs(prefs);

		if (isConnected()) {
			config.upload(mContext);

		} else {
			// No internet, so set flag to upload later
			prefs.edit().putBoolean(BOOL_CONFIG, true).commit();

			enableNetworkChangeReceiver();
		}
	}

	private void uploadAllCachedData(SharedPreferences cachePrefs) {
		for (int count = cachePrefs.getInt(NUM_DATA, 0); count > 0; count--) {
			uploadCachedDataAt(count, cachePrefs);
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

		clearDataCache(id, cachPrefs);

		uploadData(day, time, data, comment);
	}

	private void clearDataCache(int id, SharedPreferences cachePrefs) {
		// Remove data from prefs
		SharedPreferences.Editor editor = cachePrefs.edit();
		editor.putString(Keys.DATA_DAY + id, null);
		editor.putString(Keys.DATA_TIME + id, null);
		editor.putString(Keys.DATA_COMMENT + id, null);
		editor.putString(Keys.DATA_LIST + id, null);

		// decrement counter in prefs
		int count = cachePrefs.getInt(NUM_DATA, 1);
		editor.putInt(NUM_DATA, count - 1).commit();
	}

	private void uploadConfigFromPrefs() {
		SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
		SharedPreferences ques = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

		ConfigData.Factory factory = new Factory(this);
		ConfigData config = factory.generateFromPrefs(prefs, ques);

		// remove flag in prefs
		SharedPreferences sp = getSharedPreferences(CACHE, MODE_PRIVATE);
		sp.edit().putBoolean(BOOL_CONFIG, false).commit();

		config.upload(mContext);
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

	private boolean isDatabaseInitialised() {
		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
		boolean result = sp.contains(Keys.QUES_NUMBER_QUESTIONS) && sp.getInt(Keys.QUES_NUMBER_QUESTIONS, 0) > 0;
		return result;
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

	@Override
	public void onConfigUploadSuccess(ConfigProxy conf) {
		if (DEBUG) Log.d(TAG, "Data saved successfully");
	}

	@Override
	public void onConfigUploadFailure(ServerFailure failure) {
		Log.e(TAG, "Config not saved");
		Log.e(TAG, failure.getMessage());

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
}
