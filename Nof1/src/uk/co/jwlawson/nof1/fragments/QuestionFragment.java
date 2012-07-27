/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson, WMG, University of Warwick
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

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Abstract class to hold any question for data collection. The question should have an integer for an answer (or
 * boolean mapped to 0 / 1 )
 * 
 * @author John
 * 
 */
public abstract class QuestionFragment extends SherlockFragment {

	public QuestionFragment() {
	}

	/**
	 * Class questions any UI element and return answer to question.
	 * 
	 * @return Answer to the fragment's question
	 */
	public abstract int getResult();

}
