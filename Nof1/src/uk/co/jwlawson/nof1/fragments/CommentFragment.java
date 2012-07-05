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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;

public class CommentFragment extends QuestionFragment {

	private static final String TAG = "CommentFragment";
	private static int COUNT;
	private String mComment;
	private EditText mText;
	private boolean init = false;
	private final int mId;

	public CommentFragment() {
		mId = COUNT;
		COUNT++;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		COUNT--;
		// Bit of a hack, but assume all fragments are destroyed at once eg.
		// screen rotation. This means they all get initialised correctly.
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!init) mySetArguments(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (!init) mySetArguments(savedInstanceState);

		View view = inflater.inflate(R.layout.row_layout_data_comment, container, false);

		mText = (EditText) view.findViewById(R.id.data_input_comment_edittext);
		if (mComment != null) mText.setText(mComment);

		if (BuildConfig.DEBUG) Log.d(TAG, mId + " view created");
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("CommentFrag" + mId + "Comment", mComment);
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArguments(args);
	}

	private void mySetArguments(Bundle args) {
		if (args != null && args.containsKey("CommentFrag" + mId + "Comment")) {
			mComment = args.getString("CommentFrag" + mId + "Comment");
		}
		init = true;
	}

	@Override
	public Object getResult() {
		return mComment;
	}
}
