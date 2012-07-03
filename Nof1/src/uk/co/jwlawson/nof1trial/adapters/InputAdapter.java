package uk.co.jwlawson.nof1trial.adapters;

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
