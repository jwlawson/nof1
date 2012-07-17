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
package uk.co.jwlawson.nof1.activities;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.DataSource;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.containers.Question;
import uk.co.jwlawson.nof1.fragments.CheckFragment;
import uk.co.jwlawson.nof1.fragments.CommentFragment;
import uk.co.jwlawson.nof1.fragments.NumberFragment;
import uk.co.jwlawson.nof1.fragments.QuestionFragment;
import uk.co.jwlawson.nof1.fragments.RadioFragment;
import uk.co.jwlawson.nof1.fragments.RescheduleDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

/**
 * Activity to load and show the questionnaire presented to the patient. Can be run in preview mode, so the doctor can
 * see what the questionnaire will look like
 * without risking saving any data.
 * 
 * @author John Lawson
 * 
 */
public class Questionnaire extends SherlockFragmentActivity implements RescheduleDialog.OnRescheduleListener {

	private static final String TAG = "Questionnaire";
	private static final boolean DEBUG = true;

	public static final int RESULT_DONE = 10;
	public static final int RESULT_BACK = 11;

	/** List of all questions included in questionnaire */
	private ArrayList<QuestionFragment> mQuestionList;

	private CommentFragment mComment;

	/** True if in preview mode: no details saved */
	private boolean mPreview;

	private DataSource mData;

	public Questionnaire() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.mock_layout_data_input);

		setSupportProgressBarIndeterminateVisibility(false);

		mQuestionList = new ArrayList<QuestionFragment>();

		if (savedInstanceState == null) {
			// Need to load fragments
			new QuestionLoader().execute();
		}

		Intent i = getIntent();
		mPreview = i.getBooleanExtra(Keys.INTENT_PREVIEW, false);

		Button btnOk = (Button) findViewById(R.id.data_input_button_ok);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});

		Button btnCan = (Button) findViewById(R.id.data_input_button_cancel);
		btnCan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		if (mPreview) {
			Log.d(TAG, "Building questionnaire in preview mode");
			btnOk.setText(R.string.save);
			btnCan.setText(R.string.cancel);
			Toast.makeText(this, R.string.questionnaire_preview_explain, Toast.LENGTH_LONG).show();
		} else {
			Log.d(TAG, "Building questionnaire and loading database");
			mData = new DataSource(this);
			new DataBaseLoader().execute();
		}
	}

	private void save() {
		if (mPreview) {
			// Save the questionnaire and return to DoctorConfig
			// Well, questions are already saved to SharedPreferenced, so just return
			setResult(RESULT_DONE);
			finish();
		} else {
			// Save data to database

			// Get day of trial we are in
			int day = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE).getInt(Keys.SCHED_CUMULATIVE_DAY, 0);

			// Get question responses
			int[] data = new int[mQuestionList.size()];
			for (int i = 0; i < mQuestionList.size(); i++) {
				data[i] = mQuestionList.get(i).getResult();
			}
			if (mComment != null) {
				// Save with comment
				String comment = mComment.getComment();
				mData.saveData(day, data, comment);
			} else {
				// Save without comment
				mData.saveData(day, data);
			}
			// TODO handle ad hoc data entry
		}
	}

	private void cancel() {
		if (mPreview) {
			// Return to FormBuilder
			setResult(RESULT_BACK);
			finish();
		} else {
			// Show reschedule dialog
			RescheduleDialog dialog = RescheduleDialog.newInstance();
			dialog.show(getSupportFragmentManager(), "dialog");

		}
	}

	private class QuestionLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (DEBUG) Log.d(TAG, "AsyncTask QuestionLoader started");
			setSupportProgressBarIndeterminateVisibility(true);

			SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

			// Check whether the device is large enough for 2 columns
			boolean dualCol = (findViewById(R.id.data_input_fragment_layout2) != null);
			if (dualCol) Log.d(TAG, "Dual columns found");
			int column = 0;

			// Go through question shared preference and extract each question
			for (int i = 0; sp.contains(Keys.QUES_TEXT + i); i++) {

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

				QuestionFragment q;

				int inputType = sp.getInt(Keys.QUES_TYPE + i, Question.SCALE);

				// Make QuestionFragment
				switch (inputType) {
				case Question.SCALE:
					q = RadioFragment.newInstance(sp.getString(Keys.QUES_TEXT + i, null), sp.getString(Keys.QUES_MIN + i, null),
							sp.getString(Keys.QUES_MAX + i, null));
					break;
				case Question.NUMBER:
					q = NumberFragment.newInstance(sp.getString(Keys.QUES_TEXT + i, null));
					break;
				case Question.CHECK:
					q = CheckFragment.newInstance(sp.getString(Keys.QUES_TEXT + i, null), false);
					break;
				default:
					// Should never happen
					q = null;
					Log.e(TAG, "Something horrid has happened: Unknown question type");
				}
				mQuestionList.add(q);

				// Add the questionFragment to the layout.
				switch (column) {
				case 0:
					ft.add(R.id.data_input_fragment_layout, q, "quesFrag" + i);
					break;
				case 1:
					ft.add(R.id.data_input_fragment_layout2, q, "quesFrag" + i);
					break;
				}
				if (dualCol) {
					column++;
					column %= 2;
				}

				ft.commit();

			}
			// If should show comment fragment, add to layout
			if (sp.getBoolean(Keys.COMMENT, false)) {
				if (DEBUG) Log.d(TAG, "Adding comment fragment");
				mComment = CommentFragment.newInstance();
				getSupportFragmentManager().beginTransaction().add(R.id.data_input_comment_frame, mComment, "com").commit();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (DEBUG) Log.d(TAG, "AsyncTask QuestionLoader finished");
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	private class DataBaseLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mData.open();
			return null;
		}

	}

	@Override
	public void onReschedule(boolean rescheduled) {
		if (rescheduled) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Dismiss any open dialogs to prevent leaks
		RescheduleDialog dialog = (RescheduleDialog) getSupportFragmentManager().findFragmentByTag("dialog");
		if (dialog != null) dialog.dismiss();

		// Close database connection
		if (mData != null) mData.close();
	}
}
