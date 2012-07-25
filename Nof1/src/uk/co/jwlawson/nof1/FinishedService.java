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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Service to
 * 
 * @author John Lawson
 * 
 */
public class FinishedService extends Service {

	private static final String CVS_FILE = "results.csv";

	private File mFile;

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
			// Save file to /Android/data/uk.co.jwlawson.nof1/cache/
			File temp = Environment.getExternalStorageDirectory();

			dir = new File(temp.getAbsoluteFile() + "/Android/data/uk.co.jwlawson.nof1/files");

		} else if (storageWriteable) {

			dir = getExternalFilesDir(null);

		} else {
			// Toast.makeText(this, "No external storage found. Using internal storage which may not work.",
			// Toast.LENGTH_LONG).show();
			storageInternal = true;
			dir = getFilesDir();
		}
		mFile = new File(dir, CVS_FILE);

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
				BufferedWriter writer = new BufferedWriter(new FileWriter(mFile));
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

		// Add data
		while (!cursor.isAfterLast()) {
			for (int i = 0; i < size; i++) {
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

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Send CVS
			super.onPostExecute(result);

			// Stop the service
			stopSelf();
		}

	}

}
