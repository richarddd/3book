package se.chalmers.threebook;

import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarPreferenceActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class PreferencesActivity extends ActionBarPreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (Helper.SupportsNewApi()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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