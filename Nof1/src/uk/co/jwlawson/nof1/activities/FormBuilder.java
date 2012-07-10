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
package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.R;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author John Lawson
 * 
 */
public class FormBuilder extends SherlockFragmentActivity {

	private static final String TAG = "FormBuilder";
	private static final boolean DEBUG = true;
	private static final int THEME = R.style.dialog_theme;

	public FormBuilder() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.form_builder_menu, menu);
		return true;

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_form_builder_add:
			return true;

		}
		// None of my menu items
		return super.onMenuItemSelected(featureId, item);
	}

	private LayoutInflater getInflater() {
		LayoutInflater inflater;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// None Holo version, so dark alertdialog
			inflater = (LayoutInflater) getApplicationContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		} else {
			// Shiny holo light available
			inflater = getLayoutInflater();

		}
		return inflater;
	}

	/**
	 * ActionMode class to show when a list item is selected. Provides more
	 * options specific for that item.
	 */
	private class MyActionMode implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getSupportMenuInflater().inflate(R.menu.form_builder_action_mode, menu);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
			case R.id.action_mode_form_builder_edit:
				break;
			case R.id.action_mode_form_builder_delete:
				break;

			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

	}
}
