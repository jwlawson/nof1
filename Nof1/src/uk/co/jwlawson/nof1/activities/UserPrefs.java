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
package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.Scheduler;
import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Shows the user preferences, which are accessible with getDefaultPreferences(Context)
 * Also provides access to doctor config.
 * 
 * @author John Lawson
 * 
 */
public class UserPrefs extends SherlockPreferenceActivity {

	private static final String TAG = "User Prefs";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	public UserPrefs() {
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.user_preferences);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			if (DEBUG) Log.d(TAG, "Pre-Froyo phone, removing backup option");
			Preference backup = findPreference(Keys.DEFAULT_BACKUP);

			((PreferenceCategory) findPreference("category_general")).removePreference(backup);
		}

		if (DEBUG) Log.d(TAG, "Preferences loaded");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());

		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@TargetApi(8)
	@Override
	protected void onDestroy() {
		super.onDestroy();

		SharedPreferences sp = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);

		if (sp.getBoolean(Keys.DEFAULT_BACKUP, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			BackupManager bm = new BackupManager(this);
			bm.dataChanged();
			if (DEBUG) Log.d(TAG, "Requesting backup");
		}

		// Tell Scheduler to redo alarms, as time could have changed
		Intent intent = new Intent(this, Scheduler.class);
		intent.putExtra(Keys.INTENT_BOOT, true);
		startService(intent);
	}
}
