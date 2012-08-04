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

import org.nof1trial.nof1.R;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author John Lawson
 * 
 */
public class StartDate extends SherlockFragment implements OnCheckedChangeListener, OnDateSetListener {
	
	private static final String TAG = "StartDate";
	private static final boolean DEBUG = false;
	
	private static final String ARGS_DATE = "date";
	
	private CheckBox mCheck;
	
	private Button mPicker;
	
	private int[] mDate;
	
	public StartDate() {
		mDate = new int[3];
	}
	
	/**
	 * Create and initialise a new StartDate fragment
	 * 
	 * @param startDate The date to set initially or null for today
	 * @return New instance
	 */
	public static StartDate newInstance(String startDate) {
		StartDate frag = new StartDate();
		
		Bundle args = new Bundle();
		args.putString(ARGS_DATE, startDate);
		frag.setArguments(args);
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		
		if (args != null && (args.getString(ARGS_DATE) != null)) {
			
			String date = args.getString(ARGS_DATE);
			String[] dates = date.split(":");
			mDate[0] = Integer.parseInt(dates[0]);
			mDate[1] = Integer.parseInt(dates[1]);
			mDate[2] = Integer.parseInt(dates[2]);
			
		} else {
			
			Calendar c = Calendar.getInstance();
			mDate[0] = c.get(Calendar.DAY_OF_MONTH);
			mDate[1] = c.get(Calendar.MONTH);
			mDate[2] = c.get(Calendar.YEAR);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.start_date, container, false);
		
		mCheck = (CheckBox) view.findViewById(R.id.start_date_check);
		mCheck.setOnCheckedChangeListener(this);
		
		mPicker = (Button) view.findViewById(R.id.start_date_picker);
		mPicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open datePickerDialog
				showDatePicker();
			}
		});
		
		return view;
	}
	
	private void showDatePicker() {
		DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.dialog_theme, this, mDate[2], mDate[1], mDate[0]);
		dialog.show();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mCheck) {
			mPicker.setEnabled(!isChecked);
		}
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		if (DEBUG) Log.d(TAG, "Date set: " + dayOfMonth + ":" + monthOfYear + ":" + year);
		mDate[0] = dayOfMonth;
		mDate[1] = monthOfYear;
		mDate[2] = year;
	}
	
	public void setDate(String date) {
		getArguments().putString(ARGS_DATE, date);
	}
	
	public String getDate() {
		StringBuilder sb = new StringBuilder();
		if (mCheck.isChecked()) {
			
			Calendar c = Calendar.getInstance();
			mDate[0] = c.get(Calendar.DAY_OF_MONTH);
			mDate[1] = c.get(Calendar.MONTH);
			mDate[2] = c.get(Calendar.YEAR);
			
		}
		
		sb.append(mDate[0]).append(":");
		sb.append(mDate[1]).append(":");
		sb.append(mDate[2]);
		
		return sb.toString();
		
	}
	
}
