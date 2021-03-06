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

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.activities.GraphChooser;
import org.nof1trial.nof1.activities.HomeScreen;
import org.nof1trial.nof1.services.FinishedService;

/**
 * Dialog Fragment to show after user has completed questionnaire. Includes
 * options to go back to home screen, view
 * graphs or just exit.
 * 
 * @author John Lawson
 * 
 */
public class QuesComplete extends SherlockDialogFragment {

	public static QuesComplete newInstance() {
		QuesComplete frag = new QuesComplete();

		Bundle args = new Bundle();

		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.complete_layout, container, false);

		SharedPreferences userPrefs = getActivity().getSharedPreferences(Keys.DEFAULT_PREFS,
				Context.MODE_PRIVATE);
		SharedPreferences configPrefs = getActivity().getSharedPreferences(Keys.CONFIG_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences schedPrefs = getActivity().getSharedPreferences(Keys.SCHED_NAME,
				Context.MODE_PRIVATE);
		Resources res = getActivity().getResources();

		TextView thanks = (TextView) view.findViewById(R.id.complete_text_thanks);
		thanks.setText(res.getText(R.string.thanks) + " "
				+ userPrefs.getString(Keys.DEFAULT_PATIENT_NAME, ""));

		TextView progress = (TextView) view.findViewById(R.id.complete_text_progress);
		progress.setText(""
				+ res.getText(R.string.you_are_now)
				+ schedPrefs.getInt(Keys.SCHED_CUMULATIVE_DAY, 0)
				+ res.getText(R.string.out_of)
				+ (configPrefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0)
						* configPrefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0) * 2));

		// If the trial is finished, show some text saying this
		// SCHED_FINISHED gets set by Scheduler when the last questionnaire noti
		// is posted
		// So should be set before the user has completed the questionnaire
		if (schedPrefs.getBoolean(Keys.SCHED_FINISHED, false)) {
			TextView finished = (TextView) view.findViewById(R.id.complete_text_finished);
			finished.setVisibility(View.VISIBLE);
			((RelativeLayout) finished.getParent()).requestLayout();

			// Start the service to create CSV file
			Intent intent = new Intent(getActivity(), FinishedService.class);
			intent.setAction(Keys.ACTION_COMPLETE);
			getActivity().startService(intent);
		}

		Button btnGraph = (Button) view.findViewById(R.id.complete_btn_graphs);
		btnGraph.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show graphs
				TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
				builder.addNextIntent(new Intent(getActivity(), HomeScreen.class)).addNextIntent(
						new Intent(getActivity(), GraphChooser.class));
				builder.startActivities();
				getActivity().finish();
			}
		});

		Button btnHome = (Button) view.findViewById(R.id.complete_btn_home);
		btnHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show HomeScreen
				TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
				builder.addNextIntent(new Intent(getActivity(), HomeScreen.class));
				builder.startActivities();
				getActivity().finish();
			}
		});

		Button btnExit = (Button) view.findViewById(R.id.complete_btn_exit);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Finish activity. As user got here from notification, this
				// should be the only thing in task stack
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
