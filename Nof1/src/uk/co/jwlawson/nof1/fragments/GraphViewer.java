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

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.DataSource;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.views.GraphView;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment to show a graph. Loads data from database and passes to GraphView
 * 
 * @author John Lawson
 * 
 */
public class GraphViewer extends SherlockFragment {

	private static final String TAG = "GraphViewer fragment";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	private static final String ARGS_ID = "id";

	private GraphView mGraph;

	private DataSource mData;

	private FrameLayout mFrame;

	private Cursor mCursor;

	public GraphViewer() {
	}

	public int getQuestionId() {
		return getArguments().getInt(ARGS_ID);
	}

	public static GraphViewer newInstance(int questionId) {
		GraphViewer frag = new GraphViewer();

		Bundle args = new Bundle();
		args.putInt(ARGS_ID, questionId);

		frag.setArguments(args);

		if (DEBUG) Log.d(TAG, "New graphview instanced: " + questionId);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialise data source
		new DataLoader().execute();

		if (DEBUG) Log.d(TAG, "Fragment created");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO create layout for GraphViewer, so can include titles, more info etc

		if (getArguments() == null) {
			Log.w(TAG, "GraphView created with null arguments");

		} else {

			mFrame = new FrameLayout(getActivity());

		}

		if (DEBUG) Log.d(TAG, "View created");
		return mFrame;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (DEBUG) Log.d(TAG, "Data source loading");
			mData = new DataSource(getActivity());
			mData.open();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (DEBUG) Log.d(TAG, "Data source loaded");
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);

			// Load graph View
			mGraph = new GraphView(getActivity());

			// Load data from database and pass to GraphView
			int id = getArguments().getInt(ARGS_ID);
			mCursor = mData.getQuestion(id);

			// Set max x
			SharedPreferences sp = getActivity().getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE);
			int maxx = 2 * sp.getInt(Keys.CONFIG_NUMBER_PERIODS, 5) * sp.getInt(Keys.CONFIG_PERIOD_LENGTH, 5);
			mGraph.setMaxX(maxx);
			if (DEBUG) Log.d(TAG, "Setting max X: " + maxx);

			// Set max y
			int max = 0;
			while (!mCursor.isAfterLast()) {
				if (mCursor.getInt(1) > max) {
					max = mCursor.getInt(1);
				}
				mCursor.moveToNext();
			}
			mGraph.setMaxY(max);
			if (DEBUG) Log.d(TAG, "Setting max y: " + max);

			// Add vertical spacing
			int sep = sp.getInt(Keys.CONFIG_PERIOD_LENGTH, 0);
			if (sep != 0) {
				mGraph.setVerticalLines(sep);
				if (DEBUG) Log.d(TAG, "Setting vertical spacers: " + sep);
			}

			if (DEBUG) mGraph.setVerticalShading(null);

			// Check whether the trial is finished
			SharedPreferences schedPrefs = getActivity().getSharedPreferences(Keys.SCHED_NAME, Context.MODE_PRIVATE);
			if (schedPrefs.getBoolean(Keys.SCHED_FINISHED, false)) {
				// TODO Trial finished, so shade in regions for treatment b

			}

			// Set cursor
			mGraph.setCursor(mCursor);

			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

			mFrame.addView(mGraph, params);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mData.close();
		mCursor.close();
	}
}
