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
import uk.co.jwlawson.nof1.views.GraphView;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

	public GraphViewer() {

	}

	public static GraphViewer newInstance(int questionId) {
		GraphViewer frag = new GraphViewer();

		Bundle args = new Bundle();
		args.putInt(ARGS_ID, questionId);

		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialise data source
		new DataLoader().execute();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Load graph View
		mGraph = new GraphView(getActivity());

		Bundle args = getArguments();

		if (args == null) {
			Log.w(TAG, "GraphView created with null arguments");

		} else {

			int id = args.getInt(ARGS_ID);
			Cursor cursor = mData.getQuestion(id);
			mGraph.setCursor(cursor);
		}
		return mGraph;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mData = new DataSource(getActivity());
			mData.open();
			return null;
		}

	}
}
