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
package org.nof1trial.nof1.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nof1trial.nof1.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.util.Linkify;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Simple About screen containing license information, contact details etc
 * 
 * @author John Lawson
 * 
 */
public class About extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		TextView details = (TextView) findViewById(R.id.about_details);
		details.setLinkTextColor(0xff33b5e5);
		Linkify.addLinks(details, Linkify.ALL);

		Linkify.TransformFilter emptyFilter = new Linkify.TransformFilter() {
			@Override
			public String transformUrl(Matcher match, String url) {
				return "";
			}
		};

		Linkify.MatchFilter emptyMatch = new Linkify.MatchFilter() {
			@Override
			public boolean acceptMatch(CharSequence s, int start, int end) {
				return true;
			}
		};

		Pattern ABSMatcher = Pattern.compile("ActionBarSherlock");
		String urlABS = "http://actionbarsherlock.com/";
		Linkify.addLinks(details, ABSMatcher, urlABS, null, emptyFilter);

		Pattern apacheMatcher = Pattern.compile("Apache Commons Codec");
		String urlApache = "http://commons.apache.org/codec/index.html";
		Linkify.addLinks(details, apacheMatcher, urlApache, emptyMatch, emptyFilter);

		Pattern acraMatcher = Pattern.compile("ACRA crash reporting");
		String urlAcra = "http://code.google.com/p/acra";
		Linkify.addLinks(details, acraMatcher, urlAcra, emptyMatch, emptyFilter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = new Intent(this, HomeScreen.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so create a new task
				// with a synthesised back stack.
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
