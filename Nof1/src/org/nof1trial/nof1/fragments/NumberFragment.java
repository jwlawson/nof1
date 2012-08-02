/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson WMG, University of Warwick
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

import java.util.InputMismatchException;

import org.nof1trial.nof1.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class NumberFragment extends QuestionFragment {
	private static final String TAG = "NumberFragment";
	private static final boolean DEBUG = false;
	
	private static final String ARGS_TEXT = "argsText";
	
	private EditText mText;
	
	public NumberFragment() {
	}
	
	public static NumberFragment newInstance(String text) {
		
		NumberFragment frag = new NumberFragment();
		
		Bundle args = new Bundle();
		args.putString(ARGS_TEXT, text);
		
		frag.setArguments(args);
		
		if (DEBUG) Log.d(TAG, "New instance created");
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.row_layout_data_number, container, false);
		
		Bundle args = getArguments();
		
		TextView text = (TextView) view.findViewById(R.id.data_input_number_text);
		text.setText(args.getString(ARGS_TEXT));
		
		mText = (EditText) view.findViewById(R.id.data_input_number_edittext);
		
		if (DEBUG) Log.d(TAG, "View created");
		
		return view;
	}
	
	@Override
	public int getResult() {
		int result = -1;
		try {
			result = Integer.parseInt(mText.getText().toString());
		} catch (NumberFormatException e) {
			throw new InputMismatchException("Incorrect number entry");
		}
		return result;
	}
}
