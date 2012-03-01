package se.chalmers.threebook;

import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TocActivity extends Activity {
	EpubContentStream book;
	//TableOfContents toc;
	ListView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toc);
		view = (ListView) findViewById(R.id.lst_toc);
		Log.d("3", "==== Running TocActivity onCreate ====");
		
		ContentStream book = new EpubContentStream(MyBook.get().book());
		//String[] tocStr = (String[]) book.getToc().toArray();
		String[] tocStr = new String[book.getToc().size()];
		int i = 0;
		for (String s : book.getToc()){
			tocStr[i++] = s;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, tocStr);
		
		view.setAdapter(adapter);
		
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				tocEntryClicked((int)id);
			}
		});
		
	}
	
	public void tocEntryClicked(int id){
		Intent displayChapter = new Intent(this, DisplayActivity.class);
		displayChapter.putExtra("se.chalmers.threebook.selectedTocId", id);
		startActivity(displayChapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("3", "=== Running TocActivity onResume ===");
	}
	
}
