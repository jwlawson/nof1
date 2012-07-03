package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button btnNotification = (Button) findViewById(R.id.main_btn_noti);
		btnNotification.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Notification noti = new NotificationCompat.Builder(getBaseContext())
						.setContentTitle("Plese fill in a form")
						.setContentText("Nof1 Trails")
						.setSmallIcon(R.drawable.ic_launcher)
						.setLargeIcon(
								BitmapFactory
										.decodeResource(getResources(), R.drawable.ic_launcher))
						.getNotification();

				((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1,
						noti);
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
