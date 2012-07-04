package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CheckFragment extends SherlockFragment {

	private static int COUNT = 0;

	private boolean mChecked;
	private String mQuestion;
	private boolean init = false;
	private final int mId;

	public CheckFragment() {
		mId = COUNT;
		COUNT++;
	}

	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.row_layout_data_checkbox, container, false);

		if (!init) mySetArgs(savedInstanceState);

		CheckBox chk = (CheckBox) view.findViewById(R.id.data_input_checkbox_chk);
		chk.setChecked(mChecked);

		TextView txt = (TextView) view.findViewById(R.id.data_input_checkbox_txt_question);
		txt.setText(mQuestion);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		COUNT--;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!init) mySetArgs(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("CheckFrag" + mId + "Checked", mChecked);
		outState.putString("CheckFrag" + mId + "Question", mQuestion);
	};

	private void mySetArgs(Bundle args) {
		if (args != null) {
			if (args.containsKey("CheckFrag" + mId + "Checked")) {
				mChecked = args.getBoolean("CheckFrag" + mId + "Checked");
			}
			if (args.containsKey("CheckFrag" + mId + "Question")) {
				mQuestion = args.getString("CheckFrag" + mId + "Question");
			}
		}
		init = true;
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mySetArgs(args);
	}

}
