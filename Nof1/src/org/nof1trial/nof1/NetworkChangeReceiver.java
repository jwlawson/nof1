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

import org.nof1trial.nof1.services.FinishedService;
import org.nof1trial.nof1.services.Saver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Broadcast receiver that is disabled by default. Will be enabled if there is
 * data to upload, so the app can wait until
 * there is an internet connection, then upload data.
 * 
 * @author John Lawson
 * 
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

	private static final String TAG = "NetworkChangeReceiver";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	@Override
	public void onReceive(Context context, Intent intent) {

		if (DEBUG) Log.d(TAG, "Context: " + context.getPackageName());

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Network connection changed");

			boolean isConnected = isConnected(context);

			if (DEBUG) Log.d(TAG, "Internet connected: " + isConnected);

			if (isConnected) {

				startService(context, Saver.class);
				if (DEBUG) Log.d(TAG, "Saver started");

				if (needToDownloadSchedule(context)) {
					startService(context, FinishedService.class);
				}
			}
		}
	}

	private <T extends Service> void startService(Context context, Class<T> serviceClass) {
		Intent saver = new Intent(context, serviceClass);
		saver.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.startService(saver);
	}

	private boolean needToDownloadSchedule(Context context) {
		SharedPreferences sp = context.getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE);
		return sp.getBoolean(Keys.SCHED_FINISHED, false) && !sp.contains(Keys.CONFIG_SCHEDULE);
	}

	private boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork == null ? false : activeNetwork.isConnectedOrConnecting());
	}

}
