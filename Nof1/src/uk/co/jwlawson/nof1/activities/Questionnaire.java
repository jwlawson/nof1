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

import uk.co.jwlawson.nof1.containers.Question;
import uk.co.jwlawson.nof1.fragments.CheckFragment;
import uk.co.jwlawson.nof1.fragments.NumberFragment;
import uk.co.jwlawson.nof1.fragments.QuestionFragment;
import uk.co.jwlawson.nof1.fragments.RadioFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Activity to load and show the questionnaire presented to the patient. Can be run in preview mode, so the doctor can
 * see what th questionnaire will look like without risking saving any data.
 * 
 * @author John Lawson
 * 
 */
public class Questionnaire extends SherlockFragmentActivity {

	private static final String TAG = "Questionnaire";
	private static final boolean DEBUG = true;
	private static final String PREFS_NAME = "questions";

	private ArrayList<QuestionFragment> mQuestionList;

	public Questionnaire() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		for (int i = 0; sp.contains("questionText" + i); i++) {
			QuestionFragment q;
			int inputType = sp.getInt("inputType" + i, Question.SCALE);
			switch (inputType) {
			case Question.SCALE:
				q = new RadioFragment();
				break;
			case Question.NUMBER:
				q = new NumberFragment();
				break;
			case Question.CHECK:
				q = new CheckFragment();
				break;
			default:
				// Should never happen
				q = null;
				Log.e(TAG, "Something horrid has happened: Unknown question type");
			}
			mQuestionList.add(q);
		}
	}
}
