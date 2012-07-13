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

/**
 * Container class holding various keys for loading SharedPreferences or extras from Bundles, Intents etc
 * 
 * @author John Lawson
 * 
 */
public class Keys {

	/** Name of default shared preferences */
	public static final String DEFAULT_PREFS = "uk.co.jwlawson.nof1_preferences";

	/** Name of shared preferences holding the questions */
	public static final String QUES_NAME = "questions";

	/** Shared preference key with question text. Append with required question number. */
	public static final String QUES_TEXT = "questionText";

	/** Shared preference key with question type. Append with required question number. */
	public static final String QUES_TYPE = "inputType";

	/** Shared preference key with minimum hint. Append with required question number. */
	public static final String QUES_MIN = "questionMin";

	/** Shared preference key with maximum hint. Append with required question number. */
	public static final String QUES_MAX = "questionMax";

	/** Shared preference key for comment bool. True = show comment */
	public static final String COMMENT = "comment";

	/** Intent key for preview mode */
	public static final String INTENT_PREVIEW = "preview";

	/** Intent key for email string */
	public static final String INTENT_EMAIL = "email";

	/** SharedPrefernces holding config data */
	public static final String CONFIG_NAME = "config";

	/** Shared preference key for whether the questionnaire is built */
	public static final String CONFIG_BUILT = "config_built";

	/** Shared preferences key for hashed email */
	public static final String CONFIG_EMAIL = "email_hash";

	/** Shared preference key for whether this is the first time doctor has logged in */
	public static final String CONFIG_FIRST = "first_run";

	/** Shared preferences key for hashed password */
	public static final String CONFIG_PASS = "pass_hash";

	/** Shared preferences key for doctor inputed patient name */
	public static final String CONFIG_PATIENT_NAME = "patient_name";

	/** Shared preferences key for number of days per treatment period */
	public static final String CONFIG_PERIOD_LENGTH = "period_length";

	/** Shared preferences key for number of treatment periods in trial */
	public static final String CONFIG_NUMBER_PERIODS = "number_periods";

}
