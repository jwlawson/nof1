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
package org.nof1trial.nof1.activities;

import java.util.Calendar;

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.services.Saver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * Activity to allow patient to add a short note on any day.
 * 
 * @author John Lawson
 * 
 */
public class AddNote extends SherlockActivity {

	private static final String TAG = "AddNote";
	private static final boolean DEBUG = false;

	private EditText mNote;

	public AddNote() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_note_layout);

		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mNote = (EditText) findViewById(R.id.add_note_edit);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_add_note, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.d(TAG, "Option selected: " + item.getTitle());

		switch (item.getItemId()) {
		case android.R.id.home:
			// up / home action bar button pressed
			Intent upIntent = new Intent(this, HomeScreen.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
				finish();
			} else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;

		case R.id.menu_add_note_done:
			// Save note
			String note = mNote.getText().toString();
			new DataSaver().execute(note);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class DataSaver extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(String... values) {

			boolean empty = true;
			for (int i = 0; i < values.length; i++) {
				empty = empty && values[i].length() == 0;
			}
			if (empty) {
				if (DEBUG) Log.d(TAG, "Not saving empty note");
				return null;
			}

			// Find the cumulative day for saving data
			SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			String[] start = sp.getString(Keys.CONFIG_START, "").split(":");
			int[] startInt = new int[] { Integer.parseInt(start[0]), Integer.parseInt(start[1]), Integer.parseInt(start[2]) };
			Calendar calStart = Calendar.getInstance();
			calStart.set(startInt[2], startInt[1], startInt[0]);

			Calendar calNow = Calendar.getInstance();
			// Add an hour to ensure that calStart is before calNow when they
			// have the same date
			calNow.add(Calendar.HOUR, 1);
			int day1 = 0;
			while (calStart.before(calNow)) {
				calStart.add(Calendar.DAY_OF_MONTH, 1);
				day1++;
			}
			if (DEBUG) Log.d(TAG, "Data input for day number " + day1);

			for (int i = 0; i < values.length; i++) {
				Intent saver = new Intent(AddNote.this, Saver.class);
				saver.setAction(Keys.ACTION_SAVE_DATA);
				saver.putExtra(Keys.DATA_DAY, day1);
				saver.putExtra(Keys.DATA_TIME, System.currentTimeMillis());
				saver.putExtra(Keys.DATA_COMMENT, values[i]);

				// Offload saving to saver intent service
				startService(saver);

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setSupportProgressBarIndeterminateVisibility(false);

			// Exit after save
			setResult(RESULT_OK);
			finish();
		}

	}

}
