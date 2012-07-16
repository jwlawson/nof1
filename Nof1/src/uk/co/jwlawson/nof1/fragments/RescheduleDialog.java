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
package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.Keys;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.Scheduler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * @author John Lawson
 * 
 */
public class RescheduleDialog extends SherlockDialogFragment {

	private static final String TAG = "RescheduleDialog";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	public RescheduleDialog() {
	}

	public static RescheduleDialog newInstance() {
		RescheduleDialog frag = new RescheduleDialog();

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View view = getLayoutInflater(savedInstanceState).inflate(R.layout.dialog_reminder_entry, null, false);

		final Spinner spin = (Spinner) view.findViewById(R.id.reminder_dialog_spinner);

		builder.setView(view);
		builder.setTitle(R.string.reschedule);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (DEBUG) Log.d(TAG, "Rescheduling in " + spin.getSelectedItem());
				// Spinner options are 10 mins, 30mins, 1, 2, or 4 hours
				int mins = 0;
				switch (spin.getSelectedItemPosition()) {
				case 0:
					mins = 10;
					break;
				case 1:
					mins = 30;
					break;
				case 2:
					mins = 60;
					break;
				case 3:
					mins = 120;
					break;
				case 4:
					mins = 240;
					break;
				}
				Intent intent = new Intent(getActivity(), Scheduler.class);
				intent.putExtra(Keys.INTENT_RESCHEDULE, mins);
				getActivity().startService(intent);
			}
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		return builder.create();

	}
}
