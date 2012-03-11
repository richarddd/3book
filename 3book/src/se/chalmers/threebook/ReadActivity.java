package se.chalmers.threebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import nl.siegmann.epublib.epub.EpubReader;

import se.chalmers.threebook.adapters.BookNavAdapter;
import se.chalmers.threebook.adapters.PagerAdapter;
import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.ui.ActionBarFragmentActivity;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.MaxTextView;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.ui.fragments.BookImageFragment;
import se.chalmers.threebook.ui.util.BookNavItem;
import se.chalmers.threebook.util.WriterHelper;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Interpolator;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends ActionBarFragmentActivity implements ViewPager.OnPageChangeListener {

	private WebView webView;
	private FrameLayout layout;
	private Boolean selectionMode = false;
	//private ImageView imgBook;
	private float screenWidth;
	private float screenHeight;
	private LinkedList<Bitmap> forwardCache = new LinkedList<Bitmap>();
	private LinkedList<Bitmap> backwardCache = new LinkedList<Bitmap>();
	private int maxCacheCount = 2;
	private String total;
	private ProgressDialog dialog;
	private int viewHeight = 0;
	private MaxTextView txtView;

	private boolean menuShown = false;
	private RelativeLayout layoutOverlay;
	private HorizontalListView chapterListView;
	private BookNavAdapter chapterAdapter;
	private PagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private Scroller scroller;
	private Field mScroller;

	private String curUrl = "";
	private String curAnchor = "";

	public enum IntentType {
		READ_BOOK_NOT_IN_LIBRARY,
		READ_BOOK_FROM_LIBRARY,
		GO_TO_TOC_INDEX;
	}
	
	public enum IntentKey {
		INTENT_TYPE("MYINTENTTYPE"),
		TOC_INDEX("GETTOCINDEX"),
		TOC_ANCHOR("GETTOCANCHOR"),		// optional anchor information 
		FILE_PATH("GETFILETYPE");
		
		
		private String id;
		private static String PACKAGE = "se.chalmers.threebook.";
		
		private IntentKey(String id){
			this.id = id;
		}
		
		@Override
		public String toString(){
			return PACKAGE + id;
		}
	}

	/** TODO XXX: refactor this method to somewhere nicer! */
	public void goToToc(int index){
		Intent displayChapter = new Intent(this, ReadActivity.class);

		displayChapter.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(), ReadActivity.IntentType.GO_TO_TOC_INDEX);
		displayChapter.putExtra(ReadActivity.IntentKey.TOC_INDEX.toString(), index);
		startActivity(displayChapter);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if (Helper.SupportsNewApi()) {
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		} else {
			screenWidth = display.getWidth();
			screenHeight = display.getWidth();
		}
		setFullScreen(true);

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		scroller = new Scroller(this);

		webView = (WebView) findViewById(R.id.web_book);
		layout = (FrameLayout) findViewById(R.id.lay_read);
		//imgBook = (ImageView) findViewById(R.id.img_book);
		layoutOverlay = (RelativeLayout) findViewById(R.id.lay_book_overlay);
		chapterListView = (HorizontalListView)findViewById(R.id.lst_chapters);
		viewPager = (ViewPager)findViewById(R.id.pgr_book);
		
		// Handle Schtuff
		IntentType type = (IntentType) getIntent().getSerializableExtra(IntentKey.INTENT_TYPE.toString());
		ContentStream stream = null;
		
		switch (type){
		case READ_BOOK_NOT_IN_LIBRARY:
			
			break;
		case READ_BOOK_FROM_LIBRARY:
			Log.d("3", "reading from files and shit!");
			AssetManager assetManager = getAssets();
			String fileName = (String) getIntent().getSerializableExtra(IntentKey.FILE_PATH.toString());
			
			try {
				InputStream epubInputStream = assetManager.open("books/"+fileName);
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
				stream = new EpubContentStream(MyBook.get().book(), this);
				
				//webView.loadData(stream.jumpTo(0), "application/xhtml+xml", "UTF-8");
				webView.loadUrl("file:///"+stream.jumpTo(0));
			} catch (FileNotFoundException e){
				Log.e("3", "ReadActivity FNFE: " + e.getMessage() );
			} catch (IOException e) {
				Log.e("3", "ReadActivity IOE: " + e.getMessage() );
			} 
			break;
		case GO_TO_TOC_INDEX:
			int id = (int)(Integer) getIntent().getSerializableExtra(IntentKey.TOC_INDEX.toString());
			curAnchor = (String) getIntent().getSerializableExtra(IntentKey.TOC_ANCHOR.toString());
			stream = new EpubContentStream(MyBook.get().book(), this);
			try {
				Log.d("3", "Trying to jump via toc. Anchor: " + curAnchor);
				Log.d("3", "olololol blankus indianer");
				//curUrl = "file:///"+stream.jumpTo(id)+"#"+(curAnchor == null? "" : curAnchor);
				curUrl = "file:///"+stream.jumpTo(id);
				Log.d("3", "Full URL: " + curUrl);
				
				webView.loadUrl(curUrl);
				//webView.loadData(stream.jumpTo(id), "application/xhtml+xml", "UTF-8");
			} catch (IOException e) {
				Log.e("3", "IOE: IOE: could not display chapter: " + e.getMessage());
			}
			break;
		}
		
		
		mScroller = null;
		try {
			mScroller = ViewPager.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			mScroller.set(viewPager, scroller);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		chapterAdapter = new BookNavAdapter(this, chapterListView);
		chapterAdapter.setChapterNameTextView((TextView) findViewById(R.id.txt_book_nav_chapter_title));
		
		List<BookNavItem> chapters = chapterAdapter.getItems();
		for (String title : stream.getToc()){
			chapters.add(new BookNavItem(title, null));
		}
				
		/* This is the adapter for each individual chapter in the list */
		chapterAdapter.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Log.d("3", "Chapter click registered. pos/id: " + position + "/" + id);
				goToToc((int)id);
			}
			
		});
		
		
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, BookImageFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, BookImageFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, BookImageFragment.class.getName()));
		
		pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(this);
		
		//Log.d("Size Of fragments", String.valueOf(pagerAdapter.getFragments().size()));
		
		
		
		chapterListView.setAdapter(chapterAdapter);
		chapterListView.setOnScrollListener(chapterAdapter);

		viewPager.setOnTouchListener(new OnTouchListener() {
			
			private float lastDownX;
			
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					lastDownX = event.getX();
					break;
				case MotionEvent.ACTION_UP:		
					float diff = event.getX() / screenWidth;
					float diffOld = lastDownX / screenWidth;
					if (diff <= 0.33 && diffOld <= 0.33) { // left
						prevPage();
					} else if (diff >= 0.66 && diffOld >= 0.66) {// right
						nextPage();
					} else if(diff < 0.66 && diff > 0.33 && diffOld < 0.66 && diffOld > 0.33) { // middle
						menuShown = !menuShown;
						if (menuShown) {
							showOverlay(false);
						} else {
							showOverlay(true);
						}
					}
					break;
				default:
					break;
				}
				return false;
			}

		});

		//webView.loadUrl("file:///android_asset/lorem.html");

		dialog = ProgressDialog.show(this, "",
				this.getString(R.string.loading_please_wait), true);

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		webView.setPictureListener(new PictureListener() { // XXX Fokkat!

			public void onNewPicture(WebView view, Picture picture) {
				
				view.setPictureListener(null);
				
				forwardCache.add(getBitmapFromView(view));
				
				generateWebViewCache(true);
				
				((BookImageFragment)pagerAdapter.getItem(0)).getImage().setImageBitmap(forwardCache.getFirst());
				((BookImageFragment)pagerAdapter.getItem(1)).getImage().setImageBitmap(forwardCache.getFirst());
				((BookImageFragment)pagerAdapter.getItem(2)).getImage().setImageBitmap(forwardCache.get(1));
				
				pagerAdapter.notifyDataSetChanged();
				//pagerAdapter.
				
				
				dialog.dismiss();
				//generateWebViewCache(true);
			}
		});

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d("3", "in OPF!");
				if (curUrl != "" && !curUrl.contains("#")){ // XXX reload fix attempt make less retarded plz
					Log.d("3", "in if=true!");
					webView.loadUrl("javascript:document.getElementById('#"+curAnchor+"').scrollIntoView(true);");
					//view.loadUrl("javascript:document.getElementById('#"+curAnchor+"').scrollIntoView(true);");
					curUrl = curUrl+"#"+curAnchor;
					Log.d("3", "current url after fix: " + curUrl);
				}
				viewHeight = view.getBottom();
				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
						.getLayoutParams();

				layoutParams.setMargins(0, 0, 0,
						(int) (viewHeight % (18 * 1.5)));
				viewHeight -= viewHeight % (18 * 1.5);
				view.setLayoutParams(layoutParams);
				view.requestLayout();
				view.setWebViewClient(null);
			}
		});

		webView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return selectionMode ? false
						: (event.getAction() == MotionEvent.ACTION_MOVE);
			}
		});

	}

	private void showOverlay(boolean show) {
		setFullScreen(!show);

		int visibility = show ? View.VISIBLE : View.INVISIBLE;
		int gone = show ? View.VISIBLE : View.GONE;

		if (Helper.SupportsNewApi()) {
			if (show) {
				getActionBar().show();
			} else {
				getActionBar().hide();
			}
		} else {
			((View) ((LinearLayout) findViewById(R.id.actionbar_compat))
					.getParent()).setVisibility(gone);
		}
		layoutOverlay.setVisibility(visibility);
	}
	
	private synchronized void ensureCache(){
		while (forwardCache.size() < maxCacheCount) {
			webView.scrollTo(0, webView.getScrollY() + viewHeight);
			forwardCache.add(getBitmapFromView(webView));
		}
	}

	
	private void generateWebViewCache(boolean forward) {
		// TODO Fix this, not a good implementation.
		if (forward) {
			while (forwardCache.size() < maxCacheCount) {
				webView.scrollTo(0, webView.getScrollY() + viewHeight);
				forwardCache.add(getBitmapFromView(webView));
			}
		} else {
			while (backwardCache.size() < maxCacheCount) {
				webView.scrollTo(0, webView.getScrollY() - viewHeight);
				backwardCache.addFirst(getBitmapFromView(webView));
			}
		}
	}

	private Bitmap getBitmapFromView(View view) {
		view.setDrawingCacheEnabled(true);
		view.layout(0, 0, view.getWidth(), view.getBottom());
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		return bitmap;
	}

	private void nextPage() {
		viewPager.setCurrentItem(2);
	}

	private void prevPage() {
		viewPager.setCurrentItem(0);
	}

	public void setFullScreen(boolean fullscreen) {
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		if (fullscreen) {
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		} else {
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		getWindow().setAttributes(attrs);
	}

	public void selectText() {
		selectionMode = true;
		try {
			KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
			shiftPressEvent.dispatch(webView);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean value;
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.read, menu);
		value = super.onCreateOptionsMenu(menu);

		if (Helper.SupportsNewApi()) {
			getActionBar().hide();
		} else {
			((View) ((LinearLayout) findViewById(R.id.actionbar_compat))
					.getParent()).setVisibility(View.GONE);
		}

		return value;
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
		case R.id.menu_bookmark:
			selectText();
			//setFullScreen(false);
			Intent tocIntent = new Intent(webView.getContext(), TocActivity.class);
			int GET_SECTION_REFERENCE = 1;
			startActivityForResult(tocIntent, GET_SECTION_REFERENCE);

			break; 
		}
		return super.onOptionsItemSelected(item);
	}

	public void onPageScrollStateChanged(int val) {
		
		if(val == 0){
			
			
			if(viewPager.getCurrentItem() > 1){
				List<Bitmap> forwardTemp = new LinkedList<Bitmap>();
				for (int i = 1; i < forwardCache.size(); i++) {
					forwardTemp.add(forwardCache.get(i));
				}
				if (backwardCache.size() >= maxCacheCount) {
					backwardCache.removeLast();
				}
				backwardCache.addFirst(forwardCache.get(0));
				forwardCache.clear();
				forwardCache.addAll(forwardTemp);
				generateWebViewCache(true);
				((BookImageFragment)pagerAdapter.getItem(1)).getImage().setImageBitmap(forwardCache.getFirst());
				((BookImageFragment)pagerAdapter.getItem(2)).getImage().setImageBitmap(forwardCache.get(1));
			}else if(viewPager.getCurrentItem() < 1){
				List<Bitmap> backwardTemp = new LinkedList<Bitmap>();
				for (int i = 1; i < backwardCache.size(); i++) {
					backwardTemp.add(backwardCache.get(i));
				}
				if (forwardCache.size() >= maxCacheCount) {
					forwardCache.removeLast();
				}
				forwardCache.addFirst(backwardCache.get(0));
				backwardCache.clear();
				backwardCache.addAll(backwardTemp);
				generateWebViewCache(false);
				((BookImageFragment)pagerAdapter.getItem(1)).getImage().setImageBitmap(backwardCache.getFirst());
				((BookImageFragment)pagerAdapter.getItem(0)).getImage().setImageBitmap(backwardCache.get(1));
			}
			viewPager.setCurrentItem(1);
			pagerAdapter.notifyDataSetChanged();
			Log.d("Page Scroll", "Scroll FInnished");
		}
	}
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
	}

	public void onPageSelected(int position) {
		//Log.d("Page Scroll", "onPageSelected");
	}
}