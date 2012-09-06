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
package org.nof1trial.nof1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Service to save a copy of the results as a .csv file
 * 
 * @author John Lawson
 * 
 */
public class FinishedService extends Service {

	public static final String CVS_FILE = "results.csv";

	public FinishedService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Loader().execute();
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressLint("WorldReadableFiles")
	@TargetApi(8)
	private boolean createCVS(Cursor cursor) {
		/*
		 * We want to write the schedule file to external storage, as that way we know the email app will be able to
		 * find it.
		 * However if it cannot, use internal storage and a world readable file. This may or may not work depending on
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
			// Something else is wrong. It may be one of many other states, but all we need
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
			// Toast.makeText(this, "No external storage found. Using internal storage which may not work.",
			// Toast.LENGTH_LONG).show();
			storageInternal = true;
			dir = getFilesDir();
		}
		File file = new File(dir, CVS_FILE);

		if (storageInternal) {
			try {
				// File should be world readable so that GMail (or whatever the user uses) can read it
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

		DateFormat df = DateFormat.getDateInstance();

		// Add data
		while (!cursor.isAfterLast()) {
			for (int i = 0; i < size; i++) {
				if (i == 2) {
					// Want to convert time to readable format
					sb.append(df.format(new Date(cursor.getLong(i)))).append(", ");
				}
				sb.append(cursor.getString(i)).append(", ");
			}
			sb.append("\n");

			cursor.moveToNext();
		}

		return sb.toString();

	}

	private class Loader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			DataSource data = new DataSource(FinishedService.this);
			data.open();

			Cursor cursor = data.getAllColumns();

			createCVS(cursor);

			cursor.close();
			data.close();

			// Cancel medicine alarms
			SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			for (int i = 0; sp.contains(Keys.CONFIG_TIME + i); i++) {

				Intent intent = new Intent(FinishedService.this, Receiver.class);
				intent.putExtra(Keys.INTENT_MEDICINE, true);

				// Make sure each medicine notification gets a different request id
				PendingIntent pi = PendingIntent.getBroadcast(FinishedService.this, 1 + i, intent, PendingIntent.FLAG_CANCEL_CURRENT);

				alarmManager.cancel(pi);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Stop the service
			stopSelf();
		}

	}

}
