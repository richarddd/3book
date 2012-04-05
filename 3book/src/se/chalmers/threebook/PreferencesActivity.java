package se.chalmers.threebook;

import java.util.zip.Inflater;

import se.chalmers.threebook.ui.actionbarcompat.ActionBarPreferenceActivity;
import se.chalmers.threebook.util.Constants;
import se.chalmers.threebook.util.Helper;
import se.chalmers.threebook.util.PreferenceHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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

		Preference margins = (Preference) findPreference("margins");
		margins.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showMarginDialog();
				return true;
			}
		});
		
		Preference fontSize = (Preference) findPreference("font_size");
		fontSize.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showFontSizeDialog();
				return true;
			}
		});
		
		Preference backgroundColor = (Preference) findPreference("background_color");
		backgroundColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				preferences.showBackgroundColorDialog();
				return true;
			}
		});
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}