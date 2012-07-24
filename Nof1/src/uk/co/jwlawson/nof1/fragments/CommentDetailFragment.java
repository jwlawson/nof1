package uk.co.jwlawson.nof1.fragments;

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CommentDetailFragment extends SherlockFragment {

	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_COMMENT = "comment";

	public CommentDetailFragment() {
	}

	public static CommentDetailFragment newInstance(String data) {
		CommentDetailFragment frag = new CommentDetailFragment();

		Bundle args = new Bundle();
		args.putString(ARG_COMMENT, data);

		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_comment_detail, container, false);

		Bundle args = getArguments();
		if (args != null) {
			((TextView) rootView.findViewById(R.id.comment_detail)).setText(args.getString(ARG_COMMENT));
		}
		return rootView;
	}
}
