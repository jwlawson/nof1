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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

public class RadioFragment extends QuestionFragment implements RadioGroup.OnCheckedChangeListener {

	private static final String TAG = "RadioFragment";
	private static final boolean DEBUG = true;

	private static final String ARGS_TEXT = "argsText";
	private static final String ARGS_MIN = "argsMin";
	private static final String ARGS_MAX = "argsMax";

	private int mSelected;

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
		// TODO checkedId gives currently selected button
		if (DEBUG) Log.d(TAG, "RadioButton selected: " + checkedId);
		mSelected = checkedId;
	}

	@Override
	public int getResult() {
		return mSelected;
	}

}
