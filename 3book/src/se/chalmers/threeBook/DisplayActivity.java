package se.chalmers.threeBook;

import java.io.IOException;
import java.io.Reader;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import parser.epub.EpubBook;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class DisplayActivity extends Activity {
	
	private Button nextChap, prevChap, showToc;
	private WebView view;
	private Book epublibBook;
	private int tocId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		view = (WebView) findViewById(R.id.web_webView1);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginsEnabled(true);
        settings.setDomStorageEnabled(true);
		
        tocId = (int)(Integer) getIntent().getSerializableExtra("se.chalmers.threebook.tocId");
		Resource chapter = (Resource) getIntent().getSerializableExtra("se.chalmers.threebook.ChapterToDisplay");
		epublibBook = (Book) getIntent().getSerializableExtra("se.chalmers.threebook.TheBook"); // passing the entire book around hopefully won't be done later.
		nextChap = (Button) findViewById(R.id.btn_nextChapter);
	    prevChap = (Button) findViewById(R.id.btn_prevChapter);
	    showToc = (Button) findViewById(R.id.btn_displayToc);
		
	    EpubBook book = new EpubBook(epublibBook);
	    
		try {
			Log.d("3", "title of chapter to display: " + chapter.getTitle());
			view.loadData(book.getWrappedFileText(chapter), chapter.getMediaType().getName(), chapter.getInputEncoding());
		} catch (IOException e) {
			Log.d("3", "IOE in displayChapter: " + e.getMessage());
		}
		
		showToc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent tocIntent = new Intent(view.getContext(), TocActivity.class);
				int GET_SECTION_REFERENCE = 1;
				tocIntent.putExtra("se.chalmers.threebook.TableOfContents", epublibBook.getTableOfContents());
				tocIntent.putExtra("se.chalmers.threebook.TheBook", epublibBook);
				startActivityForResult(tocIntent, GET_SECTION_REFERENCE);
			}
		});
		
		prevChap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		nextChap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
}
