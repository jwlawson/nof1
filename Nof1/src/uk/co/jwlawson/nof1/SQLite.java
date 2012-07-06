/*******************************************************************************
 * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package uk.co.jwlawson.nof1;

import android.content.Context;
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
	
	/** Table name */
	public static final String TABLE_INFO = "info";
	/** Auto-incrementing ID column */
	public static final String COLUMN_ID = "_id";
	/** Day of the trial column */
	public static final String COLUMN_DAY = "day";
	/** Comments column */
	public static final String COLUMN_COMMENT = "comment";
	
	/** The number of questions, hence the number of columns called {@code question<i>} */
	public static int NUM_QUESTIONS;
	
	private static final String DATABASE_NAME = "info.db";
	private static final int DATABASE_VERSION = 1;
	
	/** Construct database with custom name and version number */
	public SQLite(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * Set the number of questions asked in data input. Must be called before the database is first created.
	 * 
	 * @param num The number of questions asked.
	 */
	public void setQuestionNumber(int num) {
		NUM_QUESTIONS = num;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(makeCreate());
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
		onCreate(db);
	}
	
	/** Create the database creation SQL command */
	private String makeCreate() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(TABLE_INFO).append("(").append(COLUMN_ID).append(" integer primary key autoincrement, ").append(COLUMN_DAY).append(" integer");
		for (int i = 0; i < NUM_QUESTIONS; i++) {
			sb.append(", ").append("question").append(i).append(" integer");
		}
		sb.append(", ").append(COLUMN_COMMENT).append(" text");
		sb.append(");");
		return sb.toString();
	}
}
