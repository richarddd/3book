package se.chalmers.threebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.adapters.BookNavAdapter;
import se.chalmers.threebook.adapters.BookPageAdapter;
import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.ui.BookFlipper;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.ui.util.BookNavItem;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Picture;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends ActionBarActivity {

	private WebView webView;
	private Boolean selectionMode = false;
	private float screenWidth;
	private float screenHeight;
	private ProgressDialog dialog;
	private int viewHeight = 0;

	private boolean menuShown = false;
	private RelativeLayout layoutOverlay;
	private HorizontalListView chapterListView;
	private BookNavAdapter chapterAdapter;
	private BookPageAdapter pagerAdapter;
	private BookFlipper bookFlipper;
	private boolean setupLayout = true;
	private int currentPosition = 0;
	private boolean endOfFile = false;

	private ContentStream stream = null;


	public enum IntentType {
		READ_BOOK_NOT_IN_LIBRARY, READ_BOOK_FROM_LIBRARY, GO_TO_TOC_INDEX;
	}

	public enum IntentKey {
		INTENT_TYPE("MYINTENTTYPE"),
		TOC_INDEX("GETTOCINDEX"),
		TOC_ANCHOR("GETTOCANCHOR"),		// optional anchor information 
		FILE_PATH("GETFILETYPE");
		
		private String id;
		private static String PACKAGE = "se.chalmers.threebook.";

		private IntentKey(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return PACKAGE + id;
		}
	}
	
	public void display(int index){
		display(index, "");
	}
	
	public void display(int index, String anchor){
		anchor = anchor == null ? "" : anchor;
		Log.d("3", "Trying to jump via toc. Index: " + index +", Anchor: " + anchor);
		
		// Scroll the chapter picker to an appropriate location
		if (chapterListView.getChildAt(0) != null){
			int chapWidth = chapterListView.getChildAt(0).getWidth();
			//chapterListView.scrollTo(0);
			//chapterListView.scrollTo((int)(chapWidth*(index-0.5))); // 1 to counter center-stuffies
			chapterListView.scrollTo((int)(chapWidth*(index-1))); // 1 to counter center-stuffies
			chapterAdapter.setCurrentChapterNumber(index, MyBook.get().book().getTableOfContents().size());
		}
		try { 
			String curUrl = "file:///"+stream.jumpTo(index)+"#"+anchor;
			Log.d("3", "time to load url into view! url is: " + curUrl);
			
			
			webView.loadUrl(curUrl);
			
			Log.d("3", "loading done, lol wut?");
		} catch (FileNotFoundException e){
			Log.d("3", "FNFE in display: " + e.getMessage());
		} catch (IOException e){
			Log.d("3","IOE in display: " + e.getMessage());
		}

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
			screenHeight = display.getHeight();
		}
		setFullScreen(true);

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		webView = (WebView) findViewById(R.id.web_book);
		layoutOverlay = (RelativeLayout) findViewById(R.id.lay_book_overlay);

		chapterListView = (HorizontalListView)findViewById(R.id.lst_chapters);
		bookFlipper = (BookFlipper) findViewById(R.id.pgr_book);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JsInterface(), "application");
		
		chapterAdapter = new BookNavAdapter(this, chapterListView);
		chapterAdapter
				.setChapterNameTextView((TextView) findViewById(R.id.txt_book_nav_chapter_title));
		chapterAdapter
				.setChapterNoTextView((TextView) findViewById(R.id.txt_book_nav_chapter_no));

		
		/* This is the adapter for each individual chapter in the list */
		chapterAdapter.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				dialog.show();
				display((int)id);
				menuShown = false;
				showOverlay(false);
				
			}

		});

		pagerAdapter = new BookPageAdapter(this);
		bookFlipper.setAdapter(pagerAdapter);
		chapterListView.setAdapter(chapterAdapter);
		chapterListView.setOnScrollListener(chapterAdapter);

		bookFlipper
				.setPreViewSwitchedListener(bookFlipper.new PreViewSwitchedListener() {

					@Override
					public void onPreViewSwitched(int targetIndex) {
						Log.d("ReadActivity",
								"Targetindex: " + String.valueOf(targetIndex));
						if (targetIndex > 0) {
							if (targetIndex > currentPosition) {
								int scrollTo = viewHeight * (targetIndex + 1);
								/*
								 * if(scrollTo >=
								 * webView.getContentHeight()+(viewHeight*2)){
								 * endOfFile = true; }
								 */
								Log.d("ReadActivity",
										"Content height: "
												+ String.valueOf(webView
														.getContentHeight()));
								webView.scrollTo(0, scrollTo);
								Log.d("ReadActivity", "Next scroll to: "
										+ String.valueOf(scrollTo));
								pagerAdapter.setNext(Helper
										.getBitmapFromView(webView));
							} else {
								endOfFile = false;
								int scrollTo = viewHeight * (targetIndex - 1);
								webView.scrollTo(0, scrollTo);
								Log.d("ReadActivity", "Prev scroll to: "
										+ String.valueOf(scrollTo));
								pagerAdapter.setPrevious(Helper
										.getBitmapFromView(webView));
							}

						}
						currentPosition = targetIndex;
					}
				});

		bookFlipper.setOnTouchListener(new OnTouchListener() {

			private float lastDownX;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					lastDownX = event.getX();
					break;
				case MotionEvent.ACTION_UP:
					float diff = event.getX() / screenWidth;
					float diffOld = lastDownX / screenWidth;
					if (diff <= 0.33 && diffOld <= 0.33 && !menuShown) { // left
						prevPage();
					} else if (diff >= 0.66 && diffOld >= 0.66 && !menuShown) {// right
						nextPage();
					} else if (diff < 0.66 && diff > 0.33 && diffOld < 0.66
							&& diffOld > 0.33) { // middle
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

		dialog = ProgressDialog.show(this, "",
				this.getString(R.string.loading_please_wait), true);

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		webView.setPictureListener(new PictureListener() { // XXX Fokkat!
			public void onNewPicture(WebView view, Picture picture) {
				view.setPictureListener(null);
			}
		});

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				if (setupLayout) {
					setupLayout = false;
					viewHeight = view.getHeight();
					FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
							.getLayoutParams();

					layoutParams.setMargins(0, 0, 0,
							(int) (viewHeight % (18 * 1.5)));
					viewHeight -= viewHeight % (18 * 1.5);
					view.setLayoutParams(layoutParams);
					view.requestLayout();
				}
				
				if (currentPosition != 0){ 
					bookFlipper.setSelection(0);
				}

				pagerAdapter.setCurrent(Helper.getBitmapFromView(view));
				view.scrollTo(0, view.getScrollY() + viewHeight);
				pagerAdapter.setNext(Helper.getBitmapFromView(view));
				pagerAdapter.notifyDataSetInvalidated();
				pagerAdapter.notifyDataSetChanged();
				dialog.dismiss();

			}
		});

		webView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return selectionMode ? false
						: (event.getAction() == MotionEvent.ACTION_MOVE);
			}
		});

		
		// Handle Schtuff
		IntentType type = (IntentType) getIntent().getSerializableExtra(IntentKey.INTENT_TYPE.toString());
		
		switch (type){
		case READ_BOOK_NOT_IN_LIBRARY:
			break;
		case READ_BOOK_FROM_LIBRARY:
			Log.d("3", "reading from files and shit!");
			
			AssetManager assetManager = getAssets();
			String fileName = (String) getIntent().getSerializableExtra(IntentKey.FILE_PATH.toString());
			//int lastIndex = getFromDatabase.lastChapterForUserDude(); // TODO implement this plz
			int lastIndex = 5;

			
			try { // Open and store ye olde book
				long t1 = System.currentTimeMillis();
				InputStream epubInputStream = assetManager.open("books/"
						+ fileName);
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
				stream = new EpubContentStream(MyBook.get().book(), this);
				long t2 = System.currentTimeMillis();
				Log.d("3", "opening book took " + (t2-t1) + "ms.");
			
			} catch (FileNotFoundException e){
				Log.e("3", "ReadActivity FNFE: " + e.getMessage() );
			} catch (IOException e) {
				Log.e("3", "ReadActivity IOE: " + e.getMessage() );
			}
			
			// Set up listeners and data for the overlay chapter-scroller
			List<BookNavItem> chapters = chapterAdapter.getItems();
			for (String title : stream.getToc()) {
				chapters.add(new BookNavItem(title, null));
			}
			((TextView) findViewById(R.id.txt_book_nav_chapter_no)).setText(lastIndex + "/" + stream.getToc().size());
			
			
			display(lastIndex);
			break;
		case GO_TO_TOC_INDEX:
			int id = (int)(Integer) getIntent().getSerializableExtra(IntentKey.TOC_INDEX.toString());
			String anchor = (String) getIntent().getSerializableExtra(IntentKey.TOC_ANCHOR.toString());
			display(id, anchor);
			break;
		}
		
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

	private void nextPage() {
		if (!endOfFile) {
			bookFlipper.nextScreen();
		} else {
			// /TODO nextChapter
		}
	}

	private void prevPage() {
		bookFlipper.prevousScreen();
		// bookFlipper.setSelection(bookFlipper.getSelectedItemPosition()-1);
		// bookFlipper.setCurrentItem(0);
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
			// setFullScreen(false);
			Intent tocIntent = new Intent(webView.getContext(),
					TocActivity.class);
			int GET_SECTION_REFERENCE = 1;
			startActivityForResult(tocIntent, GET_SECTION_REFERENCE);

			break;
		
		case R.id.menu_overflow:
			webView.zoomIn();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
		class JsInterface {
			public void fireImageIntent(String fileName){
				Log.d("3", "Firing external image intent!");
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(fileName), "image/*");
				startActivity(intent);
				Log.d("3", "Done firing external image intent");
			}
		}
	
}