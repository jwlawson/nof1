/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  John Lawson
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
package org.nof1trial.nof1.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import org.nof1trial.nof1.BuildConfig;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.MyRequestFactory;
import org.nof1trial.nof1.shared.QuestionnaireProxy;
import org.nof1trial.nof1.shared.QuestionnaireRequest;

import java.util.ArrayList;

/**
 * @author John
 * 
 */
public class Downloader extends IntentService {

	private static final String TAG = "Downloader";
	private static final boolean DEBUG = BuildConfig.DEBUG;

	private static final String CACHE = "config";
	private static final String CACHEDID = "cachedid";

	private final Context mContext = this;

	public Downloader() {
		super("Downloader");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (Keys.ACTION_DOWNLOAD_QUES.equals(intent.getAction())) {
			int id = intent.getIntExtra(Keys.INTENT_ID, 0);
			if (id != 0) {
				downloadIfConnected(id);
			}
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			int id = getSharedPreferences(CACHE, MODE_PRIVATE).getInt(CACHEDID, 0);
			if (id != 0) {
				downloadIfConnected(id);
			}
		} else {
			if (DEBUG) Log.d(TAG, "Downloader started with invalid action");
		}
	}

	private void downloadIfConnected(final long id) {

		if (isConnected()) {
			MyRequestFactory factory = Util.getRequestFactory(mContext, MyRequestFactory.class);
			QuestionnaireRequest request = factory.questionnaireRequest();
			if (DEBUG) Log.d(TAG, "Downloading questionnaire " + id);

			request.findQuestionnaire(id).fire(new Receiver<QuestionnaireProxy>() {

				@Override
				public void onSuccess(QuestionnaireProxy response) {
					if (DEBUG) Log.d(TAG, "Questionnaire downloaded successfuly");
					ArrayList<String> questions = new ArrayList<String>();
					questions.addAll(response.getQuestionList());

					ArrayList<String> mins = new ArrayList<String>();
					mins.addAll(response.getMinList());

					ArrayList<String> maxs = new ArrayList<String>();
					maxs.addAll(response.getMaxList());

					ArrayList<Integer> types = new ArrayList<Integer>();
					types.addAll(response.getTypeList());

					Intent callback = new Intent(Keys.ACTION_DOWNLOAD_COMPLETE);
					callback.putStringArrayListExtra(Keys.INTENT_QUESTIONS, questions);
					callback.putStringArrayListExtra(Keys.INTENT_MAXS, maxs);
					callback.putStringArrayListExtra(Keys.INTENT_MINS, mins);
					callback.putIntegerArrayListExtra(Keys.INTENT_TYPES, types);

					LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
					manager.sendBroadcast(callback);
				}

				@Override
				public void onFailure(ServerFailure error) {
					Log.e(TAG, "Questionnaire not downloaded");
					Log.e(TAG, error.getMessage());

					// TODO Only refresh cookie when error is an auth error
					// TODO Only allow looping a certain number of times
					// Try refreshing auth cookie
					Intent intent = new Intent(mContext, AccountService.class);
					intent.setAction(Keys.ACTION_REFRESH);
					startService(intent);

					mContext.getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit()
							.putInt(CACHEDID, (int) id).commit();

					LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
					manager.registerReceiver(new CookieReceiver(), new IntentFilter(
							Keys.ACTION_COMPLETE));
				}
			});
		}
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = (activeNetwork == null ? false : activeNetwork.isConnected());
		return isConnected;
	}

	private class CookieReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) Log.d(TAG, "Received broadcast");

			Intent downloader = new Intent(context, Downloader.class);
			downloader.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
			context.startService(downloader);
			if (DEBUG) Log.d(TAG, "Downloader started from CookieReceiver");

			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
			manager.unregisterReceiver(this);
		}

	}

}
