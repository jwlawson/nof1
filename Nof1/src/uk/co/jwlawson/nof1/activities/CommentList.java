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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class CommentList extends SherlockFragmentActivity implements CommentListFragment.OnListItemSelectedListener, CommentListFragment.OnListItemAddedListener {
	
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
			
			// Load new activity
			Intent detailIntent = new Intent(this, CommentDetail.class);
			detailIntent.putExtra(CommentDetailFragment.ARG_COMMENT, mList.get(position));
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
			
			// Quesry database
			Cursor cursor = mData.getColumn(SQLite.COLUMN_COMMENT);
			
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
