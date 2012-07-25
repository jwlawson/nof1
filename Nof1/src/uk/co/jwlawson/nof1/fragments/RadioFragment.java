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

import java.util.InputMismatchException;

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

public class RadioFragment extends QuestionFragment implements RadioGroup.OnCheckedChangeListener {

	private static final String TAG = "RadioFragment";
	private static final boolean DEBUG = false;

	private static final String ARGS_TEXT = "argsText";
	private static final String ARGS_MIN = "argsMin";
	private static final String ARGS_MAX = "argsMax";

	private int mSelected = -1;

	public RadioFragment() {
	}

	public static RadioFragment newInstance(String question, String min, String max) {

		Bundle args = new Bundle();
		args.putString(ARGS_TEXT, question);
		args.putString(ARGS_MIN, min);
		args.putString(ARGS_MAX, max);

		RadioFragment frag = new RadioFragment();
		frag.setArguments(args);

		if (DEBUG) Log.d(TAG, "New instance created");

		return frag;
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.row_layout_data_radio, container, false);

		Bundle args = getArguments();

		TextView text = (TextView) view.findViewById(R.id.data_input_radio_text_question);
		text.setText(args.getString(ARGS_TEXT));

		TextView min = (TextView) view.findViewById(R.id.data_input_radio_text_min);
		min.setText(args.getString(ARGS_MIN));

		TextView max = (TextView) view.findViewById(R.id.data_input_radio_text_max);
		max.setText(args.getString(ARGS_MAX));

		RadioGroup radio = (RadioGroup) view.findViewById(R.id.data_input_radio_radiogroup);
		radio.setOnCheckedChangeListener(this);

		if (DEBUG) Log.d(TAG, "View created");

		return view;

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// checkedId gives currently selected button
		if (DEBUG) Log.d(TAG, "RadioButton selected, ID: " + checkedId);
		switch (checkedId) {
		case R.id.radio0:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 0);
			mSelected = 0;
			break;
		case R.id.radio1:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 1);
			mSelected = 1;
			break;
		case R.id.radio2:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 2);
			mSelected = 2;
			break;
		case R.id.radio3:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 3);
			mSelected = 3;
			break;
		case R.id.radio4:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 4);
			mSelected = 4;
			break;
		case R.id.radio5:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 5);
			mSelected = 5;
			break;
		case R.id.radio6:
			if (DEBUG) Log.d(TAG, "RadioButton selected: " + 6);
			mSelected = 6;
			break;
		}
	}

	@Override
	public int getResult() {
		if (mSelected < 0) {
			// No radio selected
			throw new InputMismatchException("No radio selected");
		}
		return mSelected;
	}

}
