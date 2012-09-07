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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Broadcast receiver that is disabled by default. Will be enabled if there is data to upload, so the app can wait until
 * there is an internet connection, then upload data.
 * 
 * @author John Lawson
 * 
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

	private static final String TAG = "NetworkChangeReceiver";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	public NetworkChangeReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Network connection changed");

			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnectedOrConnecting());
			if (DEBUG) Log.d(TAG, "Internet connected: " + isConnected);

			if (isConnected) {
				SharedPreferences sp = context.getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE);

				// Pass connectivity change to saver
				Intent saver = new Intent(context, Saver.class);
				saver.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
				context.startService(saver);

				// Check whether should wake up finished service
				if (sp.getBoolean(Keys.SCHED_FINISHED, false) && sp.contains(Keys.CONFIG_SCHEDULE)) {
					Intent finished = new Intent(context, FinishedService.class);
					finished.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
					context.startService(finished);
				}
			}
		}
	}

}
