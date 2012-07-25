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

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

/**
 * List which highlights the latest selected item. Clicks are passed on to an interface.
 * 
 * @author John Lawson
 * 
 */
public class FormBuilderList extends FragList {

	private static final String TAG = "FormBuilderList";
	private static final boolean DEBUG = false;

	public FormBuilderList() {
		if (DEBUG) Log.d(TAG, "New FormBuilderList created");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (DEBUG) Log.d(TAG, "Activity created and list adapter set");
	}

}
