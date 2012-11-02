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
package org.nof1trial.nof1.containers;

import java.util.ArrayList;
import java.util.List;

import org.nof1trial.nof1.DataSource;
import org.nof1trial.nof1.Keys;
import org.nof1trial.nof1.SQLite;
import org.nof1trial.nof1.app.Util;
import org.nof1trial.nof1.shared.DataProxy;
import org.nof1trial.nof1.shared.DataRequest;
import org.nof1trial.nof1.shared.MyRequestFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * @author John Lawson
 * 
 */
public class Data {

	private static final String CACHE = "config";
	private static final String NUM_DATA = "num_data_cache";

	private int day;
	private long time;
	private String comment;
	private int[] data;
	private Context context;
	private OnDataRequestListener listener;

	public interface OnDataRequestListener {

		public void onDataUploadSuccess(Data data, DataProxy dataProxy);

		public void onDataUploadFailure(Data data, ServerFailure error);
	}

	private Data() {
	}

	public static void clearDataCache(Context context) {
		SharedPreferences cachePrefs = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);
		int count = cachePrefs.getInt(NUM_DATA, 0);

		for (int i = 0; i < count; i++) {
			clearDataCacheAt(i, cachePrefs);
		}
	}

	private static void clearDataCacheAt(int id, SharedPreferences cachePrefs) {

		// Remove data from prefs
		SharedPreferences.Editor editor = cachePrefs.edit();
		editor.putString(Keys.DATA_DAY + id, null);
		editor.putString(Keys.DATA_TIME + id, null);
		editor.putString(Keys.DATA_COMMENT + id, null);
		editor.putString(Keys.DATA_LIST + id, null);

		// decrement counter in prefs
		int count = cachePrefs.getInt(NUM_DATA, 1);
		editor.putInt(NUM_DATA, count - 1).commit();
	}

	public void upload() {
		MyRequestFactory requestFactory = Util.getRequestFactory(context, MyRequestFactory.class);
		DataRequest request = requestFactory.dataRequest();

		DataProxy proxy = request.create(DataProxy.class);
		proxy.setDay(day);
		proxy.setTime(time);
		proxy.setComment(comment);

		ArrayList<Integer> list = new ArrayList<Integer>();
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				list.add(data[i]);
			}
		}
		proxy.setQuestionData(list);

		request.save(proxy).fire(new Receiver<DataProxy>() {

			@Override
			public void onSuccess(DataProxy response) {
				listener.onDataUploadSuccess(Data.this, response);
			}

			@Override
			public void onFailure(ServerFailure error) {
				listener.onDataUploadFailure(Data.this, error);
			}

		});
	}

	public void save() {
		DataSource source = new DataSource(context);
		source.open();
		source.saveData(day, time, data, comment);
		source.close();
	}

	public void cache() {

		SharedPreferences sp = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();

		int count = sp.getInt(NUM_DATA, 0) + 1;
		// Increment counter for number of cached data sets
		edit.putInt(NUM_DATA, count);

		edit.putInt(Keys.DATA_DAY + count, day);
		edit.putLong(Keys.DATA_TIME + count, time);
		edit.putString(Keys.DATA_COMMENT + count, comment);

		if (data != null) {
			// Convert int array to string, to allow storage in shared_prefs
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				sb.append(data[i]).append(",");
			}
			// Remove trailing comma
			sb.deleteCharAt(sb.length() - 1);
			edit.putString(Keys.DATA_LIST + count, sb.toString());
		}

		edit.commit();
	}

	public static class Factory {

		private final OnDataRequestListener listener;

		private final Context context;

		public Factory(OnDataRequestListener listener, Context context) {
			this.listener = listener;
			this.context = context;
		}

		public Data generateFromCache(int id) {
			Data result = getNewData();

			SharedPreferences cachPrefs = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);

			result.day = cachPrefs.getInt(Keys.DATA_DAY + id, 0);
			result.time = cachPrefs.getLong(Keys.DATA_TIME + id, 0);
			result.comment = cachPrefs.getString(Keys.DATA_COMMENT + id, "");
			String dataStr = cachPrefs.getString(Keys.DATA_LIST + id, "");

			int[] data = null;
			if (dataStr.length() != 0) {
				String[] dataStrArr = dataStr.split(",");
				data = new int[dataStrArr.length];
				for (int i = 0; i < data.length; i++) {
					data[i] = Integer.parseInt(dataStrArr[i]);
				}
			}
			result.data = data;

			return result;
		}

		public Data generateFromCursor(Cursor cursor) {
			Data result = getNewData();

			int dayCol = cursor.getColumnIndexOrThrow(SQLite.COLUMN_DAY);
			int timeCol = cursor.getColumnIndexOrThrow(SQLite.COLUMN_TIME);
			int commentCol = cursor.getColumnIndexOrThrow(SQLite.COLUMN_COMMENT);

			result.day = cursor.getInt(dayCol);
			result.time = cursor.getLong(timeCol);
			result.comment = cursor.getString(commentCol);
			int size = commentCol - timeCol - 1;
			int[] data = new int[size];

			for (int i = 0; i < size; i++) {
				data[i] = cursor.getInt(timeCol + 1 + i);
			}
			result.data = data;

			return result;
		}

		public Data generateFromIntent(Intent intent) {
			Data result = getNewData();

			result.day = intent.getIntExtra(Keys.DATA_DAY, 0);
			result.time = intent.getLongExtra(Keys.DATA_TIME, 0);
			result.comment = intent.getStringExtra(Keys.DATA_COMMENT);
			result.data = intent.getIntArrayExtra(Keys.DATA_LIST);

			return result;
		}

		public List<Data> generateCachedDataList() {
			List<Data> list = new ArrayList<Data>();

			SharedPreferences sp = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);
			int count = sp.getInt(NUM_DATA, 0);

			for (int i = 0; i < count; i++) {
				list.add(generateFromCache(i));
			}
			return list;
		}

		private Data getNewData() {
			Data data = new Data();
			data.listener = listener;
			data.context = context;

			return data;
		}
	}
}
