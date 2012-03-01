package se.chalmers.threebook;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;



public class DisplayActivity extends Activity {

	//public static final String BOOK_URL = "pg11.epub";
	//public static final String BOOK_URL = "ub-EKJV.epub";
	public static final String BOOK_URL = "pride-prejudice.epub";
	
	private Button nextChap, prevChap, showToc;
	private WebView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_axel);
		Log.d("3", "==== Running DisplayActivity onCreate ====");
		view = (WebView) findViewById(R.id.web_webView1);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginsEnabled(true);
        settings.setDomStorageEnabled(true);
        
		int tocUniqueId = 1;
        try {
        	tocUniqueId = (int)(Integer) getIntent().getSerializableExtra("se.chalmers.threebook.selectedTocId");
        } catch (NullPointerException e){
        	// T_T
        	Log.d("3", "Caught null pointer in init, urgh. let's make a book.");
			try {
				final AssetManager assetManager = getAssets();
				InputStream epubInputStream = assetManager.open("books/"+BOOK_URL);
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
			} catch (IOException e2){
				Log.d("3", "Caught IOE in init, heh.");
			}
        }
        
        nextChap = (Button) findViewById(R.id.btn_nextChapter);
	    prevChap = (Button) findViewById(R.id.btn_prevChapter);
	    showToc = (Button) findViewById(R.id.btn_displayToc);
		
	    // TODO : replace with abstract factory later on
	    ContentStream stream = new EpubContentStream(MyBook.get().book());
	    
	    
		try {
			displayChapter(stream.jumpTo(tocUniqueId));
		} catch (IOException e) {
			Log.d("3", "IOE from displayChapter: " + e.getMessage());
		}
		
		showToc.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent tocIntent = new Intent(view.getContext(), TocActivity.class);
				int GET_SECTION_REFERENCE = 1;
				startActivityForResult(tocIntent, GET_SECTION_REFERENCE);
			}
		});
		
		class NewOCL implements OnClickListener {

			ContentStream stream;
			boolean forward; 
			
			public NewOCL(ContentStream stream, boolean forward){
				this.stream = stream;
				this.forward = forward;
			}
			
			public void onClick(View v) {
				try {
					displayChapter(forward ? stream.next() : stream.previous());
				} catch (IOException e) {
					Log.d("3", "Could not display chapter in listener");
					e.printStackTrace();
				}
			}
		}
		
		nextChap.setOnClickListener(new NewOCL(stream, true));
		prevChap.setOnClickListener(new NewOCL(stream, false));
		
	}
		
	
	public void displayChapter(String chapter) throws IOException{
			Log.d("3", "here's a chapter...");
			view.loadData(chapter, "application/xhtml+xml", "UTF-8");
	}
}
