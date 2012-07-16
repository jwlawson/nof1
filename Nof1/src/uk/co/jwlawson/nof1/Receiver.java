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
package uk.co.jwlawson.nof1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Gets the Boot_complete broadcast and starts up Scheduler service
 * 
 * @author John Lawson
 * 
 */
public class Receiver extends BroadcastReceiver {

	private static final String TAG = "Receiver";
	private static final boolean DEBUG = true && BuildConfig.DEBUG;

	/**
	 * 
	 */
	public Receiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DEBUG) Log.d(TAG, "Boot_complete broadcast caught");

		Intent i = new Intent(context, Scheduler.class);
		i.putExtra(Keys.INTENT_BOOT, true);
		context.startService(i);
	}

}
