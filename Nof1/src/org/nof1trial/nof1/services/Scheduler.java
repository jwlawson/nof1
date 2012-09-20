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

import java.util.Calendar;

import org.nof1trial.nof1.AlarmReceiver;
import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.preferences.TimePreference;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Handles scheduling next notification.
 * 
 * @author John Lawson
 * 
 */
public class Scheduler extends IntentService {

	private static final String TAG = "Scheduler";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	private static final int REQUEST_QUES = 0;

	private AlarmManager mAlarmManager;

	private BackupManager mBackupManager;

	public Scheduler() {
		super("Scheduler");
	}

	@TargetApi(8)
	@Override
	public void onCreate() {
		super.onCreate();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager = new BackupManager(this);
		}

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getBooleanExtra(Keys.INTENT_BOOT, false)) {
			if (DEBUG) Log.d(TAG, "Scheduler started after boot");

			bootSetup();

		} else if (intent.getBooleanExtra(Keys.INTENT_ALARM, false)) {
			if (DEBUG) Log.d(TAG, "Scheduler started to schedule new alarm");

			setNextAlarm();

			if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
				// If first is sent as well, then need to set up medicine
				// reminders
				setMedicineAlarm();
			}

		} else if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
			if (DEBUG) Log.d(TAG, "Scheduler run for the first time");

			String startDate = intent.getStringExtra(Keys.CONFIG_START);
			firstRun(startDate);

		} else if (intent.hasExtra(Keys.INTENT_RESCHEDULE)) {
			if (DEBUG) Log.d(TAG, "Rescheduling alarm");
			final int mins = intent.getIntExtra(Keys.INTENT_RESCHEDULE, 0);

			reschedule(mins);

		} else {
			Log.w(TAG, "IntentService started with unrecognised action");
		}
	}

	private void bootSetup() {
		// Get the next alarm time from preferences and set it
		setRepeatAlarm();
		setMedicineAlarm();
	}

	/** Set alarm for the start date of trial */
	private void firstRun(String startDate) {

		SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
		SharedPreferences.Editor schedEdit = schedPrefs.edit();

		// Load start date as next time for notification
		if (startDate == null) {
			Log.e(TAG, "Start date not initialised, config needs to be run");
		} else {
			schedEdit.putString(Keys.SCHED_NEXT_DATE, startDate);
			schedEdit.putInt(Keys.SCHED_NEXT_DAY, 1);
			schedEdit.putInt(Keys.SCHED_CUR_PERIOD, 1);

			schedEdit.commit();
			backup();

			// Set up first time run notification
			setFirstAlarm(startDate);
		}
	}

	private void setNextAlarm() {
		// Work out when next to set the alarm, set it and save in
		// preferences
		SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
		SharedPreferences.Editor schedEdit = schedPrefs.edit();

		SharedPreferences configPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

		int lastDay = schedPrefs.getInt(Keys.SCHED_NEXT_DAY, 1);
		int periodLength = configPrefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 1);
		int nextDay = -1;
		int add;
		// TODO on the very first day, could have add = 0
		for (add = 1; add <= periodLength; add++) {
			nextDay = (lastDay + add) % (periodLength);
			// Because of modulo, when the nextday would be the last in
			// period nextDay is set to zero
			if (nextDay == 0) nextDay = periodLength;

			if (configPrefs.getBoolean(Keys.CONFIG_DAY + nextDay, false)) {
				if (DEBUG) Log.d(TAG, "Found the next day to send notification: " + nextDay);
				break;
			}
		}
		if (nextDay < 0) {
			throw new RuntimeException("Invalid config settings");
		}
		int period = schedPrefs.getInt(Keys.SCHED_CUR_PERIOD, 1);
		if (nextDay < lastDay) {
			// Moving into next treatment period
			if (period + 1 > 2 * configPrefs.getInt(Keys.CONFIG_NUMBER_PERIODS, Integer.MAX_VALUE)) {
				// Finished trial
				schedEdit.putBoolean(Keys.SCHED_FINISHED, true);
			}
			schedEdit.putInt(Keys.SCHED_CUR_PERIOD, period + 1);
		}
		schedEdit.putInt(Keys.SCHED_LAST_PERIOD, period);

		// Save next day to set alarm
		schedEdit.putInt(Keys.SCHED_NEXT_DAY, nextDay);
		schedEdit.putInt(Keys.SCHED_LAST_DAY, lastDay);

		// Get the new date to set the alarm on
		String lastDate = schedPrefs.getString(Keys.SCHED_NEXT_DATE, null);
		Calendar cal = getCalendarFromString(lastDate);
		cal.add(Calendar.DAY_OF_MONTH, add);

		String nextDate = getDateStringFromCalendar(cal);

		schedEdit.putString(Keys.SCHED_NEXT_DATE, nextDate);
		schedEdit.putString(Keys.SCHED_LAST_DATE, lastDate);

		// Increment cumulative day counter
		int cumDay = schedPrefs.getInt(Keys.SCHED_NEXT_CUMULATIVE_DAY, 1);
		schedEdit.putInt(Keys.SCHED_NEXT_CUMULATIVE_DAY, cumDay + add);
		schedEdit.putInt(Keys.SCHED_CUMULATIVE_DAY, cumDay);

		schedEdit.commit();
		backup();

		// Finally, use the new values to set an alarm
		setRepeatAlarm();
	}

	private void reschedule(int mins) {
		// Work out when next to set the alarm, set it and save in preferences
		SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
		SharedPreferences.Editor schedEdit = schedPrefs.edit();

		// Roll back settings in preferences
		schedEdit.putInt(Keys.SCHED_NEXT_DAY, schedPrefs.getInt(Keys.SCHED_LAST_DAY, 1));
		schedEdit.putInt(Keys.SCHED_CUR_PERIOD, schedPrefs.getInt(Keys.SCHED_LAST_PERIOD, 1));
		schedEdit.putString(Keys.SCHED_NEXT_DATE, schedPrefs.getString(Keys.SCHED_LAST_DATE, null));
		schedEdit.putInt(Keys.SCHED_NEXT_CUMULATIVE_DAY,
				schedPrefs.getInt(Keys.SCHED_CUMULATIVE_DAY, 1));

		schedEdit.commit();
		backup();

		// Get calendar for this time + mins
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, mins);

		// Finally, use the new values to set an alarm
		Intent intent = new Intent(Scheduler.this, AlarmReceiver.class);
		intent.putExtra(Keys.INTENT_ALARM, true);
		setAlarm(intent, cal);

	}

	/** Load first date to set alarm from preferences and set alarm for then */
	private void setFirstAlarm(String startDate) {
		Intent intent = new Intent(Scheduler.this, AlarmReceiver.class);
		intent.putExtra(Keys.INTENT_FIRST, true);

		SharedPreferences config = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
		String time = getEarliestMedicineTime(config);
		if (DEBUG) Log.d(TAG, "Scheduling first time notification at " + startDate + " " + time);

		Calendar start = getCalendarFromString(startDate, time);
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);

		// Use yesterday, so that this will run if the trial starts today
		if (start.after(yesterday)) {
			setAlarm(intent, start);
		}
	}

	// TODO This really needs reworking to be less of a mess
	private String getEarliestMedicineTime(SharedPreferences config) {
		int hour = 12;
		int min = 00;

		for (int i = 0; config.contains(Keys.CONFIG_TIME + i); i++) {
			String time = config.getString(Keys.CONFIG_TIME + i, "12:00");
			String[] timeArr = time.split(":");
			int timeHour = Integer.parseInt(timeArr[0]);
			int timeMin = Integer.parseInt(timeArr[1]);
			if (timeHour < hour) {
				hour = timeHour;
				min = timeMin;
			} else if (timeHour == hour && timeMin < min) {
				min = timeMin;
			}
		}
		String time = hour + ":" + min;
		return time;
	}

	/** Load next date to set alarm from preferences and set alarm for then */
	private void setRepeatAlarm() {
		Intent intent = new Intent(Scheduler.this, AlarmReceiver.class);
		intent.putExtra(Keys.INTENT_ALARM, true);

		setAlarmFromPrefs(intent);
	}

	private void setMedicineAlarm() {
		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);

		Calendar now = Calendar.getInstance();

		// Only want alarms to start after the trial has started
		String startStr = sp.getString(Keys.CONFIG_START, "");
		if (startStr == "") {
			return;
		}

		Calendar startCal = getCalendarFromString(startStr);
		startCal.set(Calendar.HOUR, 0);
		startCal.set(Calendar.MINUTE, 0);

		if (now.before(startCal)) {
			now.setTimeInMillis(startCal.getTimeInMillis());
		}

		for (int i = 0; sp.contains(Keys.CONFIG_TIME + i); i++) {

			Calendar alarmCal = Calendar.getInstance();
			String time = sp.getString(Keys.CONFIG_TIME + i, "12:00");
			int hour = TimePreference.getHour(time);
			int min = TimePreference.getMinute(time);

			alarmCal.set(Calendar.HOUR_OF_DAY, hour);
			alarmCal.set(Calendar.MINUTE, min);

			while (alarmCal.before(now)) {
				// If we would be setting a notification in the past, add an
				// extra day to ensure it is only called in the future
				alarmCal.add(Calendar.DAY_OF_MONTH, 1);
			}

			// Only set medicine alarms when the trial is running
			if (!sp.getBoolean(Keys.SCHED_FINISHED, false)) {

				Intent intent = new Intent(this, AlarmReceiver.class);
				intent.putExtra(Keys.INTENT_MEDICINE, true);

				// Make sure each medicine notification gets a different request
				// id
				setAlarm(intent, alarmCal);
				if (DEBUG) Log.d(TAG, "Scheduling a repeating medicine alarm at " + time);
			}
		}
	}

	/**
	 * Sets an alarm for time saved in prefs which fires off the supplied intent
	 */
	private void setAlarmFromPrefs(Intent intent) {
		SharedPreferences sp = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);

		SharedPreferences userPrefs = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);

		String dateStr = sp.getString(Keys.SCHED_NEXT_DATE, null);
		String timeStr = userPrefs.getString(Keys.DEFAULT_TIME, "12:00");
		if (dateStr == null) {
			Log.e(TAG, "Config not yet run");
			return;
		}
		// Only set alarm if the trial has not finished
		if (!sp.getBoolean(Keys.SCHED_FINISHED, false)) {
			setAlarm(intent, dateStr, timeStr);
		}
	}

	/** Set an alarm to fire off specified intent at time stored in calendar */
	private void setAlarm(Intent intent, Calendar cal) {
		PendingIntent pi = PendingIntent.getBroadcast(Scheduler.this, REQUEST_QUES, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}

	/**
	 * Set an alarm at specified date and time.
	 * 
	 * @param intent
	 * @param dateStr
	 *            DD:MM:YYYY
	 * @param timeStr
	 *            HH:MM
	 */
	private void setAlarm(Intent intent, String dateStr, String timeStr) {

		Calendar cal = getCalendarFromString(dateStr, timeStr);

		setAlarm(intent, cal);
	}

	private Calendar getCalendarFromString(String date) {
		String[] lastArr = date.split(":");
		int[] lastInt = new int[] { Integer.parseInt(lastArr[0]), Integer.parseInt(lastArr[1]),
				Integer.parseInt(lastArr[2]) };
		Calendar cal = Calendar.getInstance();
		cal.set(lastInt[2], lastInt[1], lastInt[0]);
		return cal;
	}

	private Calendar getCalendarFromString(String dateStr, String timeStr) {
		String[] dateArr = dateStr.split(":");
		String[] timeArr = timeStr.split(":");

		int[] dateInt = new int[] { Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]),
				Integer.parseInt(dateArr[2]) };
		int[] timeInt = new int[] { Integer.parseInt(timeArr[0]), Integer.parseInt(timeArr[1]) };

		Calendar cal = Calendar.getInstance();
		cal.set(dateInt[2], dateInt[1], dateInt[0], timeInt[0], timeInt[1]);
		return cal;
	}

	private String getDateStringFromCalendar(Calendar cal) {
		StringBuilder sb = new StringBuilder();
		sb.append(cal.get(Calendar.DAY_OF_MONTH)).append(":");
		sb.append(cal.get(Calendar.MONTH)).append(":");
		sb.append(cal.get(Calendar.YEAR));

		String nextDate = sb.toString();
		return nextDate;
	}

	@TargetApi(8)
	private void backup() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager.dataChanged();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
