package com.example.test.activities;

import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.SherlockListActivity;
import com.example.test.activities.adapters.InputAdapter;

public class ListActivity extends SherlockListActivity {

	public ListActivity() {
		String[] values = new String[] {
				"On average, in comparison to your usual episodes, how long were the attacks?",
				"Very long", "Very short",
				"On average, in comparison with your usual episodes, how severe were the attacks",
				"Very bad", "Very mild" };

		InputAdapter adapter = new InputAdapter(this, new ArrayList<String>(Arrays.asList(values)));

		setListAdapter(adapter);
	}

}
