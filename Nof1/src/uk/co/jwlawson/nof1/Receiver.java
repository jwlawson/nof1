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

import uk.co.jwlawson.nof1.activities.Questionnaire;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Gets the Boot_complete broadcast and starts up Scheduler service
 * 
 * @author John Lawson
 * 
 */
public class Receiver extends BroadcastReceiver {

	private static final String TAG = "Receiver";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	/**
	 * 
	 */
	public Receiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		// intent.getAction() could be null
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			if (DEBUG) Log.d(TAG, "Boot_complete broadcast caught");

			Intent i = new Intent(context, Scheduler.class);
			i.putExtra(Keys.INTENT_BOOT, true);
			context.startService(i);

		} else if (intent.getBooleanExtra(Keys.INTENT_ALRAM, false)) {
			if (DEBUG) Log.d(TAG, "AlarmManager alarm caught");

			// Show notification
			setNotification(context);

			// Pass to scheduler
			Intent i = new Intent(context, Scheduler.class);
			i.putExtra(Keys.INTENT_BOOT, false);
			i.putExtra(Keys.INTENT_ALRAM, true);
			context.startService(i);

		} else if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
			if (DEBUG) Log.d(TAG, "First time run alarm caught");

			// Show first run notification
			setFirstNotification(context);

			// Pass to scheduler
			Intent i = new Intent(context, Scheduler.class);
			i.putExtra(Keys.INTENT_BOOT, false);
			i.putExtra(Keys.INTENT_ALRAM, true);
			context.startService(i);
		}
	}

	private void setNotification(Context context) {

		SharedPreferences sp = context.getSharedPreferences(Keys.DEFAULT_PREFS, Context.MODE_PRIVATE);

		Intent intent = new Intent(context, Questionnaire.class);
		intent.putExtra(Keys.INTENT_PREVIEW, false);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentIntent(pi).setContentTitle("N-of-1 Trials").setContentText("Please fill in the questionnnaire").setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_noti);

		if (sp.getBoolean(Keys.DEFAULT_LOUD, false)) {
			// Notification makes noise
			builder.setDefaults(Notification.DEFAULT_SOUND);
		}

		if (sp.getBoolean(Keys.DEFAULT_FLASH, false)) {
			// Notification to flash
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
		}

		if (sp.getBoolean(Keys.DEFAULT_VIBE, false)) {
			// Notification to vibrate
			builder.setDefaults(Notification.DEFAULT_VIBRATE);
		}

		Notification noti = builder.getNotification();

		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0x100, noti);

		if (DEBUG) Log.d(TAG, "Notification posted");

	}

	private void setFirstNotification(Context context) {

		SharedPreferences sp = context.getSharedPreferences(Keys.DEFAULT_PREFS, Context.MODE_PRIVATE);

		// TODO Load home screen
		Intent intent = new Intent(context, Questionnaire.class);
		intent.putExtra(Keys.INTENT_PREVIEW, false);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentIntent(pi).setContentTitle("N-of-1 Trials").setContentText("Your trial is due to start today").setAutoCancel(true);

		if (sp.getBoolean(Keys.DEFAULT_LOUD, false)) {
			// Notification makes noise
			builder.setDefaults(Notification.DEFAULT_SOUND);
		}

		if (sp.getBoolean(Keys.DEFAULT_FLASH, false)) {
			// Notification to flash
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
		}

		if (sp.getBoolean(Keys.DEFAULT_VIBE, false)) {
			// Notification to vibrate
			builder.setDefaults(Notification.DEFAULT_VIBRATE);
		}

		Notification noti = builder.getNotification();

		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0x100, noti);

		if (DEBUG) Log.d(TAG, "Notification posted");

	}

}
