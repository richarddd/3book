package se.chalmers.threebook;

import se.chalmers.threebook.ui.actionbarcompat.ActionBarPreferenceActivity;
import se.chalmers.threebook.util.Helper;
import se.chalmers.threebook.util.PreferenceHelper;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;
import android.view.Window;

public class PreferencesActivity extends ActionBarPreferenceActivity {
	
	private PreferenceHelper preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (!Helper.SupportsNewApi()) {
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		preferences = new PreferenceHelper(this);
		
		if (Helper.SupportsNewApi()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		Preference margins = findPreference("margins");
		margins.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showMarginDialog();
				return true;
			}
		});
		
		Preference fontSize = findPreference("font_size");
		fontSize.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showFontSizeDialog();
				return true;
			}
		});
		
		Preference backgroundColor = findPreference("background_color");
		backgroundColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showBackgroundColorDialog();
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}