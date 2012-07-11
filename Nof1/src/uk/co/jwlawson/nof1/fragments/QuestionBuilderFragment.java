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

import java.security.InvalidParameterException;

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

/**
 * Fragment containing a number of input fields to allow clinician to build a
 * question. Can be displayed as either a dialog or a view.
 * 
 * @author John Lawson
 * 
 */
public class QuestionBuilderFragment extends SherlockDialogFragment implements AdapterView.OnItemSelectedListener {

	private static final String TAG = "QuestionBuilderFragment";
	private static final boolean DEBUG = true;

	public static final int DIALOG = 0;
	public static final int VIEW = 1;

	private static int COUNT = 1;

	private final int mId;

	private RelativeLayout mScaleLayout;
	private boolean mDialog;

	public QuestionBuilderFragment() {
		mId = COUNT;
		COUNT++;
	}

	public static QuestionBuilderFragment newInstance(int viewType) {
		if (DEBUG) Log.d(TAG, "New QuestionBuilderFragment instanced");
		QuestionBuilderFragment qbf = new QuestionBuilderFragment();

		switch (viewType) {
		case DIALOG:
			qbf.setDialog(true);
			break;
		case VIEW:
			qbf.setDialog(false);
			break;
		default:
			throw new InvalidParameterException(TAG + " viewType should be either DIALOG or VIEW");
		}

		if (qbf.mDialog) qbf.setStyle(STYLE_NO_TITLE, 0);
		return qbf;
	}

	private View makeView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.config_question, container, false);

		TextView txtQuest = (TextView) view.findViewById(R.id.config_question_text);
		txtQuest.setText("Question " + mId);

		mScaleLayout = (RelativeLayout) view.findViewById(R.id.config_question_minmax_layout);
		mScaleLayout.setVisibility(View.INVISIBLE);

		Spinner spnInput = (Spinner) view.findViewById(R.id.config_question_spinner_type);
		spnInput.setOnItemSelectedListener(this);

		if (!mDialog) {
			LinearLayout buttonBar = (LinearLayout) view.findViewById(R.id.config_question_button_bar);
			buttonBar.setVisibility(View.INVISIBLE);
		} else {
			Button btnOK = (Button) view.findViewById(R.id.config_question_button_ok);

			Button btnCan = (Button) view.findViewById(R.id.config_question_button_cancel);
		}

		return view;
	}

	private void setDialog(boolean bool) {
		mDialog = bool;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!mDialog) {
			setHasOptionsMenu(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (DEBUG) Log.d(TAG, "View created");

		View view = makeView(inflater, container, savedInstanceState);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		if (DEBUG) Log.d(TAG, "Menu size " + menu.size());
		inflater.inflate(R.menu.menu_config_questions, menu);
		if (DEBUG) Log.d(TAG, "new menu size " + menu.size());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		COUNT--;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.config_question_spinner_type:
			// Input type spinner
			String item = (String) parent.getItemAtPosition(position);
			if (item.equalsIgnoreCase("Scale")) {
				mScaleLayout.setVisibility(View.VISIBLE);
			} else {
				mScaleLayout.setVisibility(View.INVISIBLE);
			}
			break;

		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Don't care
	}
}
