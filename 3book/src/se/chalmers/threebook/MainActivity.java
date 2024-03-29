package se.chalmers.threebook;

import java.util.List;
import se.chalmers.threebook.R;
import se.chalmers.threebook.adapters.RecentBookAdapter;
import se.chalmers.threebook.db.BookDataHelper;
import se.chalmers.threebook.model.Book;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.util.Helper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private RelativeLayout layOpenImport;
	private RelativeLayout laySettings;
	private RelativeLayout layCollection;
	private RelativeLayout layFavourites;
	
	private HorizontalListView lstRecentBooks;
	private RecentBookAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		layOpenImport = (RelativeLayout) findViewById(R.id.lay_open_import);
		laySettings = (RelativeLayout) findViewById(R.id.lay_settings);
		layCollection = (RelativeLayout) findViewById(R.id.lay_my_collection);
		layFavourites = (RelativeLayout) findViewById(R.id.lay_favourites);
		lstRecentBooks = (HorizontalListView) findViewById(R.id.lst_recent_books);
		
		final Context context = lstRecentBooks.getContext();
		adapter = new RecentBookAdapter(context, BookDataHelper.getBooks(context));
		
		lstRecentBooks.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Helper.openBook(view.getContext(), (Book) parent.getItemAtPosition(position));
			}
		});
		
		lstRecentBooks.setAdapter(adapter);

		layOpenImport.setOnClickListener(new StartOnClick(FileBrowserActivity.class));
		/*layOpenImport.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});*/
		//laySettings.setOnClickListener(new StartOnClick(ReadActivity.class));
		laySettings.setOnClickListener(new StartOnClick(PreferencesActivity.class));
		//laySettings.setOnClickListener(new StartOnClick(DisplayActivity.class));
		layCollection.setOnClickListener(new StartOnClick(CollectionActivity.class));
		layFavourites.setOnClickListener(new StartOnClick(FavouritesActivity.class));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
                break;

                
            
                /*case R.id.menu_refresh:
                Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
                getActionBarHelper().setRefreshActionItemState(true);
                getWindow().getDecorView().postDelayed(
                        new Runnable() {
                            public void run() {
                                getActionBarHelper().setRefreshActionItemState(false);
                            }
                        }, 1000);
                break;
                 */
            case R.id.menu_search:
                Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_share:
                Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	private class StartOnClick implements OnClickListener {
		private Class intentClass;
		public StartOnClick(Class intentClass) {
			super();
			this.intentClass = intentClass;
		}
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(), intentClass);
			startActivity(intent);
		}
	}
}