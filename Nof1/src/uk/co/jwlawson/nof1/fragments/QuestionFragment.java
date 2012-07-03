package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class QuestionFragment extends SherlockFragment {

	public static int COUNT = 0;

	private String mQuestion;
	private String mMin;
	private String mMax;
	private int mId;

	public QuestionFragment(String question, String min, String max) {
		mQuestion = question;
		mMin = min;
		mMax = max;
		mId = COUNT;
		COUNT++;
	}

	public QuestionFragment() {
		if (BuildConfig.DEBUG) Log.d(getClass().getName(), "New QuestionFragment" + mQuestion);
		mId = COUNT;
		COUNT++;
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		if (savedState != null) {
			if (savedState.containsKey("QuesFrag" + mId + "Question"))
				mQuestion = savedState.getString("QuesFrag" + mId + "Question");
			if (savedState.containsKey("QuesFrag" + mId + "Min"))
				mMin = savedState.getString("QuesFrag" + mId + "Min");
			if (savedState.containsKey("QuesFrag" + mId + "Max"))
				mMax = savedState.getString("QuesFrag" + mId + "Max");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.mock_row_layout_data_input, container, false);

		((TextView) view.findViewById(R.id.textView1)).setText(mQuestion);
		((TextView) view.findViewById(R.id.textView2)).setText(mMin);
		((TextView) view.findViewById(R.id.textView3)).setText(mMax);

		return view;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// For each questionFragment, add its strings to the bundle
		// Use Id to differentiate.
		outState.putString("QuesFrag" + mId + "Question", mQuestion);
		outState.putString("QuesFrag" + mId + "Min", mMin);
		outState.putString("QuesFrag" + mId + "Max", mMax);
	}

	@Override
	public void setArguments(Bundle savedState) {
		mQuestion = savedState.getString("QuesFrag" + mId + "Question");
		mMin = savedState.getString("QuesFrag" + mId + "Min");
		mMax = savedState.getString("QuesFrag" + mId + "Max");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Destroyed");
		COUNT = 0;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Stopped");
	}
}
