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

import uk.co.jwlawson.nof1.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

public class MainActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button btnNotification = (Button) findViewById(R.id.main_btn_noti);
		btnNotification.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(getBaseContext(), FragActivity.class);
				PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

				Notification noti = new NotificationCompat.Builder(getBaseContext()).setContentTitle("Please fill in a form")
						.setContentText("Nof1 Trials").setSmallIcon(R.drawable.ic_launcher).setContentIntent(pi)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher)).getNotification();

				noti.flags |= Notification.FLAG_AUTO_CANCEL;

				((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, noti);
			}
		});

		Button btnData = (Button) findViewById(R.id.main_btn_data);
		btnData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), FragActivity.class);
				startActivity(i);
			}
		});

		Button btnGraphs = (Button) findViewById(R.id.main_btn_graphs);
		btnGraphs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), GraphActivity.class);
				startActivity(i);
			}
		});

		Button btnPrefs = (Button) findViewById(R.id.main_btn_settings);
		btnPrefs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), FormBuilder.class);
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
