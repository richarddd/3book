package se.chalmers.threeBook;

import java.util.List;

import se.chalmers.threeBook.adapters.RecentBookAdapter;
import se.chalmers.threeBook.ui.HorizontalListView;
import se.chalmers.threeBook.ui.RecentBook;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

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
		
		adapter = new RecentBookAdapter(this);

		layOpenImport.setOnClickListener(new StartOnClick(FileBrowserActivity.class));
		laySettings.setOnClickListener(new StartOnClick(FileBrowserActivity.class));
		layCollection.setOnClickListener(new StartOnClick(FileBrowserActivity.class));
		layFavourites.setOnClickListener(new StartOnClick(FileBrowserActivity.class));
		
		List<RecentBook> bookList = adapter.getItems();
		
		for(int i = 0; i < 1000; i++){
			bookList.add(new RecentBook(new String("Test "+i).toUpperCase(), R.drawable.recent_book_cover));	
		}
		
		
		lstRecentBooks.setAdapter(adapter);
		
		
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