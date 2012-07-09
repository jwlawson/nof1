package uk.co.jwlawson.nof1.activities;

import uk.co.jwlawson.nof1.R;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class UserPrefs extends SherlockPreferenceActivity {

	public UserPrefs() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.user_preferences);
	}

}
