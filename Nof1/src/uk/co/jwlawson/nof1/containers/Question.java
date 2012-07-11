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
package uk.co.jwlawson.nof1.containers;

/**
 * @author John Lawson
 * 
 */
public class Question {
	
	private static final int SCALE = 0;
	private static final int NUMBER = 1;
	private static final int CHECK = 2;
	
	private int mInputType;
	private String mQuestionStr;
	private String mMin;
	private String mMax;
	
	/**
	 * @param inputType The type of input for the Question. SCALE, NUMBER or CHECK.
	 * @param question The text of the question.
	 */
	public Question(int inputType, String question) {
		if (inputType < SCALE || inputType > CHECK) {
			throw new IllegalArgumentException("InputType should be one of SCALE, NUMBER or CHECK");
		}
		mInputType = inputType;
		mQuestionStr = question;
	}
	
	/**
	 * Set the max and min hints for those questions of type SCALE. Throws UnsupportedOperationException if the question is not SCALE.
	 * 
	 * @param min
	 * @param max
	 */
	public void setMinMax(String min, String max) {
		if (mInputType != SCALE) {
			throw new UnsupportedOperationException("Min and max can only be set for questions of input type SCALE");
		}
		mMin = min;
		mMax = max;
	}
	
	public String getQuestionStr() {
		return mQuestionStr;
	}
	
	public void setQuestionStr(String questionStr) {
		mQuestionStr = questionStr;
	}
	
	public String getMin() {
		return mMin;
	}
	
	public String getMax() {
		return mMax;
	}
}
