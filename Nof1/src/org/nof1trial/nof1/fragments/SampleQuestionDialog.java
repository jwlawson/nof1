/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson
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

import java.util.ArrayList;

import org.nof1trial.nof1.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * @author John Lawson
 * 
 */
public class SampleQuestionDialog extends SherlockDialogFragment implements OnClickListener {

	private ArrayList<CheckBox> mList;

	private OnSamplesCheckedListener mListener;

	public static SampleQuestionDialog newInstance() {

		SampleQuestionDialog frag = new SampleQuestionDialog();

		return frag;
	}

	public interface OnSamplesCheckedListener {
		public abstract void onSampleChecked(boolean[] checked);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mList = new ArrayList<CheckBox>();
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (OnSamplesCheckedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass().getName() + " must implement OnSamplesCheckedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sample_questions, container, false);

		String[] questions = getResources().getStringArray(R.array.sample_ques_brief);

		RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.sample_questions_text).getParent();

		int id = R.id.sample_questions_text;

		for (int i = 0; i < questions.length; i++) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, id);

			CheckBox chk = new CheckBox(getActivity());
			chk.setText(questions[i]);
			chk.setId(0x1000 + i);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				// Pre honeycomb have dark dialogs, so need to set text as white
				chk.setTextColor(Color.WHITE);
			}

			layout.addView(chk, params);

			mList.add(chk);

			id = 0x1000 + i;
		}

		layout.requestLayout();

		Button btnOk = (Button) view.findViewById(R.id.sample_questions_btn_ok);
		btnOk.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		boolean[] result = new boolean[mList.size()];

		for (int i = 0; i < mList.size(); i++) {
			result[i] = mList.get(i).isChecked();
		}

		mListener.onSampleChecked(result);
		dismiss();
	}

}
