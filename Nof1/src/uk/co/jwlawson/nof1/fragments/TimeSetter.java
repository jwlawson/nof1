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

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment that holds a number of times. Allows adding more times and editing existing times.
 * 
 * @author John Lawson
 * 
 */
public class TimeSetter extends SherlockFragment {

	private int mNumber = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.config_doctor_medicine_timing, container, false);

		Button add = (Button) view.findViewById(R.id.config_doctor_medicine_timing_add);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Make new time, add to layout and launch time picker dialog
				TimeHolder frag = TimeHolder.newInstance("12:00");

				FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				ft.add(R.id.config_doctor_medicine_timing_layout, frag, "time" + mNumber);
				mNumber++;
				ft.commit();
			}
		});

		Button remove = (Button) view.findViewById(R.id.config_doctor_medicine_timing_remove);
		remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNumber < 1) return;
				FragmentManager fm = getSherlockActivity().getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.remove(fm.findFragmentByTag("time" + (mNumber - 1)));
				ft.commit();
				mNumber--;
			}
		});

		return view;
	}

	public String[] getTimes() {
		String[] result = new String[mNumber];

		FragmentManager fm = getSherlockActivity().getSupportFragmentManager();
		for (int i = 0; i < mNumber; i++) {
			result[i] = ((TimeHolder) fm.findFragmentByTag("time" + i)).getTime();
		}

		return result;
	}
}
