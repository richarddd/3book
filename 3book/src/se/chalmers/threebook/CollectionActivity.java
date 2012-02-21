package se.chalmers.threebook;

import se.chalmers.threeBook.R;
import se.chalmers.threebook.ui.ActionBarTabActivity;
import se.chalmers.threebook.ui.fragments.AuthorsFragment;
import se.chalmers.threebook.ui.fragments.BooksFragment;
import se.chalmers.threebook.ui.fragments.TagsFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class CollectionActivity extends ActionBarTabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection);
		
		addFragment(BooksFragment.class, getString(R.string.books), savedInstanceState);
		addFragment(AuthorsFragment.class,  getString(R.string.authors), savedInstanceState);
		addFragment(TagsFragment.class,  getString(R.string.tags), savedInstanceState);
		
		buildTabs(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.collection, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_search:
			Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_settings:
			Toast.makeText(this, "Tapped settings", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
