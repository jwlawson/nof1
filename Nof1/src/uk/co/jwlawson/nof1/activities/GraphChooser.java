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
import uk.co.jwlawson.nof1.fragments.FragList;
import uk.co.jwlawson.nof1.fragments.GraphList;
import uk.co.jwlawson.nof1.fragments.GraphViewer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * @author John Lawson
 * 
 */
public class GraphChooser extends SherlockFragmentActivity implements GraphList.OnListItemSelectedListener {

	private static final String TAG = "Graph Chooser";
	private static final boolean DEBUG = false;

	private boolean mDual;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.graph_chooser);

		setSupportProgressBarIndeterminateVisibility(false);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		FrameLayout frame = (FrameLayout) findViewById(R.id.graph_chooser_view);

		if (frame != null) {

			mDual = true;

			GraphViewer viewer = GraphViewer.newInstance(0);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.graph_chooser_view, viewer, "viewer");
			ft.commit();

			GraphList list = (GraphList) getSupportFragmentManager().findFragmentById(R.id.graph_chooser_list);
			list.setSelection(0);

			if (DEBUG) Log.d(TAG, "Created in dual pane mode");

		} else {
			mDual = false;
			if (DEBUG) Log.d(TAG, "Created in single pane mode");
		}
	}

	@Override
	public void onListItemSelected(ListView l, View v, int position, long id) {
		if (DEBUG) Log.d(TAG, "Item clicked: " + position);

		if (mDual) {
			// load graph into framelayout

			GraphViewer viewer = (GraphViewer) getSupportFragmentManager().findFragmentByTag("viewer");
			if (viewer.getQuestionId() != position) {
				// Make new viewer for selected question
				viewer = GraphViewer.newInstance(position);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.graph_chooser_view, viewer, "viewer");
				ft.commit();
			}
		} else {
			// Clear list selection
			((FragList) getSupportFragmentManager().findFragmentById(R.id.graph_chooser_list)).clearSelected();
			// Launch new activity
			Intent intent = new Intent(this, GraphDisplay.class);
			intent.putExtra(Keys.INTENT_ID, position);
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (DEBUG) Log.d(TAG, "Home button selected");
			Intent upIntent = new Intent(this, HomeScreen.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so create a new task
				// with a synthesized back stack.
				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
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
