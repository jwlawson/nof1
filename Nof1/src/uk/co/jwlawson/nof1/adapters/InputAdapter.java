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
package uk.co.jwlawson.nof1.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import uk.co.jwlawson.nof1.R;

public class InputAdapter extends ArrayAdapter<String> {

	private ArrayList<String> mQuestions;
	private ArrayList<String> mMins;
	private ArrayList<String> mMaxs;
	private Context mContext;

	/**
	 * 
	 * @param context
	 * @param objects
	 *            List of form Question, Min ans, Max ans
	 */
	public InputAdapter(Context context, List<String> objects) {
		super(context, R.layout.mock_row_layout_data_input, objects);

		mQuestions = new ArrayList<String>();
		mMins = new ArrayList<String>();
		mMaxs = new ArrayList<String>();

		mContext = context;
		for (int i = 0; i < objects.size(); i = i + 3) {
			mQuestions.add(objects.get(i));
			mMins.add(objects.get(i + 1));
			mMaxs.add(objects.get(i + 2));
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.mock_row_layout_data_input, parent, false);
		TextView question = (TextView) rowView.findViewById(R.id.textView1);
		TextView min = (TextView) rowView.findViewById(R.id.textView2);
		TextView max = (TextView) rowView.findViewById(R.id.textView3);

		question.setText(mQuestions.get(position));
		min.setText(mMins.get(position));
		max.setText(mMaxs.get(position));

		return rowView;
	}

}
