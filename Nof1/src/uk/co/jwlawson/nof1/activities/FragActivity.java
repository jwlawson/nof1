package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.QuestionFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class FragActivity extends SherlockFragmentActivity {

	public FragActivity() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mock_layout_data_input);

		// I think that if the state is saved, fragments are kept
		// Need to try this out
		if (savedInstanceState == null) {
			// Load fragments in data input view in background
			new FragLoader().execute();
		}

		if (BuildConfig.DEBUG)
			Log.d(this.getClass().getName(), "Fragments created and added to view");
	}

	private class FragLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			// Will read these from the config file
			String[] qus = new String[] {
					"On average, in comparison to your usual episodes, how long were the attacks?",
					"On average, in comparison with your usual episodes, how severe were the attacks?",
					"On average how difficult do you find it to climb the stairs?",
					"How breathless are you after walking continuously for 5 minutes?" };
			String[] min = new String[] { "Very long", "Very bad", "Very hard", "Very breathless" };
			String[] max = new String[] { "Very short", "Very mild", "Very easy", "Not at all" };

			// Check whether the device is large enough for 2 columns
			boolean dualCol = (findViewById(R.id.data_input_fragment_layout2) != null);
			if (dualCol) Log.d("FragAct", "Dual columns found");

			for (int i = 0; i < qus.length; i++) {

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.data_input_fragment_layout,
						new QuestionFragment(qus[i], min[i], max[i]), "fragment" + i);
				if (dualCol)
					ft.add(R.id.data_input_fragment_layout2, new QuestionFragment(qus[qus.length
							- 1 - i], min[qus.length - 1 - i], max[qus.length - 1 - i]),
							"fragment2" + i);
				ft.commit();
			}

			for (int i = 0; i < qus.length; i++) {

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.data_input_fragment_layout,
						new QuestionFragment(qus[i], min[i], max[i]), "fragment" + i);
				if (dualCol)
					ft.add(R.id.data_input_fragment_layout2, new QuestionFragment(qus[qus.length
							- 1 - i], min[qus.length - 1 - i], max[qus.length - 1 - i]),
							"fragment2" + i);
				ft.commit();
			}
			return null;
		}

	}
}
