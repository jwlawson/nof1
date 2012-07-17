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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author John Lawson
 * 
 */
public class DataSource {
	
	private SQLite mHelper;
	
	private SQLiteDatabase mDatabase;
	
	private String[] mColumns;
	
	public DataSource(Context context) {
		mHelper = new SQLite(context);
	}
	
	/** Opens the database. Should not be called in a main thread */
	public void open() {
		mDatabase = mHelper.getWritableDatabase();
		
		int num = mHelper.getNumberQuestions();
		mColumns = new String[num + 3];
		mColumns[0] = SQLite.COLUMN_ID;
		mColumns[1] = SQLite.COLUMN_DAY;
		mColumns[2] = SQLite.COLUMN_QUESTION;
		for (int i = 0; i < num; i++) {
			mColumns[i + 3] = SQLite.COLUMN_QUESTION + i;
		}
	}
	
	/** Close database */
	public void close() {
		mHelper.close();
	}
	
	/**
	 * Save data to the database. Open must have been called before this.
	 * 
	 * @param day Day data is saved
	 * @param data Data to save
	 * @return The column id saved
	 */
	public long saveData(int day, int[] data) {
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_DAY, day);
		for (int i = 0; i < data.length; i++) {
			values.put(SQLite.COLUMN_QUESTION, data[i]);
		}
		
		long insertId = mDatabase.insert(SQLite.TABLE_INFO, null, values);
		
		return insertId;
	}
	
	/**
	 * Save data to the database. Open must have been called before this.
	 * 
	 * @param day
	 * @param data
	 * @param comment
	 * @return The column id saved
	 */
	public long saveData(int day, int[] data, String comment) {
		long insertId = saveData(day, data);
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_COMMENT, comment);
		
		mDatabase.update(SQLite.TABLE_INFO, values, SQLite.COLUMN_ID + "=" + insertId, null);
		
		return insertId;
	}
	
	/**
	 * Get a column from the database
	 * 
	 * @param column Column name to return
	 * @return A cursor containing the day data was recorded and the requested column
	 */
	public Cursor getColumn(String column) {
		
		return getColumns(new String[] { SQLite.COLUMN_DAY, column });
	}
	
	/**
	 * Get columns from the database
	 * 
	 * @param columns Column names to return
	 * @return A cursor containing the requested columns
	 */
	public Cursor getColumns(String[] columns) {
		Cursor cursor = mDatabase.query(SQLite.TABLE_INFO, columns, null, null, null, null, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
}
