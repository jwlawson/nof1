/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson
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
package org.nof1trial.nof1.fragments;

import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.R;
import org.nof1trial.nof1.services.Scheduler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * @author John Lawson
 * 
 */
public class RescheduleDialog extends SherlockDialogFragment {
	
	private static final String TAG = "RescheduleDialog";
	private static final boolean DEBUG = false;
	
	private static final String ARGS_SPINNER = TAG + "Spinner";
	
	private Spinner mSpinner;
	
	private OnRescheduleListener mListener;
	
	public RescheduleDialog() {
	}
	
	public interface OnRescheduleListener {
		
		public void onReschedule(boolean rescheduled);
	}
	
	public static RescheduleDialog newInstance() {
		RescheduleDialog frag = new RescheduleDialog();
		
		return frag;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnRescheduleListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass().getName() + " must implement OnRescheduleListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_reminder_entry, null, false);
		
		mSpinner = (Spinner) view.findViewById(R.id.reminder_dialog_spinner);
		if (savedInstanceState != null) {
			mSpinner.setSelection(savedInstanceState.getInt(ARGS_SPINNER));
		}
		
		builder.setView(view);
		builder.setTitle(R.string.schedule_reminder);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (DEBUG) Log.d(TAG, "Rescheduling in " + mSpinner.getSelectedItem());
				// Spinner options are 10 mins, 30mins, 1, 2, or 4 hours
				int mins = 0;
				switch (mSpinner.getSelectedItemPosition()) {
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
				
				mListener.onReschedule(true);
			}
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onReschedule(false);
				dialog.dismiss();
			}
		});
		
		return builder.create();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARGS_SPINNER, mSpinner.getSelectedItemPosition());
	}
}
