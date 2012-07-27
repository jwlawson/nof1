/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson, WMG, University of Warwick
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import uk.co.jwlawson.nof1.DataSource;
import uk.co.jwlawson.nof1.FinishedService;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.Scheduler;

import java.util.Random;

public class MainActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSupportActionBar().setHomeButtonEnabled(false);

		Button btnNotification = (Button) findViewById(R.id.main_btn_noti);
		btnNotification.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this, Scheduler.class);
				intent.putExtra(Keys.INTENT_FIRST, true);
				startService(intent);
			}
		});

		Button btnData = (Button) findViewById(R.id.main_btn_data);
		btnData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), Questionnaire.class);
				startActivity(i);
			}
		});

		Button btnGraphs = (Button) findViewById(R.id.main_btn_graphs);
		btnGraphs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), GraphChooser.class);
				startActivity(i);
			}
		});

		Button btnPrefs = (Button) findViewById(R.id.main_btn_settings);
		btnPrefs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), UserPrefs.class);
				startActivity(i);
			}
		});

		Button btnLogin = (Button) findViewById(R.id.main_btn_login);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), DoctorLogin.class);
				startActivity(i);
			}
		});

		Button btnConfig = (Button) findViewById(R.id.main_btn_config);
		btnConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), DoctorConfig.class);
				startActivity(i);
			}
		});

		Button btnBuilder = (Button) findViewById(R.id.main_btn_builder);
		btnBuilder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), FormBuilder.class);
				startActivity(i);
			}
		});

		Button btnAbout = (Button) findViewById(R.id.main_btn_about);
		btnAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), About.class);
				startActivity(i);
			}
		});

		Button btnHome = (Button) findViewById(R.id.main_btn_home);
		btnHome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), HomeScreen.class);
				startActivity(i);
			}
		});

		Button btnPop = (Button) findViewById(R.id.main_btn_pop);
		btnPop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				populateDatabase();
			}
		});

		Button btnComments = (Button) findViewById(R.id.main_btn_comments);
		btnComments.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), CommentList.class);
				startActivity(i);
			}
		});

		Button btnCrash = (Button) findViewById(R.id.main_btn_crash);
		btnCrash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				throw new RuntimeException();
			}
		});

		Button btnCreateCSV = (Button) findViewById(R.id.main_btn_createcsv);
		btnCreateCSV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), FinishedService.class);
				startService(i);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void populateDatabase() {
		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

		if (!sp.contains(Keys.QUES_NUMBER_QUESTIONS)) {
			sp.edit().putInt(Keys.QUES_NUMBER_QUESTIONS, 10).commit();
		}

		Random rand = new Random();
		DataSource source = new DataSource(this);
		source.open();
		for (int i = 1; i < 10; i++) {
			int[] data = new int[sp.getInt(Keys.QUES_NUMBER_QUESTIONS, 10)];
			for (int j = 0; sp.contains(Keys.QUES_TEXT + j); j++) {
				data[j] = rand.nextInt(10);
			}

			source.saveData(i * 5, "12:00", data);
		}
		source.close();
	}

}
