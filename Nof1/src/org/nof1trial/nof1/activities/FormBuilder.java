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
package org.nof1trial.nof1.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.containers.Question;
import org.nof1trial.nof1.fragments.CheckFragment;
import org.nof1trial.nof1.fragments.FormBuilderList;
import org.nof1trial.nof1.fragments.QuestionBuilderDialog;
import org.nof1trial.nof1.fragments.SampleQuestionDialog;
import org.nof1trial.nof1.services.Downloader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Lawson
 * 
 */
public class FormBuilder extends SherlockFragmentActivity implements
		FormBuilderList.OnListItemSelectedListener, QuestionBuilderDialog.OnQuestionEditedListener,
		SampleQuestionDialog.OnSamplesCheckedListener {

	private static final String TAG = "FormBuilder";
	private static final boolean DEBUG = false;

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
	private final ArrayList<Question> mQuestionList;

	/** Currently selected or last instanced QuestionBuilder */
	private QuestionBuilderDialog mQuestionBuilder;

	/** CheckFragment asking whether to show a comment box */
	private CheckFragment mCommentFrag;

	/** Instance of backup manager */
	private BackupManager mBackupManager;

	private final Context mContext = this;

	private ProgressDialog dialog;

	@TargetApi(8)
	public FormBuilder() {
		mQuestionList = new ArrayList<Question>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager = new BackupManager(mContext);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.form_builder_list);
		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);

		mList = (FormBuilderList) getSupportFragmentManager().findFragmentById(
				R.id.form_builder_list_fragment);

		if (!sp.contains(Keys.QUES_TEXT + 0)) {
			// No saved questions
			SampleQuestionDialog samples = SampleQuestionDialog.newInstance();
			samples.show(getSupportFragmentManager(), "samples");
		}

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

		mCommentFrag = (CheckFragment) getSupportFragmentManager().findFragmentById(
				R.id.form_builder_check_fragment);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.form_builder_menu, menu);
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_form_builder_done:
			// User happy with questionnaire
			setResult(RESULT_OK);
			finish();
			return true;
		case R.id.menu_form_builder_add:
			// Add new question to bottom of the list and load new
			// QuestionBuilder
			Question q = new Question(Question.SCALE, "");
			mQuestionList.add(q);
			((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
			setListSelected(mQuestionList.indexOf(q));
			edit(mListSelected);
			return true;
		case R.id.menu_form_builder_preview:
			// Build and preview a questionnaire
			saveToFile();
			Intent i = new Intent(mContext, Questionnaire.class);
			i.putExtra(Keys.INTENT_PREVIEW, true);
			startActivityForResult(i, REQUEST_PREVIEW);
			return true;
		case R.id.menu_form_builder_samples:
			// Show samples dialog fragment
			SampleQuestionDialog frag = SampleQuestionDialog.newInstance();
			frag.show(getSupportFragmentManager(), "samples");
			return true;
		case android.R.id.home:
			if (DEBUG) Log.d(TAG, "Home button pressed");
			Intent upIntent = new Intent(mContext, DoctorConfig.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so
				// create a new task
				// with a synthesized back stack.
				TaskStackBuilder.create(mContext)
						.addNextIntent(new Intent(mContext, HomeScreen.class))
						.addNextIntent(new Intent(mContext, UserPrefs.class))
						.addNextIntent(upIntent).startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
		default:
			return super.onOptionsItemSelected(item);
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

	/** Set the currently selected item. Also sets it in ListFragment */
	private void setListSelected(int listSelected) {
		mListSelected = listSelected;
		if (mList != null) {
			mList.setSelection(listSelected);
		}

	}

	/** Show the selected question in some editable form */
	private void edit(int selection) {
		QuestionBuilderDialog q;
		if (mDualPane) {

			q = QuestionBuilderDialog.newInstance(QuestionBuilderDialog.VIEW,
					mQuestionList.get(selection));
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.form_builder_details_frame, q, "view");
			ft.commit();

		} else {
			q = QuestionBuilderDialog.newInstance(QuestionBuilderDialog.DIALOG,
					mQuestionList.get(selection));

			// Shows fragment as dialog
			q.show(getSupportFragmentManager(), "dialog");
		}
		mQuestionBuilder = q;
	}

	private void saveToFile() {
		// Get previous number of questions
		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
		int numQues = sp.getInt(Keys.QUES_NUMBER_QUESTIONS, 0);

		SharedPreferences.Editor editor = sp.edit();

		// Mark to remove all previous entries.
		editor.clear();

		// For each question, load into editor
		for (int i = 0; i < mQuestionList.size(); i++) {
			Question q = mQuestionList.get(i);
			editor.putString(Keys.QUES_TEXT + i, q.getQuestionStr()).putInt(Keys.QUES_TYPE + i,
					q.getInputType());
			if (q.getInputType() == Question.SCALE) {
				editor.putString(Keys.QUES_MIN + i, q.getMin()).putString(Keys.QUES_MAX + i,
						q.getMax());
			}
			if (DEBUG) Log.d(TAG, q.getQuestionStr() + ": " + q.getInputType());
		}

		editor.putInt(Keys.QUES_NUMBER_QUESTIONS, mQuestionList.size());

		if (mCommentFrag.getResult() == 1) {
			editor.putBoolean(Keys.COMMENT, true);
		}
		// Save changes
		editor.commit();

		// If questions added, tell SQLite the database needs updating
		if (mQuestionList.size() > numQues) {
			SharedPreferences config = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
			// Increase database version by 1
			int dbVersion = config.getInt(Keys.CONFIG_DB_VERSION, 1);
			config.edit().putInt(Keys.CONFIG_DB_VERSION, dbVersion + 1).commit();
		}

		backup();
	}

	@TargetApi(8)
	private void backup() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mBackupManager.dataChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// Save when activity is destroyed. Ensures the list is kept on config
		// changes.
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

	@SuppressWarnings("rawtypes")
	@Override
	public void onSampleChecked(boolean[] checked) {
		String[] questions = getResources().getStringArray(R.array.sample_questions);
		int[] types = getResources().getIntArray(R.array.sample_ques_types);
		String[] min = getResources().getStringArray(R.array.sample_ques_min);
		String[] max = getResources().getStringArray(R.array.sample_ques_max);

		for (int i = 0; i < checked.length; i++) {
			if (checked[i]) {
				// Add question
				Question q = new Question(types[i], questions[i]);
				if (types[i] == Question.SCALE) {
					q.setMinMax(min[i], max[i]);
				}
				mQuestionList.add(q);
			}
		}
		((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void downloadQuestions(int id) {
		if (isConnected()) {

			showProgressDialog();

			launchDownloaderService(id);

			registerCallbackReceiver();

		} else {
			Toast.makeText(mContext, R.string.download_not_connected, Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());
		return isConnected;
	}

	private void showProgressDialog() {
		dialog = new ProgressDialog(mContext);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setMessage(getText(R.string.download_progress));
		dialog.show();
	}

	private void launchDownloaderService(int id) {
		Intent downloader = new Intent(mContext, Downloader.class);
		downloader.setAction(Keys.ACTION_DOWNLOAD_QUES);
		downloader.putExtra(Keys.INTENT_ID, id);
		startService(downloader);
	}

	private void registerCallbackReceiver() {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
		manager.registerReceiver(new QuesReceiver(),
				new IntentFilter(Keys.ACTION_DOWNLOAD_COMPLETE));
	}

	private class QuesReceiver extends BroadcastReceiver {

		@SuppressWarnings("rawtypes")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Keys.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
				List<String> questions = intent.getStringArrayListExtra(Keys.INTENT_QUESTIONS);
				List<String> mins = intent.getStringArrayListExtra(Keys.INTENT_MINS);
				List<String> maxs = intent.getStringArrayListExtra(Keys.INTENT_MAXS);
				List<Integer> types = intent.getIntegerArrayListExtra(Keys.INTENT_TYPES);

				for (int i = 0; i < questions.size(); i++) {
					Question q = new Question(types.get(i), questions.get(i));
					if (types.get(i) == Question.SCALE) {
						q.setMinMax(mins.get(i), maxs.get(i));
					}
					if (mQuestionList != null) {
						mQuestionList.add(q);
					}
				}
				if (mList != null) {
					((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
				}
				if (dialog != null) {
					dialog.dismiss();
				}
			}

			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
			manager.unregisterReceiver(this);
		}

	}

}
