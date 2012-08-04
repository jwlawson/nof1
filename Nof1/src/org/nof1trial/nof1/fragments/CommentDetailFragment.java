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

import org.nof1trial.nof1.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CommentDetailFragment extends SherlockFragment {
	
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_COMMENT = "comment";
	
	public CommentDetailFragment() {
	}
	
	public static CommentDetailFragment newInstance(String data) {
		CommentDetailFragment frag = new CommentDetailFragment();
		
		Bundle args = new Bundle();
		args.putString(ARG_COMMENT, data);
		
		frag.setArguments(args);
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_comment_detail, container, false);
		
		Bundle args = getArguments();
		if (args != null) {
			((TextView) rootView.findViewById(R.id.comment_detail)).setText(args.getString(ARG_COMMENT));
		}
		return rootView;
	}
}
