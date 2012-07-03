package uk.co.jwlawson.nof1.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import uk.co.jwlawson.nof1.R;

public class QuestionFragment extends SherlockFragment {
	
	private String mQuestion;
	private String mMin;
	private String mMax;
	
	public QuestionFragment(String question, String min, String max) {
		mQuestion = question;
		mMin = min;
		mMax = max;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.mock_row_layout_data_input, container, false);
		
		((TextView) view.findViewById(R.id.textView1)).setText(mQuestion);
		((TextView) view.findViewById(R.id.textView2)).setText(mMin);
		((TextView) view.findViewById(R.id.textView3)).setText(mMax);
		
		return view;
		
	}
	
}
