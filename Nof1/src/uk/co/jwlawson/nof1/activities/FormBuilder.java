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

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.containers.Question;
import uk.co.jwlawson.nof1.fragments.CheckFragment;
import uk.co.jwlawson.nof1.fragments.FormBuilderList;
import uk.co.jwlawson.nof1.fragments.QuestionBuilderDialog;
import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	private static final int REQUEST_PREVIEW = 10;

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

	/** CheckFragment asking whether to show a comment box */
	private CheckFragment mCommentFrag;

	/** Instance of backup manager */
	private BackupManager mBackupManager;

	/** True if want backup */
	private boolean mBackup;

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

	@TargetApi(8)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

		// Load layout. This will change depending on the screen size
		setContentView(R.layout.form_builder_list);

		// Find list fragment
		mList = (FormBuilderList) getSupportFragmentManager().findFragmentById(R.id.form_builder_list_fragment);

		// Fill mQuestionList and set as the array for mList
		for (int i = 0; sp.contains(Keys.QUES_TEXT + i); i++) {

			int inputType = sp.getInt(Keys.QUES_TYPE + i, Question.SCALE);
			String text = sp.getString(Keys.QUES_TEXT + i, "");

			Question q = new Question(inputType, text);
			if (inputType == Question.SCALE) {
				String min = sp.getString(Keys.QUES_MIN + i, "");
				String max = sp.getString(Keys.QUES_MAX + i, "");
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
		} else if (mDualPane && mQuestionList.size() > 0) {
			// Otherwise, load up the first item on the list
			setListSelected(0);
			edit(0);
		}

		mCommentFrag = (CheckFragment) getSupportFragmentManager().findFragmentById(R.id.form_builder_check_fragment);

		SharedPreferences userPrefs = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
		mBackup = userPrefs.getBoolean(Keys.DEFAULT_BACKUP, false);

		if (mBackup && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager = new BackupManager(this);
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
		case R.id.menu_form_builder_done:
			// User happy with questionnaire
			setResult(RESULT_OK);
			finish();
			return true;
		case R.id.menu_form_builder_add:
			// Add new question to bottom of the list and load new QuestionBuilder
			Question q = new Question(Question.SCALE, "");
			mQuestionList.add(q);
			((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
			setListSelected(mQuestionList.indexOf(q));
			edit(mListSelected);
			return true;
		case R.id.menu_form_builder_preview:
			// Build and preview a questionnaire
			saveToFile();
			Intent i = new Intent(this, Questionnaire.class);
			i.putExtra(Keys.INTENT_PREVIEW, true);
			startActivityForResult(i, REQUEST_PREVIEW);
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
		// Open SharedPrefences editor
		SharedPreferences.Editor editor = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE).edit();

		// Mark to remove all previous entries.
		editor.clear();

		// For each question, load into editor
		for (int i = 0; i < mQuestionList.size(); i++) {
			Question q = mQuestionList.get(i);
			editor.putString(Keys.QUES_TEXT + i, q.getQuestionStr()).putInt(Keys.QUES_TYPE + i, q.getInputType());
			if (q.getInputType() == Question.SCALE) {
				editor.putString(Keys.QUES_MIN + i, q.getMin()).putString(Keys.QUES_MAX + i, q.getMax());
			}
			if (DEBUG) Log.d(TAG, q.getQuestionStr() + ": " + q.getInputType());
		}

		if (mCommentFrag.getResult() == 1) {
			editor.putBoolean(Keys.COMMENT, true);
		}
		// Save changes
		editor.commit();

		backup();
	}

	@TargetApi(8)
	private void backup() {
		if (mBackup && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager.dataChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// Save when activity is destroyed. Ensures the list is kept on config changes.
		saveToFile();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PREVIEW:
			if (resultCode == Questionnaire.RESULT_DONE) {
				// Doctor happy with questionnaire, return to DoctorConfig
				setResult(RESULT_OK);
				finish();

			} else if (resultCode == Questionnaire.RESULT_BACK) {
				// Doctor not happy with questionnaire, stay here.

			}
			return;
		}
		// Not my request.
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onQuestionEdited(Question question) {
		mQuestionList.remove(mListSelected);
		mQuestionList.add(mListSelected, question);
		((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
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

}
