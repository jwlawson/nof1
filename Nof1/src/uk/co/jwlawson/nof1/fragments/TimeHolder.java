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
package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Simple fragment with text view displaying time and an edit button.
 * 
 * @author John Lawson
 * 
 */
public class TimeHolder extends SherlockFragment {

	private static final String ARGS_TIME = "time";

	private String mTime;
	private TextView mTextView;

	public static TimeHolder newInstance(String time) {
		TimeHolder frag = new TimeHolder();

		Bundle args = new Bundle();
		args.putString(ARGS_TIME, time);

		frag.setArguments(args);

		return frag;
	}

	public String getTime() {
		return mTime;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mTime = getArguments().getString(ARGS_TIME);
		} else {
			mTime = "12:00";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.config_doctor_medicine_timer, container, false);

		mTextView = (TextView) view.findViewById(R.id.config_doctor_medicine_timer_text);
		mTextView.setText(mTime);

		Button edit = (Button) view.findViewById(R.id.config_doctor_medicine_timer_edit);
		edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});

		return view;
	}

	private void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final TimePicker picker = new TimePicker(getActivity());
		picker.setCurrentHour(getHour());
		picker.setCurrentMinute(getMin());
		builder.setView(picker);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int hour = picker.getCurrentHour();
				int min = picker.getCurrentMinute();

				mTime = hour + ":" + min;
				mTextView.setText(mTime);

				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private int getHour() {
		String[] arr = mTime.split(":");
		return Integer.parseInt(arr[0]);
	}

	private int getMin() {
		String[] arr = mTime.split(":");
		return Integer.parseInt(arr[1]);
	}

}
