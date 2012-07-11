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
package uk.co.jwlawson.nof1.activities;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.containers.Question;
import uk.co.jwlawson.nof1.fragments.FormBuilderList;
import uk.co.jwlawson.nof1.fragments.QuestionBuilderDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author John Lawson
 * 
 */
public class FormBuilder extends SherlockFragmentActivity implements FormBuilderList.OnListItemSelectedListener,
		QuestionBuilderDialog.OnQuestionEditedListener {

	private static final String TAG = "FormBuilder";
	private static final boolean DEBUG = true;
	private static final String PREFS_NAME = "questions";

	/** ListFragment */
	private FormBuilderList mList;

	/** true when the actionmode is active */
	private boolean mInActionMode = false;

	/** True if using dual pane layout */
	private boolean mDualPane;

	/** Tracks the selected item in the ListFragment */
	private int mListSelected = -1;

	/** List of questions passed on to ListFragment */
	private ArrayList<Question> mQuestionList;

	/** Currently selected or last instanced QuestionBuilder */
	private QuestionBuilderDialog mQuestionBuilder;

	public FormBuilder() {
		mQuestionList = new ArrayList<Question>();
	}

	/** Set the currently selected item. Also sets it in ListFragment */
	private void setListSelected(int listSelected) {
		mListSelected = listSelected;
		if (mList != null) {
			mList.setSelection(listSelected);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		// Load layout. This will change depending on the screen size
		setContentView(R.layout.form_builder_list);

		// Find list fragment
		mList = (FormBuilderList) getSupportFragmentManager().findFragmentById(R.id.form_builder_list_fragment);

		// TODO Fill mQuestionList and set as the array for mList
		for (int i = 1; sp.contains("questionText" + i); i++) {

			int inputType = sp.getInt("inputType" + i, Question.SCALE);
			String text = sp.getString("questionText" + i, "Questiontext");

			Question q = new Question(inputType, text);
			if (inputType == Question.SCALE) {
				String min = sp.getString("questionMin" + i, "");
				String max = sp.getString("questionMax" + i, "");
				q.setMinMax(min, max);
			}

			mQuestionList.add(q);
		}
		mList.setArrayList(mQuestionList);

		// See if the layout has a frame for the second pane.
		FrameLayout frame = (FrameLayout) findViewById(R.id.form_builder_details_frame);
		mDualPane = (frame != null) && (frame.getVisibility() == View.VISIBLE);
		if (DEBUG) Log.d(TAG, "DualPane = " + mDualPane);

		if (savedInstanceState != null) {
			// Get selected item from saved state
			setListSelected(savedInstanceState.getInt(TAG + "listSelection"));
		} else if (mDualPane) {
			// Otherwise, load up the first item on the list
			setListSelected(0);
			edit(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.form_builder_menu, menu);
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_form_builder_add:
			// Add new question to bottom of the list and load new QuestionBuilder
			Question q = new Question(Question.SCALE);
			mQuestionList.add(q);
			((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
			setListSelected(mQuestionList.indexOf(q));
			edit(mListSelected);
			return true;
		case R.id.menu_form_builder_preview:
			// TODO Build and preview a questionnaire
			saveToFile();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onListItemSelected(ListView l, View v, int position, long id) {
		mListSelected = position;
		if (!mInActionMode) {
			startActionMode(new ListActionMode());
		}
		if (mDualPane) {
			edit(position);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TAG + "listSelection", mListSelected);
	}

	/** Show the selected question in some editable form */
	private void edit(int selection) {
		QuestionBuilderDialog q;
		if (mDualPane) {

			q = QuestionBuilderDialog.newInstance(QuestionBuilderDialog.VIEW, mQuestionList.get(selection));
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.form_builder_details_frame, q, "view");
			ft.commit();

		} else {
			q = QuestionBuilderDialog.newInstance(QuestionBuilderDialog.DIALOG, mQuestionList.get(selection));

			// Shows fragment as dialog
			q.show(getSupportFragmentManager(), "dialog");
		}
		mQuestionBuilder = q;
	}

	private void saveToFile() {
		SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

		for (int i = 0; i < mQuestionList.size(); i++) {
			Question q = mQuestionList.get(i);
			editor.putString("questionText" + i, q.getQuestionStr()).putInt("inputType" + i, q.getInputType());
			if (q.getInputType() == Question.SCALE) {
				editor.putString("questionMin", q.getMin()).putString("questionMax", q.getMax());
			}
		}
		editor.commit();
	}

	/**
	 * ActionMode class to show when a list item is selected. Provides more
	 * options specific for that item.
	 */
	private class ListActionMode implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getSupportMenuInflater().inflate(R.menu.form_builder_action_mode, menu);
			if (mDualPane) {
				menu.removeItem(R.id.action_mode_form_builder_edit);
				getSupportMenuInflater().inflate(R.menu.menu_config_questions, menu);
			}
			mInActionMode = true;
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
			case R.id.action_mode_form_builder_edit:
				edit(mListSelected);
				break;
			case R.id.action_mode_form_builder_delete:
				mQuestionList.remove(mListSelected);
				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
				break;
			case R.id.action_mode_form_builder_move_up:
				Question q = mQuestionList.get(mListSelected);
				mQuestionList.remove(mListSelected);
				mQuestionList.add(mListSelected - 1, q);
				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
				break;
			case R.id.action_mode_form_builder_move_down:
				Question q1 = mQuestionList.get(mListSelected);
				mQuestionList.remove(mListSelected);
				mQuestionList.add(mListSelected + 1, q1);
				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
				break;
			case R.id.menu_config_questions_save:
				mQuestionBuilder.save();
				break;

			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (DEBUG) Log.d(TAG, "Actionmode closed");
			if (!mDualPane) mList.clearSelected();
			mInActionMode = false;
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onQuestionEdited(Question question) {
		mQuestionList.remove(mListSelected);
		mQuestionList.add(mListSelected, question);
		((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
	}

}
