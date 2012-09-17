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

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.services.Scheduler;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Shows the user preferences, which are accessible with getDefaultPreferences(Context) Also provides access to doctor
 * config.
 * 
 * @author John Lawson
 * 
 */
public class UserPrefs extends SherlockPreferenceActivity {

	private static final String TAG = "User Prefs";
	private static final boolean DEBUG = false;

	private final Context mContext = this;

	public UserPrefs() {
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		PreferenceManager.setDefaultValues(mContext, R.xml.user_preferences, false);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.user_preferences);

		if (DEBUG) Log.d(TAG, "Preferences loaded");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());

		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = new Intent(this, HomeScreen.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
				finish();
			} else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@TargetApi(8)
	@Override
	protected void onPause() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			BackupManager bm = new BackupManager(this);
			bm.dataChanged();
			if (DEBUG) Log.d(TAG, "Requesting backup");
		}

		// Tell Scheduler to redo alarms, as time could have changed
		Intent intent = new Intent(this, Scheduler.class);
		intent.putExtra(Keys.INTENT_BOOT, true);
		startService(intent);
		super.onPause();
	}
}
