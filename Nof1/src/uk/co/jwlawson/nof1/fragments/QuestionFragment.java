package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class QuestionFragment extends SherlockFragment implements
		RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

	private static final String TAG = "QuestionFragment";
	private static int COUNT = 0;

	private String mQuestion;
	private String mMin;
	private String mMax;
	private int mSelected;
	private boolean init = false;
	private int mId;

	public QuestionFragment() {
		mId = COUNT;
		COUNT++;
	}

	public int getSelected() {
		return mSelected;
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		if (!init) mySetArguments(savedState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// TODO if the view is too narrow to hold all 7 radiobuttons, swap for a
		// slider?

		if (!init) mySetArguments(savedInstanceState);

		View view = inflater.inflate(R.layout.mock_row_layout_data_input, container, false);

		((TextView) view.findViewById(R.id.textView1)).setText(mQuestion);
		((TextView) view.findViewById(R.id.textView2)).setText(mMin);
		((TextView) view.findViewById(R.id.textView3)).setText(mMax);

		RadioGroup radio = (RadioGroup) view.findViewById(R.id.radioGroup1);
		SeekBar seek = (SeekBar) view.findViewById(R.id.seekBar1);

		boolean wide = (container.getWidth() > radio.getWidth());

		if (wide) {
			radio.setOnCheckedChangeListener(this);
			seek.setVisibility(View.GONE);
		} else {
			seek.setOnSeekBarChangeListener(this);
			radio.setVisibility(View.GONE);
		}

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
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArguments(args);
	}

	private void mySetArguments(Bundle args) {
		if (args != null) {
			if (args.containsKey("QuesFrag" + mId + "Question"))
				mQuestion = args.getString("QuesFrag" + mId + "Question");
			if (args.containsKey("QuesFrag" + mId + "Min"))
				mMin = args.getString("QuesFrag" + mId + "Min");
			if (args.containsKey("QuesFrag" + mId + "Max"))
				mMax = args.getString("QuesFrag" + mId + "Max");
		}
		init = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Destroyed");
		COUNT--;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// checkedId gives currently selected button
		if (BuildConfig.DEBUG) Log.d(TAG, "RadioButton selected: " + checkedId);
		mSelected = checkedId;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// progress is value between 0 and 7
		if (BuildConfig.DEBUG) Log.d(TAG, "Seekbar at " + progress);
		mSelected = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Not needed
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Not needed
	}

}
