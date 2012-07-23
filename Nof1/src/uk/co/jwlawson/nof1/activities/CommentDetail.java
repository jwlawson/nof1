package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.CommentDetailFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class CommentDetail extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_detail);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState == null) {
			String comment = getIntent().getStringExtra(CommentDetailFragment.ARG_COMMENT);
			CommentDetailFragment fragment = CommentDetailFragment.newInstance(comment);
			getSupportFragmentManager().beginTransaction().add(R.id.comment_detail_container, fragment).commit();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpTo(this, new Intent(this, CommentList.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
