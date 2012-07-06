/*******************************************************************************
 * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.BuildConfig;
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
	private static int COUNT = 0;
	
	private String mQuestion;
	private String mMin;
	private String mMax;
	private int mSelected;
	private boolean init = false;
	private final int mId;
	
	public RadioFragment() {
		mId = COUNT;
		COUNT++;
		
	}
	
	public int getSelected() {
		return mSelected;
	}
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		if (!init) mySetArguments(savedState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if (!init) mySetArguments(savedInstanceState);
		
		View view = inflater.inflate(R.layout.row_layout_data_radio, container, false);
		
		((TextView) view.findViewById(R.id.data_input_radio_text_question)).setText(mQuestion);
		
		TextView min = (TextView) view.findViewById(R.id.data_input_radio_text_min);
		min.setText(mMin);
		
		TextView max = (TextView) view.findViewById(R.id.data_input_radio_text_max);
		max.setText(mMax);
		
		RadioGroup radio = (RadioGroup) view.findViewById(R.id.data_input_radio_radiogroup);
		radio.setOnCheckedChangeListener(this);
		
		return view;
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// For each questionFragment, add its strings to the bundle
		// Use Id to differentiate.
		outState.putString("QuesFrag" + mId + "Question", mQuestion);
		outState.putString("QuesFrag" + mId + "Min", mMin);
		outState.putString("QuesFrag" + mId + "Max", mMax);
	}
	
	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArguments(args);
	}
	
	private void mySetArguments(Bundle args) {
		if (args != null) {
			if (args.containsKey("QuesFrag" + mId + "Question")) mQuestion = args.getString("QuesFrag" + mId + "Question");
			if (args.containsKey("QuesFrag" + mId + "Min")) mMin = args.getString("QuesFrag" + mId + "Min");
			if (args.containsKey("QuesFrag" + mId + "Max")) mMax = args.getString("QuesFrag" + mId + "Max");
		}
		init = true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Destroyed");
		COUNT--;
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// checkedId gives currently selected button
		if (BuildConfig.DEBUG) Log.d(TAG, "RadioButton selected: " + checkedId);
		mSelected = checkedId;
	}
	
	@Override
	public int getResult() {
		return mSelected;
	}
	
}
