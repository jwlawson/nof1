package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.BuildConfig;
import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.fragments.QuestionFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
		
		Fragment frag1 = new QuestionFragment("On average, in comparison to your usual episodes, how long were the attacks?", "Very long", "Very short");
		Fragment frag2 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
		Fragment frag3 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
		Fragment frag4 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
		Fragment frag5 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.data_input_fragment_layout, frag1, "fragment1");
		ft.add(R.id.data_input_fragment_layout, frag2, "fragment2");
		ft.add(R.id.data_input_fragment_layout, frag3, "fragment3");
		ft.add(R.id.data_input_fragment_layout, frag4, "fragment4");
		ft.add(R.id.data_input_fragment_layout, frag5, "fragment5");
		ft.commit();
		
		if (findViewById(R.id.data_input_fragment_layout2) != null) {
			
			if (BuildConfig.DEBUG) Log.d(this.getClass().getName(), "Dual column view found");
			
			Fragment frag6 = new QuestionFragment("On average, in comparison to your usual episodes, how long were the attacks?", "Very long", "Very short");
			Fragment frag7 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
			Fragment frag8 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
			Fragment frag9 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
			Fragment frag0 = new QuestionFragment("On average, in comparison with your usual episodes, how severe were the attacks?", "Very bad", "Very mild");
			
			FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
			ft2.add(R.id.data_input_fragment_layout2, frag6, "fragment6");
			ft2.add(R.id.data_input_fragment_layout2, frag7, "fragment7");
			ft2.add(R.id.data_input_fragment_layout2, frag8, "fragment8");
			ft2.add(R.id.data_input_fragment_layout2, frag9, "fragment9");
			ft2.add(R.id.data_input_fragment_layout2, frag0, "fragment0");
			ft2.commit();
		}
		
		if (BuildConfig.DEBUG) Log.d(this.getClass().getName(), "Fragments created and added to view");
	}
	
}
