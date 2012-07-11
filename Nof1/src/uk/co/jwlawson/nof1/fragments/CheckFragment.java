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
import android.widget.CheckBox;
import android.widget.TextView;

public class CheckFragment extends QuestionFragment {

	private static final String TAG = "CheckFragment";
	private static final boolean DEBUG = true;

	private static final String ARGS_TEXT = "argsText";
	private static final String ARGS_DEFAULT = "argsDefault";

	private boolean mChecked;
	private String mQuestion;

	public CheckFragment() {
	}

	public static CheckFragment newInstance(String questionText, boolean def) {

		CheckFragment frag = new CheckFragment();

		Bundle args = new Bundle();
		args.putString(ARGS_TEXT, questionText);
		args.putBoolean(ARGS_DEFAULT, def);

		frag.setArguments(args);

		if (DEBUG) Log.d(TAG, "New instance created");

		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.row_layout_data_checkbox, container, false);

		Bundle args = getArguments();

		CheckBox chk = (CheckBox) view.findViewById(R.id.data_input_checkbox_chk);
		chk.setChecked(args.getBoolean(ARGS_DEFAULT));

		TextView txt = (TextView) view.findViewById(R.id.data_input_checkbox_txt_question);
		txt.setText(args.getString(ARGS_TEXT));

		if (DEBUG) Log.d(TAG, "View created");

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public int getResult() {
		if (mChecked) return 1;
		else return 0;
	}

}
