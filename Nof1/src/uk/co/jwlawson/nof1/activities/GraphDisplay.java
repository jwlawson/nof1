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

import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.GraphViewer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * @author John Lawson
 * 
 */
public class GraphDisplay extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		FrameLayout frame = new FrameLayout(this);
		frame.setId(R.id.graph_chooser_view);
		setContentView(frame);

		Intent intent = getIntent();
		int questionId = intent.getIntExtra(Keys.INTENT_ID, 0);

		SharedPreferences sp = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
		setTitle(sp.getString(Keys.QUES_TEXT + questionId, ""));

		if (savedInstanceState == null) {

			GraphViewer viewer = GraphViewer.newInstance(questionId);
			getSupportFragmentManager().beginTransaction().add(R.id.graph_chooser_view, viewer, "viewer").commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = new Intent(this, GraphChooser.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so create a new task
				// with a synthesized back stack.
				TaskStackBuilder.create(this).addNextIntent(new Intent(this, HomeScreen.class)).addNextIntent(upIntent).startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
