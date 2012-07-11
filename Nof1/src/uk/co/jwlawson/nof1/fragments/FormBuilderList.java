/*******************************************************************************
 * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package uk.co.jwlawson.nof1.fragments;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.containers.Question;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * List which highlights the latest selected item. Clicks are passed on to an interface.
 * 
 * @author John Lawson
 * 
 */
public class FormBuilderList extends SherlockListFragment {

	private static final String TAG = "FormBuilderList";
	private static final boolean DEBUG = true;

	private OnListItemSelectedListener mListener;
	private View mSelectedView = null;

	public FormBuilderList() {
		if (DEBUG) Log.d(TAG, "New FormBuilderList created");
	}

	public interface OnListItemSelectedListener {

		/**
		 * 
		 * @param l The ListView where the click happened
		 * @param v The view that was clicked within the ListView
		 * @param position The position of the view in the list
		 * @param id The row id of the item that was clicked
		 */
		public void onListItemSelected(ListView l, View v, int position, long id);
	}

	public void setArrayList(ArrayList<Question> list) {
		ArrayAdapter<Question> adapter = new ArrayAdapter<Question>(getActivity(), android.R.layout.simple_expandable_list_item_1,
				android.R.id.text1, list);
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (DEBUG) Log.d(TAG, "Activity created and list adapter set");
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (DEBUG) Log.d(TAG, "Item selected " + position);

		// Clear previously clicked view and colour this clicked view
		clearSelected();
		mSelectedView = v;
		mSelectedView.setBackgroundColor(0xFF33B5E5);
		getListView().setItemChecked(position, true);

		// Pass click on to activity
		mListener.onListItemSelected(l, v, position, id);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Check that the activity implements the interface
		try {
			mListener = (OnListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnListItemSelectedListener");
		}
	}

	/**
	 * Function to clear the background colour of the selected view. Use when a
	 * different view is selected.
	 */
	public void clearSelected() {
		if (DEBUG) Log.d(TAG, "Clearing selection");
		ListView lv = getListView();
		for (int i = 0; i < lv.getChildCount(); i++) {
			lv.getChildAt(i).setBackgroundResource(0);
		}
	}

}
