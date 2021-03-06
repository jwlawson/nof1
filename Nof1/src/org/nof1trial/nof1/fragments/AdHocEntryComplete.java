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
package org.nof1trial.nof1.fragments;

import java.util.Calendar;

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.activities.GraphChooser;
import org.nof1trial.nof1.activities.HomeScreen;
import org.nof1trial.nof1.services.Scheduler;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Dialog fragment to show when the user fills in the questionnaire without
 * prompting.
 * 
 * @author John Lawson
 * 
 */
public class AdHocEntryComplete extends SherlockDialogFragment {

	public static AdHocEntryComplete newInstance() {
		AdHocEntryComplete frag = new AdHocEntryComplete();

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.ad_hoc_complete, container, false);

		TextView thanks = (TextView) view.findViewById(R.id.ad_hoc_text_thanks);

		// Get layout, as we might need to refresh later
		RelativeLayout layout = (RelativeLayout) thanks.getParent();

		// Load preferences
		SharedPreferences userPrefs = getActivity().getSharedPreferences(Keys.DEFAULT_PREFS, Context.MODE_PRIVATE);
		SharedPreferences configPrefs = getActivity().getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE);
		SharedPreferences schedPrefs = getActivity().getSharedPreferences(Keys.SCHED_NAME, Context.MODE_PRIVATE);
		Resources res = getActivity().getResources();

		thanks.setText(res.getText(R.string.thanks) + " " + userPrefs.getString(Keys.DEFAULT_PATIENT_NAME, ""));

		TextView progress = (TextView) view.findViewById(R.id.ad_hoc_text_you_are);
		progress.setText("" + res.getText(R.string.ad_hoc_you_are_now) + schedPrefs.getInt(Keys.SCHED_CUMULATIVE_DAY, 0)
				+ res.getText(R.string.out_of)
				+ (configPrefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0) * configPrefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0) * 2));

		TextView cancelText = (TextView) view.findViewById(R.id.ad_hoc_text_cancel_today);
		final Button btnCancel = (Button) view.findViewById(R.id.ad_hoc_btn_cancel_today);

		// Get next date for scheduler to run
		SharedPreferences sp = getActivity().getSharedPreferences(Keys.SCHED_NAME, Context.MODE_PRIVATE);
		String alarm = sp.getString(Keys.SCHED_NEXT_DATE, "");
		Calendar cal = Calendar.getInstance();

		String now = cal.get(Calendar.DAY_OF_MONTH) + ":" + cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.YEAR);
		if (alarm.equalsIgnoreCase(now) && !sp.getString(Keys.CONFIG_START, "").equals(now)) {
			// Will have an alarm today
			btnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Run scheduler to cancel todays alarm
					btnCancel.setEnabled(false);
					Intent intent = new Intent(getActivity(), Scheduler.class);
					intent.setAction(Keys.ACTION_CANCEL_ALARM);
					getActivity().startService(intent);
				}
			});

		} else {
			// Was not scheduled to enter data today, hide buttons
			cancelText.setVisibility(View.GONE);
			btnCancel.setVisibility(View.GONE);
			// refresh layout
			layout.requestLayout();
		}

		Button btnGraph = (Button) view.findViewById(R.id.ad_hoc_btn_graphs);
		btnGraph.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show graphs
				TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
				builder.addNextIntent(new Intent(getActivity(), HomeScreen.class)).addNextIntent(new Intent(getActivity(), GraphChooser.class));
				builder.startActivities();
				getActivity().finish();
			}
		});

		Button btnHome = (Button) view.findViewById(R.id.ad_hoc_btn_home);
		btnHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Go to home screen
				TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
				builder.addNextIntent(new Intent(getActivity(), HomeScreen.class));
				builder.startActivities();
				getActivity().finish();
			}
		});

		Button btnExit = (Button) view.findViewById(R.id.ad_hoc_btn_exit);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Go to android home screen
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});

		return view;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		// Finish activity when the dialog is closed to prevent user pressing
		// back, clearing the dialog and being back
		// at data input screen
		if (getActivity() != null && !getActivity().isFinishing()) {
			getActivity().finish();
		}
	}
}
