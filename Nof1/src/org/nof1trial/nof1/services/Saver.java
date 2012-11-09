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

import java.util.List;

import org.acra.ACRA;
import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.DataSource;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.NetworkChangeReceiver;
import org.nof1trial.nof1.containers.ConfigData;
import org.nof1trial.nof1.containers.ConfigData.Factory;
import org.nof1trial.nof1.containers.Data;
import org.nof1trial.nof1.containers.DataUploadQueue;
import org.nof1trial.nof1.shared.ConfigProxy;

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

import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * @author John Lawson
 * 
 */
public class Saver extends IntentService implements ConfigData.OnConfigRequestListener,
		DataUploadQueue.OnDataUploadedListener {

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
					uploadAllCachedData();
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
					uploadAllCachedData();
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
		DataUploadQueue queue = new DataUploadQueue(this);

		Data.Factory factory = new Data.Factory(queue, mContext);

		DataSource datasource = new DataSource(mContext);
		datasource.open();
		Cursor cursor = datasource.getAllColumns();
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			Data data = factory.generateFromCursor(cursor);
			queue.addData(data);
			cursor.moveToNext();
		}
		cursor.close();
		datasource.close();

		queue.start();
	}

	private void saveData(Intent intent) {
		DataUploadQueue queue = new DataUploadQueue(this);

		Data.Factory factory = new Data.Factory(queue, mContext);
		Data data = factory.generateFromIntent(intent);

		data.save();
		backup();

		if (isConnected()) {
			if (DEBUG) Log.d(TAG, "Connected to internet, sending data to server");
			queue.addData(data);
			queue.start();
		} else {
			data.cache();
			enableNetworkChangeReceiver();
		}
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

	private void uploadAllCachedData() {
		DataUploadQueue queue = new DataUploadQueue(this);
		Data.Factory factory = new Data.Factory(queue, mContext);
		List<Data> dataList = factory.generateCachedDataList();

		for (Data data : dataList) {
			queue.addData(data);
		}
		Data.clearDataCache(mContext);
		queue.start();
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
		pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	private void disableNetworkChangeReceiver() {
		PackageManager pm = getPackageManager();
		ComponentName comp = new ComponentName(this, NetworkChangeReceiver.class);
		pm.setComponentEnabledSetting(comp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork == null ? false : activeNetwork.isConnected());
	}

	private boolean isDatabaseInitialised() {
		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
		boolean result = sp.contains(Keys.QUES_NUMBER_QUESTIONS)
				&& sp.getInt(Keys.QUES_NUMBER_QUESTIONS, 0) > 0;
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
		if (DEBUG) Log.d(TAG, "Config saved successfully");
	}

	@Override
	public void onConfigUploadFailure(ServerFailure failure) {
		Log.e(TAG, "Config not saved");

		if ("Auth failure".equals(failure.getMessage().trim())) {
			ACRA.getErrorReporter().handleSilentException(new Throwable(failure.getMessage()));
		}

		refreshAuthCookie();

		// Save for later
		getSharedPreferences(CACHE, MODE_PRIVATE).edit().putBoolean(BOOL_CONFIG, true).commit();

		registerCookieReceiver();
	}

	@Override
	public void onDataUploaded(List<Data> successList, List<Data> failedList) {

		if (!failedList.isEmpty()) {
			// TODO Only refresh cookie when error is an auth error
			// TODO Only allow looping a certain number of times
			refreshAuthCookie();

			for (Data data : failedList) {
				data.cache();
			}

			registerCookieReceiver();

		}
	}

	private void registerCookieReceiver() {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
		manager.registerReceiver(new CookieReceiver(), new IntentFilter(Keys.ACTION_COMPLETE));
	}

	private void refreshAuthCookie() {
		Intent intent = new Intent(mContext, AccountService.class);
		intent.setAction(Keys.ACTION_REFRESH);
		startService(intent);
	}
}
