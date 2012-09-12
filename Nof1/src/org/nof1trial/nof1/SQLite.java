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

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class to create, load, update etc the database
 * 
 * @author John Lawson
 * 
 */
public class SQLite extends SQLiteOpenHelper {

	private static final String TAG = "SQLiteHelper";
	private static final boolean DEBUG = false;

	/** Table name */
	public static final String TABLE_INFO = "info";
	/** Auto-incrementing ID column */
	public static final String COLUMN_ID = "_id";
	/** Day of the trial column */
	public static final String COLUMN_DAY = "day";
	/** Time column */
	public static final String COLUMN_TIME = "time";
	/** Comments column */
	public static final String COLUMN_COMMENT = "comment";
	/** Question column prefix */
	public static final String COLUMN_QUESTION = "question";

	/**
	 * The number of questions, hence the number of columns called {@code question<i>}
	 */
	private int mNumQuestion;

	/** Database file name */
	public static final String DATABASE_NAME = "info.db";

	private Context mContext;

	/** Construct database with custom name and version number */
	public SQLite(Context context) {
		super(context, DATABASE_NAME, null, context.getSharedPreferences(Keys.CONFIG_NAME, Context.MODE_PRIVATE).getInt(Keys.CONFIG_DB_VERSION, 1));

		mContext = context;
	}

	/**
	 * Set the number of questions asked in data input. Must be called before the database is first created.
	 * 
	 * @param num The number of questions asked.
	 */
	public void setQuestionNumber(int num) {
		mNumQuestion = num;
	}

	public int getNumberQuestions() {
		return mNumQuestion;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(makeCreate());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

		// Make array of all column headers
		int num = getNumberQuestions();
		String[] columns = new String[num + 4];
		columns[0] = SQLite.COLUMN_ID;
		columns[1] = SQLite.COLUMN_DAY;
		columns[2] = SQLite.COLUMN_TIME;

		for (int i = 0; i < num; i++) {
			columns[i + 3] = SQLite.COLUMN_QUESTION + i;
		}
		columns[num + 3] = SQLite.COLUMN_COMMENT;

		// Get all data from database
		Cursor cursor;
		synchronized (DataSource.sDataLock) {
			cursor = db.query(TABLE_INFO, columns, null, null, null, null, null);
		}

		// Store all data in arrays. Could be lots of data
		int size = cursor.getCount();
		int[] days = new int[size];
		long[] times = new long[size];
		int[][] questions = new int[size][num];
		String[] comments = new String[size];

		cursor.moveToFirst();

		// If time is of form HH:MM need to convert to long
		boolean updateTimeToLong = cursor.getString(2).contains(":");

		Calendar startCal = null;
		Calendar cal = null;
		if (updateTimeToLong) {
			SharedPreferences prefs = mContext.getSharedPreferences(Keys.CONFIG_START, Context.MODE_PRIVATE);
			String startDate = prefs.getString(Keys.CONFIG_START, null);
			if (startDate == null) {
				updateTimeToLong = false;
			} else {
				// Get start date of the trial
				String[] arr = startDate.split(":");
				int startDay = Integer.parseInt(arr[0]);
				int startMonth = Integer.parseInt(arr[1]);
				int startYear = Integer.parseInt(arr[2]);

				startCal = Calendar.getInstance();
				startCal.set(startYear, startMonth, startDay);

				cal = Calendar.getInstance();
			}
		}

		for (int i = 0; i < size; i++) {
			days[i] = cursor.getInt(1);

			if (updateTimeToLong) {
				// convert from hh:mm to standard time in millis
				String time = cursor.getString(2);
				int hour = Integer.parseInt(time.substring(0, 2));
				int min = Integer.parseInt(time.substring(3, 5));

				cal.setTimeInMillis(startCal.getTimeInMillis());
				cal.set(Calendar.HOUR, hour);
				cal.set(Calendar.MINUTE, min);
				cal.add(Calendar.DAY_OF_MONTH, days[i]);

				times[i] = cal.getTimeInMillis();
			} else {
				times[i] = cursor.getLong(2);
			}
			for (int j = 0; j < num; j++) {
				questions[i][j] = cursor.getInt(j + 3);
			}
			comments[i] = cursor.getString(num + 3);

			cursor.moveToNext();
		}
		cursor.close();

		// Delete current table
		synchronized (DataSource.sDataLock) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);

			// Make new table
			onCreate(db);
		}

		// Fill new table with old data
		for (int i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_DAY, days[i]);
			values.put(COLUMN_TIME, times[i]);
			for (int j = 0; j < questions[i].length; j++) {
				values.put(SQLite.COLUMN_QUESTION + j, questions[i][j]);
			}
			values.put(COLUMN_COMMENT, comments[i]);

			synchronized (DataSource.sDataLock) {
				db.insert(SQLite.TABLE_INFO, null, values);
			}
		}
	}

	/** Create the database creation SQL command */
	private String makeCreate() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(TABLE_INFO).append("(").append(COLUMN_ID).append(" integer primary key autoincrement, ").append(COLUMN_DAY).append(" integer, ")
				.append(COLUMN_TIME).append(" integer");

		if (mNumQuestion == 0) Log.e(TAG, "No question number set");
		for (int i = 0; i < mNumQuestion; i++) {
			sb.append(", ").append("question").append(i).append(" integer");
		}

		sb.append(", ").append(COLUMN_COMMENT).append(" text");
		sb.append(");");
		return sb.toString();
	}

}
