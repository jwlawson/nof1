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

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.DataSource;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.SQLite;
import uk.co.jwlawson.nof1.fragments.CommentDetailFragment;
import uk.co.jwlawson.nof1.fragments.CommentListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class CommentList extends SherlockFragmentActivity implements CommentListFragment.OnListItemSelectedListener,
		CommentListFragment.OnListItemAddedListener {

	private static final String TAG = "CommentList";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	/** True if on device wide enough for two panels */
	private boolean mTwoPane;

	/** List of all comments as strings */
	private ArrayList<String> mList;

	/** Database handler */
	private DataSource mData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);

		mList = new ArrayList<String>();

		if (findViewById(R.id.comment_detail_container) != null) {
			mTwoPane = true;
		}

		new Loader().execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemSelected(ListView l, View v, int position, long id) {
		if (DEBUG) Log.d(TAG, "List item selected: " + position);

		if (mTwoPane) {
			// Replace comment details fragment with new one
			// TODO Check whether we need to replace it
			CommentDetailFragment frag = CommentDetailFragment.newInstance(mList.get(position));
			getSupportFragmentManager().beginTransaction().replace(R.id.comment_detail_container, frag).commit();

		} else {
			// Clear selection from carrying on when return from new actiity
			((CommentListFragment) getSupportFragmentManager().findFragmentById(R.id.comment_list)).clearSelected();

			// Get text
			TextView text = (TextView) v.findViewById(android.R.id.text1);

			// Load new activity
			Intent detailIntent = new Intent(this, CommentDetail.class);
			detailIntent.putExtra(CommentDetailFragment.ARG_COMMENT, mList.get(position));
			detailIntent.putExtra("title", text.getText());
			startActivity(detailIntent);
		}
	}

	@Override
	public void onListItemAdded(String comment, String date) {
		mList.add(comment);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure database is closed if open
		if (mData != null) mData.close();
	}

	private class Loader extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			if (DEBUG) Log.d(TAG, "Loading cursor");

			// Open database
			mData = new DataSource(CommentList.this);
			mData.open();

			// Query database
			Cursor cursor = mData.getColumns(new String[] { SQLite.COLUMN_DAY, SQLite.COLUMN_TIME, SQLite.COLUMN_COMMENT });
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			// Set list cursor
			((CommentListFragment) getSupportFragmentManager().findFragmentById(R.id.comment_list)).setCursor(result);

			super.onPostExecute(result);
		}
	}
}
