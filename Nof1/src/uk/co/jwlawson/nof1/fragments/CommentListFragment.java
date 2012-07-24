package uk.co.jwlawson.nof1.fragments;

import java.util.ArrayList;
import java.util.Calendar;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.SQLite;
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
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

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

			int calDay = 0;

			// load data from cursor
			while (!mCursor.isAfterLast()) {
				int day = mCursor.getInt(dayCol);
				String comment = mCursor.getString(comCol);
				if (comment.length() != 0) {
					cal.add(Calendar.DAY_OF_MONTH, day - calDay);

					String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
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
