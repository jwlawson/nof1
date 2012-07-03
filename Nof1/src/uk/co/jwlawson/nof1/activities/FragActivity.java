package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.fragments.QuestionFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.test.R;

public class FragActivity extends SherlockFragmentActivity {
	
	public FragActivity() {
		
		Fragment frag1 = new QuestionFragment("On average, in comparison to your usual episodes, how long were the attacks?", "Very long", "Very short");
		Fragment frag2 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks", "Very bad", "Very mild");
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.data_input_fragment_layout, frag1);
		ft.add(R.id.data_input_fragment_layout, frag2);
		ft.commit();
	}
	
}
