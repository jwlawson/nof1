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
package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.CheckFragment;
import uk.co.jwlawson.nof1.fragments.CommentFragment;
import uk.co.jwlawson.nof1.fragments.NumberFragment;
import uk.co.jwlawson.nof1.fragments.RadioFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

public class FragActivity extends SherlockFragmentActivity {
	
	private static final String TAG = "FragmentAtivity";
	
	public FragActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.mock_layout_data_input);
		
		setSupportProgressBarIndeterminateVisibility(false);
		
		// I think that if the state is saved, fragments are kept
		// Need to try this out
		if (savedInstanceState == null) {
			if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Fragments being loaded.");
			
			// Set indeterminate spinner
			setSupportProgressBarIndeterminateVisibility(true);
			
			// Load fragments in data input view in background
			new FragLoader().execute();
		}
		
		Button cancel = (Button) findViewById(R.id.data_input_button_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reschedule();
			}
		});
		
		Button ok = (Button) findViewById(R.id.data_input_button_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});
		
		if (BuildConfig.DEBUG) Log.d(this.getClass().getName(), "Fragments created and added to view");
	}
	
	private void save() {
		// TODO Grab data from each fragment question and save in database
	}
	
	private void reschedule() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_reminder_entry, null);
		final Spinner spin = (Spinner) view.findViewById(R.id.reminder_dialog_spinner);
		
		builder.setTitle("Remind me again in ");
		
		builder.setView(view);
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO schedule new reminder
				if (BuildConfig.DEBUG) Log.d(TAG, "Rescheduling reminder in " + spin.getSelectedItem());
			}
		});
		
		builder.create().show();
	}
	
	private class FragLoader extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			
			// Will read these from the config file
			// File i/o can stall, hence why AsyncTask is required.
			
			// Add some questions
			String[] qus = new String[] { "On average, in comparison to your usual episodes, how long were the attacks?", "On average, in comparison with your usual episodes, how severe were the attacks?", "On average how difficult do you find it to climb the stairs?", "How breathless are you after walking continuously for 5 minutes?" };
			String[] min = new String[] { "Very long", "Very bad", "Very hard", "Very breathless" };
			String[] max = new String[] { "Very short", "Very mild", "Very easy", "Not at all" };
			
			// Check whether the device is large enough for 2 columns
			boolean dualCol = (findViewById(R.id.data_input_fragment_layout2) != null);
			if (dualCol) Log.d("FragAct", "Dual columns found");
			int column = 0;
			
			Bundle a = new Bundle();
			for (int i = 0; i < qus.length; i++) {
				a.putString("QuesFrag" + i + "Question", qus[i]);
				a.putString("QuesFrag" + i + "Min", min[i]);
				a.putString("QuesFrag" + i + "Max", max[i]);
			}
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			for (int i = 0; i < qus.length; i++) {
				RadioFragment frag = new RadioFragment();
				frag.setArguments(a);
				switch (column) {
				case 0:
					ft.add(R.id.data_input_fragment_layout, frag, "quesFrag" + i);
					break;
				case 1:
					ft.add(R.id.data_input_fragment_layout2, frag, "quesFrag" + i);
					break;
				}
				if (dualCol) {
					column++;
					column %= 2;
				}
			}
			ft.commit();
			
			// Add some checkboxes
			String[] qus2 = new String[] { "Have you suffered a seizure today?", "Were you able to go to work?" };
			Bundle b = new Bundle();
			b.putBoolean("CheckFrag0Checked", false);
			b.putString("CheckFrag0Question", qus2[0]);
			b.putBoolean("CheckFrag1Checked", true);
			b.putString("CheckFrag1Question", qus2[1]);
			CheckFragment chkFrag1 = new CheckFragment();
			CheckFragment chkFrag2 = new CheckFragment();
			
			chkFrag1.setArguments(b);
			chkFrag2.setArguments(b);
			
			// add fragments to window
			FragmentTransaction ft0 = getSupportFragmentManager().beginTransaction();
			ft0.add(R.id.data_input_fragment_layout, chkFrag1, "chkfrag1");
			if (dualCol) {
				ft0.add(R.id.data_input_fragment_layout2, chkFrag2, "chkfrag2");
			} else {
				ft0.add(R.id.data_input_fragment_layout, chkFrag2, "chkfrag2");
			}
			ft0.commit();
			if (BuildConfig.DEBUG) Log.d("FA", "Radios created");
			
			// Add number fragments
			NumberFragment num1 = new NumberFragment();
			NumberFragment num2 = new NumberFragment();
			b.putString("NumFrag0Question", "How many times have you fallen over today?");
			b.putString("NumFrag1Question", "On average how many metres can you walk without getting out of breath?");
			num1.setArguments(b);
			num2.setArguments(b);
			
			FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
			ft2.add(R.id.data_input_fragment_layout, num1, "num1");
			if (dualCol) {
				ft2.add(R.id.data_input_fragment_layout2, num2, "num2");
			} else {
				ft2.add(R.id.data_input_fragment_layout, num2, "num2");
			}
			ft2.commit();
			if (BuildConfig.DEBUG) Log.d("FA", "Nums created");
			
			// Add comment fragment
			CommentFragment com = new CommentFragment();
			FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
			// if (dualCol) {
			ft1.add(R.id.data_input_comment_frame, com, "com");
			// } else {
			// ft1.add(R.id.data_input_fragment_layout, com, "com");
			// }
			ft1.commit();
			if (BuildConfig.DEBUG) Log.d("FA", "Comment created");
			return null;
		}
		
		protected void onPostExecute(Void v) {
			if (BuildConfig.DEBUG) Log.d(getClass().getName(), "Fragments finished loading");
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}
