package se.chalmers.threeBook;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TocActivity extends Activity {
	Book book;
	TableOfContents toc;
	ListView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.toc);
		view = (ListView) findViewById(R.id.lst_toc);

		// FIGURE OUT WHAT AND HOW TO PASS AROUND
		book = (Book) getIntent().getSerializableExtra("se.chalmers.threebook.TheBook");
		toc = book.getTableOfContents();
		
		String[] tocStr = new String[toc.size()];
		int i = 0;
		for (TOCReference res : toc.getTocReferences()){
			tocStr[i++] = res.getTitle();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, tocStr);
		
		view.setAdapter(adapter);
		
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				tocEntryClicked((int)id);
				
			}
		});
	}
	
	public void tocEntryClicked(int id){
		Resource res = toc.getTocReferences().get((int) id).getResource();
		Intent displayChapter = new Intent(this, DisplayActivity.class);
		displayChapter.putExtra("se.chalmers.threebook.tocId", id);
		displayChapter.putExtra("se.chalmers.threebook.ChapterToDisplay", res);
		displayChapter.putExtra("se.chalmers.threebook.TheBook", book);
		startActivity(displayChapter);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
}
