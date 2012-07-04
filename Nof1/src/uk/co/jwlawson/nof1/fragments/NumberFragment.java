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
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class NumberFragment extends SherlockFragment {
	private static final String TAG = "NumberFragment";

	private static int COUNT = 0;
	private String mQuestion;
	private EditText mText;
	private String mNumber;
	private boolean init = false;
	private int mId;

	public NumberFragment() {
		mId = COUNT;
		COUNT++;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		COUNT--;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!init) mySetArguments(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (!init) mySetArguments(savedInstanceState);

		View view = inflater.inflate(R.layout.row_layout_data_number, container, false);

		TextView text = (TextView) view.findViewById(R.id.data_input_number_text);
		text.setText(mQuestion);

		mText = (EditText) view.findViewById(R.id.data_input_number_edittext);
		if (mNumber != null) mText.setText(mNumber);

		if (BuildConfig.DEBUG) Log.d(TAG, mId + " view created");

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("NumFrag" + mId + "Question", mQuestion);
		outState.putString("NumFrag" + mId + "Number", mNumber);
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArguments(args);
	}

	private void mySetArguments(Bundle args) {
		if (args != null) {
			mQuestion = args.getString("NumFrag" + mId + "Question");
			mNumber = args.getString("NumFrag" + mId + "Number");
			init = true;
		}
	}
}
