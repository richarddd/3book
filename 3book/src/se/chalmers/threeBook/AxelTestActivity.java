package se.chalmers.threeBook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import parser.epub.ImgRewriter;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class AxelTestActivity extends Activity {
/** Called when the activity is first created. */
	
	public static final String HTML_START = "<html><head></head><body>\n"; 
	public static final String HTML_END = "\n</body></html>";
	public static final String BOOK_URL = "pg11.epub";
	//public static final String BOOK_URL = "ub-EKJV.epub";
	
	private List<Resource> chapters = new ArrayList<Resource>(20);
	public JsInterface api;
	private WebView view;
	public Book book;
	
	private Button nextChap, prevChap, showToc;
	private int curChap = 1;
	private int maxChap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        view = (WebView) findViewById(R.id.web_webView1);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginsEnabled(true);
        settings.setDomStorageEnabled(true);
        
        final AssetManager assetManager = getAssets();
        
        view.setVerticalScrollBarEnabled(false);
        view.setBackgroundColor(Color.GREEN);

        api = new JsInterface(); 
        view.addJavascriptInterface(api, "jsinterface");
        
        
        nextChap = (Button) findViewById(R.id.btn_nextChapter);
        prevChap = (Button) findViewById(R.id.btn_prevChapter);
        showToc = (Button) findViewById(R.id.btn_displayToc);
        
        nextChap.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AxelTestActivity moi = AxelTestActivity.this;
        		try {
        			if ((moi.curChap+1) < maxChap){
        				Log.d("3", "Displaying next chapter");
        				long t1 = System.currentTimeMillis();
        				moi.displayChapter(++moi.curChap);
        				Log.d("3", "took " + (System.currentTimeMillis()-t1) + "ms");
        			} else {
        				Log.d("3", "Final chapter reached, no new display.");
        			}
					
				} catch (IOException e) {
					Log.d("3", "IOE in nextChap: " + e.getMessage());
				}
        		
        	}
        });
        
        prevChap.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AxelTestActivity moi = AxelTestActivity.this;
        		try {
        			if ((moi.curChap-1) >= 0){
        				Log.d("3", "Displaying previous chapter");
        				long t1 = System.currentTimeMillis();
        				moi.displayChapter(--moi.curChap);
        				Log.d("3", "took " + (System.currentTimeMillis()-t1) + "ms");
        			} else {
        				Log.d("3", "First chapter reached, no new display.");
        				Log.d("3", "Value is: " + getBodyContents(getChapter(0)));
        			}
					
				} catch (IOException e) {
					Log.d("3", "IOE in nextChap: " + e.getMessage());
				}
        		
        	}
        });
        
        showToc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent tocIntent = new Intent(view.getContext(), TocActivity.class);
				int GET_SECTION_REFERENCE = 1;
				tocIntent.putExtra("se.chalmers.threebook.TableOfContents", book.getTableOfContents());
				tocIntent.putExtra("se.chalmers.threebook.TheBook", book);
				startActivityForResult(tocIntent, GET_SECTION_REFERENCE);
			}
		});
        
        
        view.loadUrl("file:///android_asset/container.html");
        view.setWebChromeClient(new WebChromeClient() {
        	@Override
        	public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
        		new AlertDialog.Builder(AxelTestActivity.this).setTitle("sdgs").setMessage(message).setPositiveButton("OK", null).create().show();
				return true;
        	}
        });
		// read epub file
		EpubReader epubReader = new EpubReader();
		
		try {
			InputStream epubInputStream = assetManager.open("books/"+BOOK_URL);
			book = epubReader.readEpub(epubInputStream);
			// print the first title
			
    		List<String> titles = book.getMetadata().getTitles();
    		
    		
    		Log.d("3", "book title:" + (titles.isEmpty() ? "book has no title" : titles.get(0)));
    		Log.d("3", "Before getting chappy");
    		
    		// Set max chapter
    		this.maxChap = book.getSpine().size();
    		
    		try {
    			long t1 = System.currentTimeMillis();
    			String text = getChapter(1);
    			Log.d("3", "After getting chappy, time to grab the body contents:");
    			int end = text.indexOf("</body>");
    			int st1 = text.indexOf("<body");
    			int st2 = text.indexOf(">", st1);
    			Log.d("3", "substrings: st1="+st1+", st2="+st2+", end="+end);
    			String contents = text.substring(st2+1, end);
    			Log.d("3", "Start of contents: " + contents.substring(0, 100));
    			Log.d("3", "end of contents: " + contents.substring(contents.length()-100, contents.length()));
    			
    			Log.d("3", "ugly string shenanigans took: " + (System.currentTimeMillis() - t1) + "ms");
    			//view.loadData(contents, book.getSpine().getResource(1).getMediaType().getName(), book.getSpine().getResource(1).getInputEncoding());
    			view.loadData(text, book.getSpine().getResource(1).getMediaType().getName(), book.getSpine().getResource(1).getInputEncoding());
    		} catch (Exception e){
    			Log.d("3", "Exception: " + e.getMessage() + ", of type:" + e.getClass());
    		}    		
		} catch (FileNotFoundException e) {
			Log.d("3", "FNFE: " + e.getMessage());
			Log.d("3", "trace: " + e.getStackTrace());
		} catch (IOException e) {
			Log.d("3", "IOE: " + e.getMessage());
			Log.d("3", "trace: " + e.getStackTrace());
		}
		
    }
    
    public String getChapter(int index) throws IOException{
    	Spine spine = book.getSpine();
    	Resource chap = spine.getResource(index);
    	if (chap.getMediaType().getName().equals("application/xthml+xml")){
    		throw new IllegalArgumentException("Spine content was not xthml, wtf? Media type: " + chap.getMediaType().getName());
    	}
    	char[] cb = new char[(int) chap.getSize()]; // XXX size is long, this could go bad-bad?
    	Reader reader = chap.getReader();
    	
    	int charsRead = reader.read(cb);
    	Log.d("3", "Chapter encoding: " + chap.getInputEncoding());
    	Log.d("3", "Media type: " + chap.getMediaType().getName());
    	Log.d("3", "read " + charsRead + "bytes into buffer of size" + cb.length + ".");
    	return String.copyValueOf(cb);
    }
    
    public void displayChapter(int index) throws IOException{
    	String text = getChapter(index);
    	Log.d("3", "displayChapter: Chapter refID: " + book.getSpine().getResource(index).getId());
    	StringBuilder sb = new StringBuilder();
    	sb.append(HTML_START).append(getBodyContents(text)).append(HTML_END);
    	Log.d("3", "Media type: " + book.getSpine().getResource(index).getMediaType().getName() + ", enc: " + book.getSpine().getResource(index).getInputEncoding());
//file://mnt/sdcard/epub/OEBPS/
    	//String rewritten = ImgRewriter.rewriteImages(sb.toString(), "file:///android_asset/books/");
    	//Log.d("3", "rewritten string: " + rewritten);
    	//view.loadDataWithBaseURL("file:///android_asset/", rewritten, book.getSpine().getResource(index).getMediaType().getName(), book.getSpine().getResource(index).getInputEncoding(), "");
    	//view.loadData(rewritten, book.getSpine().getResource(index).getMediaType().getName(), book.getSpine().getResource(index).getInputEncoding());
    	//view.loadDataWithBaseURL("file:///android_asset/",sb.toString(), book.getSpine().getResource(index).getMediaType().getName(), book.getSpine().getResource(index).getInputEncoding(), "");
    	view.loadData(sb.toString(), book.getSpine().getResource(index).getMediaType().getName(), book.getSpine().getResource(index).getInputEncoding());
    	//view.loadData(text, book.getSpine().getResource(index).getMediaType().getName(), book.getSpine().getResource(index).getInputEncoding());
    	//Log.d("3", "URL is: " + view.getUrl());
    }
    
    private String getBodyContents(String xhtml){
    	int end = xhtml.indexOf("</body>");
		int st1 = xhtml.indexOf("<body");
		int st2 = xhtml.indexOf(">", st1);
		return xhtml.substring(st2+1, end);
    }
    
    public void displayToc(){
    	List<TOCReference> ref = book.getTableOfContents().getTocReferences();
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html><head></head><body>\n");
    	sb.append("<script>function gotoSection(refid, id){\n" +
//    		"alert('got click! id:' + id); \n" +
    		"window.jsinterface.lol(); \n" +
    		"window.jsinterface.navToSection(refid, id);\n" +	
    	 "return; }</script>");
    	sb.append("<ol>\n");
    	int i = 0;
    	for (TOCReference r : ref){
    		addLi(r.getTitle(), r.getResourceId(), sb, i++);
    	}
    	sb.append("</ol>\n");
    	sb.append("</body></html>");
    	Log.d("3", "TOC html: " + sb.toString());
    	view.loadData(sb.toString(), "text/html", "UTF-8");
    }
    
    private StringBuilder addLi(String str, String refId, StringBuilder sb, int count){
    	return sb.append("\t<li><a href=\"javascript:gotoSection('"+refId+","+count+"')\">").append(str).append("</a></li>\n");
    }
    
    class JsInterface {
    	
    	private String[] words = {
			"Hej", 
			"Lorem ipsun dolor sit amet ich kommer inte ihåg mer av den gamla godningen men men men", 
			"Hörru", 
			"Kompis", 
			"bruschaaaan", 
			"punkt . punkt . punkt .a . a.sd .asd .sa.das.dsa.d.asd.asd.sad.dsa..dsa.sad.ads........ . ", 
			".a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v................................................................................................................................................................."
    	};
    	
    	private int wc = 0;
    	
    	private long t1 = 0, t2 = 0;
    	private int row = 0;
    	private boolean overflow = false;
    	
    	public void navToSection(String refId, String id){
    		int spineResIndex = -1; 
    		int firstResById = -1;
    		
    		Log.d("3", "navToSection called! refId: " + refId + ", id: " + id);
    		boolean containsId = book.getResources().containsId(refId);
    		boolean containsHref = book.getResources().containsByHref(refId);
    		spineResIndex = book.getSpine().getResourceIndex(refId);
    		firstResById = book.getSpine().findFirstResourceById(refId);
    		Log.d("3", "Does the book contain this by id? " + containsId + ", by href?: " + containsHref);
    		Log.d("3", "Spine resource index? " + spineResIndex +", firstResById? : " + firstResById);
    	}
    	
    	public boolean isOverflow(){
			return overflow;
		}
    	
    	public void sendData(String str, int length){
    		Log.d("3", "Sending date to JS-side..");
    		Log.d("3", "Data being sent: " + str);
    		//view.loadUrl("javascript:sendData('"+str+"','"+length+"')");
    		view.loadUrl("javascript:sendData('"+str+"')");
    	}

		public void setOverflow(){
    		Log.d("flow", "overflow is overflows");
    		overflow = true;
    	}
		
		public void loadNext(){
			row++;
			view.loadUrl("javascript:addContent('<p>Hej hej! "+row+"</p>')");
		}
		
		public void loadStringPile(){
			StringBuilder sb = new StringBuilder();
			int count = 0;
			String word = words[wc % words.length]; 
			// VARY i to send different lenghts.
			for (int i = 0; i <= 7; i++){
				sb.append("<p>" + word + " " + i + "!</p> .");
				count++;
			}
			
			String s = sb.toString();
			Log.d("3", s.length() + " characters sent...");
			t1 = System.currentTimeMillis();
			view.loadUrl("javascript:addStringPile('"+s+"')");
			wc++;
		}
		
		public void lol(){
			Log.d("3", "lol!");
		}
		
		public void clicked(){
			Log.d("3", "i was clicked");
		}
		
		public void stopTimer(){
			Log.d("3", "finished displaying lines");
			Log.d("3", "took " + (System.currentTimeMillis() - t1) + "ms");
		}

    }
}