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
package org.nof1trial.nof1.app;

import org.nof1trial.nof1.shared.ConfigProxy;
import org.nof1trial.nof1.shared.ConfigRequest;
import org.nof1trial.nof1.shared.MyRequestFactory;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * @author John Lawson
 * 
 */
public class Saver extends IntentService {

	/**
	 * @param name
	 */
	public Saver(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				MyRequestFactory factory = (MyRequestFactory) Util.getRequestFactory(Saver.this, MyRequestFactory.class);
				ConfigRequest request = factory.configRequest();

				ConfigProxy conf = request.create(ConfigProxy.class);

				request.update(conf).fire();

				return null;
			}

		}.execute();
	}

}
