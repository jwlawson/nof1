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
import android.widget.CheckBox;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CheckFragment extends SherlockFragment {

	private static final String TAG = "CheckFragment";

	private static int COUNT = 0;

	private boolean mChecked;
	private String mQuestion;
	private boolean init = false;
	private final int mId;

	public CheckFragment() {
		mId = COUNT;
		COUNT++;
	}

	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.row_layout_data_checkbox, container, false);

		if (!init) mySetArgs(savedInstanceState);

		CheckBox chk = (CheckBox) view.findViewById(R.id.data_input_checkbox_chk);
		chk.setChecked(mChecked);

		TextView txt = (TextView) view.findViewById(R.id.data_input_checkbox_txt_question);
		txt.setText(mQuestion);

		if (BuildConfig.DEBUG) Log.d(TAG, mId + " view created");

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		COUNT--;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!init) mySetArgs(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("CheckFrag" + mId + "Checked", mChecked);
		outState.putString("CheckFrag" + mId + "Question", mQuestion);
	};

	private void mySetArgs(Bundle args) {
		if (args != null) {
			if (args.containsKey("CheckFrag" + mId + "Checked")) {
				mChecked = args.getBoolean("CheckFrag" + mId + "Checked");
			}
			if (args.containsKey("CheckFrag" + mId + "Question")) {
				mQuestion = args.getString("CheckFrag" + mId + "Question");
			}
		}
		init = true;
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArgs(args);
	}

}
