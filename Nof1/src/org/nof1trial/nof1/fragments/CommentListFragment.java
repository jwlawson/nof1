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
import java.util.Calendar;

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.SQLite;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CommentListFragment extends FragList {
	
	private static final String TAG = "CommentListFragment";
	private static final boolean DEBUG = false;
	
	private Cursor mCursor;
	
	private OnListItemAddedListener mListener;
	
	public interface OnListItemAddedListener {
		
		/**
		 * Called when a list item is added to the adapter
		 * 
		 * @param comment The comment added
		 * @param date The date when the comment was submitted
		 */
		public void onListItemAdded(String comment, String date);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (OnListItemAddedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass().getName() + " must implement OnListItemAddedListener");
		}
	}
	
	public void setCursor(Cursor cursor) {
		
		mCursor = cursor;
		
		ArrayList<String> list = new ArrayList<String>();
		setArrayList(list);
		
		new Loader().execute();
	}
	
	@Override
	public void onDestroy() {
		if (mCursor != null) mCursor.close();
		super.onDestroy();
	}
	
	private class Loader extends AsyncTask<Void, String, Void> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if (DEBUG) Log.d(TAG, "Loading values from cursor");
			
			mCursor.moveToFirst();
			
			SharedPreferences sp = getActivity().getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE);
			
			// Get start date
			String[] start = sp.getString(Keys.CONFIG_START, "").split(":");
			int[] startArr = new int[] { Integer.parseInt(start[0]), Integer.parseInt(start[1]), Integer.parseInt(start[2]) };
			
			// Get start date calendar
			Calendar cal = Calendar.getInstance();
			cal.set(startArr[2], startArr[1], startArr[0], 12, 00);
			
			// get column ids
			int dayCol = mCursor.getColumnIndexOrThrow(SQLite.COLUMN_DAY);
			int comCol = mCursor.getColumnIndexOrThrow(SQLite.COLUMN_COMMENT);
			int timeCol = mCursor.getColumnIndex(SQLite.COLUMN_TIME);
			
			int calDay = 1;
			
			// load data from cursor
			while (!mCursor.isAfterLast()) {
				int day = mCursor.getInt(dayCol);
				String comment = mCursor.getString(comCol);
				if (comment != null && comment.length() != 0) {
					cal.add(Calendar.DAY_OF_MONTH, day - calDay);
					
					// Ensure that for minutes less than 10 they have prefix of 0
					String[] time = mCursor.getString(timeCol).split(":");
					int mins = Integer.parseInt(time[1]);
					if (mins < 10) {
						time[1] = "0" + mins;
					}
					
					String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) + " " + time[0] + ":"
							+ time[1];
					mListener.onListItemAdded(comment, date);
					publishProgress(date);
					
					calDay = day;
				}
				mCursor.moveToNext();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			// Add data to list adapter
			ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
			for (int i = 0; i < values.length; i++) {
				adapter.add(values[i]);
			}
			adapter.notifyDataSetChanged();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			if (DEBUG) Log.d(TAG, "Data loaded");
		}
		
	}
}
