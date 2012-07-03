package uk.co.jwlawson.nof1trial.activities;

import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1trial.fragments.QuestionFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class FragActivity extends SherlockFragmentActivity {
	
	public FragActivity() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Fragment frag1 = new QuestionFragment("On average, in comparison to your usual episodes, how long were the attacks?", "Very long", "Very short");
		Fragment frag2 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks", "Very bad", "Very mild");
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.data_input_fragment_layout, frag1, "fragment1");
		ft.add(R.id.data_input_fragment_layout, frag2, "fragment2");
		ft.commit();
	}
	
}
