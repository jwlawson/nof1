package uk.co.jwlawson.nof1.activities;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.CommentDetailFragment;
import uk.co.jwlawson.nof1.fragments.CommentListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class CommentList extends SherlockFragmentActivity implements CommentListFragment.OnListItemSelectedListener,
		CommentListFragment.OnListItemAddedListener {
	
	private boolean mTwoPane;
	
	private ArrayList<String> mList;
	
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
		
		if (mTwoPane) {
			CommentDetailFragment frag = CommentDetailFragment.newInstance(mList.get(position));
			getSupportFragmentManager().beginTransaction().replace(R.id.comment_detail_container, frag).commit();
			
		} else {
			Intent detailIntent = new Intent(this, CommentDetail.class);
			detailIntent.putExtra(CommentDetailFragment.ARG_COMMENT, mList.get(position));
			startActivity(detailIntent);
		}
	}
	
	@Override
	public void onListItemAdded(String comment, String date) {
		mList.add(comment);
	}
}
