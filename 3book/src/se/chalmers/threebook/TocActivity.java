package se.chalmers.threebook;

import java.io.IOException;


import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.OldEpubContentStream;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.model.TocReference;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.util.Helper;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TocActivity extends ActionBarActivity {

	public enum IntentValue {
		READ
	}

	public enum IntentKey {

	}

	OldEpubContentStream book;
	ListView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toc);
		view = (ListView) findViewById(R.id.lst_toc);
		Log.d("3", "==== Running TocActivity onCreate ====");

		ContentStream book = null;
		try {
			book = new OldEpubContentStream(MyBook.get().book(), getCacheDir());
		} catch (IOException e) {
			Log.d("3", "Could not open book in TocActivity, crapcakes!");
			e.printStackTrace();
		}
		String[] tocStr = new String[book.getToc().size()];
		int i = 0;
		for (TocReference r : book.getToc().getLinearToc()){
			tocStr[i++] = r.getTitle();
		}
		

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, tocStr);

		view.setAdapter(adapter);

		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				tocEntryClicked((int) id);
			}
		});

	}

	public void tocEntryClicked(int id) {
		// TODO refactor to use general method somewhere!
		Intent displayChapter = new Intent(this, ReadActivity.class);

		// XXX this access is probabl not OK.
		String fragment = MyBook.get().book().getTableOfContents()
				.getTocReferences().get(id).getFragmentId();
		String resourceID = MyBook.get().book().getTableOfContents()
				.getTocReferences().get(id).getResourceId();

		Log.d("3", "TocActivity sends fragmentID: " + fragment);
		Log.d("3", "TocActivity keeps resourceID : " + resourceID);

		displayChapter.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(),
				ReadActivity.IntentType.GO_TO_TOC_INDEX);
		displayChapter
				.putExtra(ReadActivity.IntentKey.TOC_INDEX.toString(), id);
		displayChapter.putExtra(ReadActivity.IntentKey.TOC_ANCHOR.toString(),
				fragment);
		startActivity(displayChapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("3", "=== Running TocActivity onResume ===");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
