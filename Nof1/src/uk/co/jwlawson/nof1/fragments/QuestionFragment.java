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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class QuestionFragment extends SherlockFragment implements
		RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

	private static final String TAG = "QuestionFragment";
	private static final int RADIOWIDTH = 450;
	private static int COUNT = 0;

	private static int sDim;
	private static boolean sDimInit = true;

	private String mQuestion;
	private String mMin;
	private String mMax;
	private int mSelected;
	private boolean init = false;
	private int mId;

	public QuestionFragment() {
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
		if (sDimInit) Log.d(TAG, "Oops");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (!init) mySetArguments(savedInstanceState);

		if (sDimInit) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Loading dimension data");
			DisplayMetrics metrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int h = metrics.heightPixels;
			int w = metrics.widthPixels;
			sDim = h > w ? w : h;
			sDimInit = false;
		}

		View view = inflater.inflate(R.layout.mock_row_layout_data_input, container, false);

		((TextView) view.findViewById(R.id.textView1)).setText(mQuestion);

		TextView min = (TextView) view.findViewById(R.id.textView2);
		min.setText(mMin);

		TextView max = (TextView) view.findViewById(R.id.textView3);
		max.setText(mMax);

		RadioGroup radio = (RadioGroup) view.findViewById(R.id.radioGroup1);
		SeekBar seek = (SeekBar) view.findViewById(R.id.seekBar1);

		if (BuildConfig.DEBUG) Log.d(TAG, "" + container.getMeasuredWidth() + " " + sDim);

		// At first run, container.getMeasuredWidth() returns correct value, but
		// after rotate only returns 0;
		boolean wide = (sDim / 2 > RADIOWIDTH);

		// Wide is assumed, so all relations are set up for this
		if (wide) {
			radio.setOnCheckedChangeListener(this);
			seek.setVisibility(View.GONE);
		} else {
			seek.setOnSeekBarChangeListener(this);
			radio.setVisibility(View.GONE);

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(min.getLayoutParams());
			lp.addRule(RelativeLayout.ALIGN_LEFT, R.id.seekBar1);
			lp.addRule(RelativeLayout.BELOW, R.id.seekBar1);
			min.setLayoutParams(lp);

			lp = new RelativeLayout.LayoutParams(max.getLayoutParams());
			lp.addRule(RelativeLayout.ALIGN_RIGHT, R.id.seekBar1);
			lp.addRule(RelativeLayout.BELOW, R.id.seekBar1);
			max.setLayoutParams(lp);

			// Fix the spacing of min and max.
			// Currently the seekbar view fills screen, despite having padding.
			Resources res = getResources();
			min.setPadding((int) res.getDimension(R.dimen.padding_large), 0, 0,
					(int) res.getDimension(R.dimen.padding_vsmall));
			max.setPadding(0, 0, (int) res.getDimension(R.dimen.padding_large),
					(int) res.getDimension(R.dimen.padding_vsmall));
		}

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
			if (args.containsKey("QuesFrag" + mId + "Question"))
				mQuestion = args.getString("QuesFrag" + mId + "Question");
			if (args.containsKey("QuesFrag" + mId + "Min"))
				mMin = args.getString("QuesFrag" + mId + "Min");
			if (args.containsKey("QuesFrag" + mId + "Max"))
				mMax = args.getString("QuesFrag" + mId + "Max");
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
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// progress is value between 0 and 7
		if (BuildConfig.DEBUG) Log.d(TAG, "Seekbar at " + progress);
		mSelected = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Not needed
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Not needed
	}

}
