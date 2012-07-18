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

import java.util.ArrayList;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment holding a number of checkboxes. Can increase/decrease number while running.
 * 
 * @author John Lawson
 * 
 */
public class CheckArray extends SherlockFragment {

	private static final String TAG = "CheckArray Fragment";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	private static final String ARGS_NUMBER = "number";
	private static final String ARGS_PREFIX = "prefix";
	private static final String ARGS_CHECKED = "checked";

	private RelativeLayout mLayout;

	private ArrayList<CheckBox> mBoxList;

	public CheckArray() {
		mBoxList = new ArrayList<CheckBox>();
	}

	/**
	 * Make new instance of CheckArray with {@code <number>} checkboxes labelled {@code <prefix>1 ... <prefix><number>}
	 * 
	 * @param number the number of checkboxes in new array
	 * @param prefix the prefix for each label, a single trailing space is added to this.
	 * @return New CheckArray
	 */
	public static CheckArray newInstance(int number, String prefix) {
		CheckArray frag = new CheckArray();

		Bundle args = new Bundle();
		args.putInt(ARGS_NUMBER, number);
		args.putString(ARGS_PREFIX, prefix);
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

		View view = inflater.inflate(R.layout.check_array, container, false);

		Bundle args = getArguments();

		CheckBox chk = (CheckBox) view.findViewById(R.id.check_array_box);
		chk.setText(args.getString(ARGS_PREFIX) + " " + 1);
		chk.setId(1);
		mBoxList.add(chk);

		mLayout = (RelativeLayout) chk.getParent();

		if (args.containsKey(ARGS_NUMBER)) {
			if (DEBUG) Log.d(TAG, "Loading previous number of boxes " + args.getInt(ARGS_NUMBER, 1));
			int num = args.getInt(ARGS_NUMBER, 1);

			// Layout starts with 1
			setNumber(num, 1);
		} else {
			args.putInt(ARGS_NUMBER, 1);
		}

		if (args.containsKey(ARGS_CHECKED)) {
			if (DEBUG) Log.d(TAG, "Loading checked data");
			int[] arr = args.getIntArray(ARGS_CHECKED);
			for (int i : arr) {
				// i refers to the label, not the index
				mBoxList.get(i - 1).setChecked(true);
			}
		}

		if (DEBUG) Log.d(TAG, "View created. Width: " + mLayout.getMeasuredWidth());
		return mLayout;
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);
		if (DEBUG) Log.d(TAG, "CheckArray inflated");

		if (getArguments() == null) {
			Bundle args = new Bundle();

			TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.CheckFragmentArguments);
			args.putString(ARGS_PREFIX, (String) a.getText(R.styleable.CheckArrayArguments_android_label));

			a.recycle();

			setArguments(args);
			if (DEBUG) Log.d(TAG, "Args loaded: " + a.getText(R.styleable.CheckArrayArguments_android_label));
		}
	}

	/** Set the number of checkboxes to display */
	public void setNumber(int num) {
		setNumber(num, getArguments().getInt(ARGS_NUMBER));
	}

	/**
	 * Set the number of checkboxes to display
	 * 
	 * @param num Number of boxes to display
	 * @param size Number of boxes currently shown
	 */
	public void setNumber(int num, int size) {
		Bundle args = getArguments();
		if (DEBUG) Log.d(TAG, "Storing number of boxes " + num);
		args.putInt(ARGS_NUMBER, num);

		if (num == size) {
			// No changes needed.
			return;
		} else if (num > size) {
			// Add more check boxes
			String prefix = args.getString(ARGS_PREFIX);

			for (int i = size + 1; i <= num; i++) {
				if (DEBUG) Log.d(TAG, "Adding " + i);
				CheckBox chk = new CheckBox(getActivity());
				chk.setId(i);
				chk.setText(prefix + " " + i);
				if (DEBUG) Log.d(TAG, "New checkbox " + i + " width: " + chk.getMeasuredWidth());
				mBoxList.add(chk);

				// TODO More than one column?
				RelativeLayout.LayoutParams params = (LayoutParams) chk.getLayoutParams();
				if (params == null) {
					params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				}
				if (i >= 2) params.addRule(RelativeLayout.BELOW, mBoxList.get(i - 2).getId());

				chk.setLayoutParams(params);
				mLayout.addView(chk);
			}

		} else {
			// Get rid of checkboxes
			for (int i = size; i > num; i--) {
				if (DEBUG) Log.d(TAG, "Removing " + i);
				CheckBox chk = mBoxList.get(i - 1);
				mBoxList.remove(i - 1);
				mLayout.removeView(chk);
			}
		}

		mLayout.requestLayout();
	}

	/** Get positions of all selected checkboxes */
	public int[] getSelected() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (CheckBox chk : mBoxList) {
			if (chk.isChecked()) {
				list.add(chk.getId());
			}
		}
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	/** Set the states of all checkboxes in array */
	public void setSelected(boolean[] selected) {

		int min = selected.length < mBoxList.size() ? selected.length : mBoxList.size();

		for (int i = 0; i < min; i++) {
			mBoxList.get(i).setChecked(selected[i]);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getArguments().putIntArray(ARGS_CHECKED, getSelected());
	}
}
