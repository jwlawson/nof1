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
package org.nof1trial.nof1;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

/**
 * Application. Required to set up ACRA.
 * 
 * @author John Lawson
 * 
 */
// @formatter:off
@ReportsCrashes(formKey = "dFVwWmhnY3A0TFM1VnNhbVFTaXFfaUE6MQ") 
// @formatter:on
public class App extends Application {

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}

}
