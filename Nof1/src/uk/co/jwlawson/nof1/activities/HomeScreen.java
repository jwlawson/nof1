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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * The main home screen that users see when they open the app. On first run will also set up the task stack to allow
 * doctors to input data, then patients preferences, then back to this screen.
 * 
 * @author John Lawson
 * 
 */
public class HomeScreen extends SherlockActivity {

	private static final String TAG = "HomeScreen";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(false);

		SharedPreferences sp = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);

		if (!sp.contains(Keys.DEFAULT_FIRST)) {
			if (DEBUG) Log.d(TAG, "App launched for the first time");

			TaskStackBuilder builder = TaskStackBuilder.create(this);

			builder.addNextIntent(new Intent(this, HomeScreen.class));
			builder.addNextIntent(new Intent(this, UserPrefs.class));
			builder.addNextIntent(new Intent(this, DoctorLogin.class));

			builder.startActivities();

		} else {
			// Not the first time app is run

			setContentView(R.layout.home_layout);

			Button btnData = (Button) findViewById(R.id.home_btn_data);
			btnData.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Launch questionnaire
					Intent intent = new Intent(HomeScreen.this, Questionnaire.class);
					startActivity(intent);
				}
			});

			SharedPreferences quesPrefs = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

			Button btnGraphs = (Button) findViewById(R.id.home_btn_graph);

			if (quesPrefs.contains(Keys.QUES_TEXT + 0)) {
				// Enable viewing graphs, as questionnaire built

				btnGraphs.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Launch graph activity
						Intent intent = new Intent(HomeScreen.this, GraphChooser.class);
						startActivity(intent);
					}
				});
			} else {
				// Questionnaire not made, so don't want to create empty database
				btnGraphs.setEnabled(false);
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());

		switch (item.getItemId()) {

		case R.id.menu_home_settings:
			Intent intent = new Intent(this, UserPrefs.class);
			startActivity(intent);
			return true;

		case R.id.menu_home_about:
			Intent intent1 = new Intent(this, About.class);
			startActivity(intent1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
