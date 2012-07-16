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
package uk.co.jwlawson.nof1;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Handles scheduling next notification.
 * 
 * @author John Lawson
 * 
 */
public class Scheduler extends Service {

	private static final String TAG = "Scheduler";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	private AlarmManager mAlarmManager;

	/**
	 * 
	 */
	public Scheduler() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		if (DEBUG) Log.d(TAG, "Service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getBooleanExtra(Keys.INTENT_BOOT, false)) {
			if (DEBUG) Log.d(TAG, "Scheduler started after boot");

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					// Do some stuff

					// Close service once done
					Scheduler.this.stopSelf();
				}
			});
			thread.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "Service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
