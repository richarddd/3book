package se.chalmers.threebook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
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
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends ActionBarActivity {

	private WebView webView;
	private Boolean selectionMode = false;
	private float screenWidth;
	private float screenHeight;
	private ProgressDialog dialog;
	private int viewHeight = 0;
	private static final float WEBVIEW_TRANSPARENCY_VALUE = 0.5f;

	private boolean menuShown = false;
	private RelativeLayout layoutOverlay;
	private HorizontalListView chapterListView;
	private BookNavAdapter chapterAdapter;
	private BookPageAdapter pagerAdapter;
	private BookFlipper bookFlipper;
	private boolean setupLayout = true;
	private int currentPosition = 0;
	private boolean endOfFile = false;
	private float lastDownX;
	private boolean webviewOnTouch = false;

	private ContentStream stream = null;

	public enum IntentType {
		READ_BOOK_NOT_IN_LIBRARY, READ_BOOK_FROM_LIBRARY, GO_TO_TOC_INDEX;
	}

	public enum IntentKey {
		INTENT_TYPE("MYINTENTTYPE"), TOC_INDEX("GETTOCINDEX"), TOC_ANCHOR(
				"GETTOCANCHOR"), // optional anchor information
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

	public void display(int index) {
		display(index, "");
	}

	public void display(int index, String anchor) {
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
		} catch (FileNotFoundException e) {
			Log.d("3", "FNFE in display: " + e.getMessage());
		} catch (IOException e) {
			Log.d("3", "IOE in display: " + e.getMessage());
		}

	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		
		Log.d("ReadActivity", "View is created");

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

		chapterListView = (HorizontalListView) findViewById(R.id.lst_chapters);
		bookFlipper = (BookFlipper) findViewById(R.id.pgr_book);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JsInterface(), "application");

		chapterAdapter = new BookNavAdapter(this, chapterListView);
		chapterAdapter
				.setChapterNameTextView((TextView) findViewById(R.id.txt_book_nav_chapter_title));
		chapterAdapter
				.setChapterNoTextView((TextView) findViewById(R.id.txt_book_nav_chapter_no));

		/* This is the adapter for each individual chapter in the list */
		chapterAdapter.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog.show();
				display((int) id);
				showOverlay(false);

			}

		});
		
		webView.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				
				Log.d("webview", "long press invoked");
				
				float diff = lastDownX / screenWidth;
				if(diff < 0.33){
					fastFlip(-1);
				}else if(diff > 0.66){
					fastFlip(1);
				}
				return true;
			}
		});

		pagerAdapter = new BookPageAdapter(this);
		bookFlipper.setAdapter(pagerAdapter);
		
		
		
		
		chapterListView.setAdapter(chapterAdapter);
		chapterListView.setOnScrollListener(chapterAdapter);

		bookFlipper
				.setPreViewSwitchedListener(bookFlipper.new PreViewSwitchedListener() {

					@Override
					public void onPreViewSwitched(int targetIndex, int direction) {
						webView.setVisibility(View.VISIBLE);
						if (direction != 0) {
							Log.d("ReadActivity",
									"Targetindex: "
											+ String.valueOf(targetIndex));

							// webView.setAlpha(WEBVIEW_TRANSPARENCY_VALUE);
							// bookFlipper.setVisibility(View.INVISIBLE);
							if (targetIndex > 0) {
								if (targetIndex > currentPosition) {
									int scrollTo = viewHeight
											* (targetIndex + 1);
									/*
									 * if(scrollTo >=
									 * webView.getContentHeight()+
									 * (viewHeight*2)){ endOfFile = true; }
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
									int scrollTo = viewHeight
											* (targetIndex - 1);
									webView.scrollTo(0, scrollTo);
									Log.d("ReadActivity", "Prev scroll to: "
											+ String.valueOf(scrollTo));
									pagerAdapter.setPrevious(Helper
											.getBitmapFromView(webView));
								}

							}
							webView.scrollTo(0, viewHeight * targetIndex);
							currentPosition = targetIndex;
						}
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
		

		webView.setWebViewClient(new WebViewClient(){

			@Override
			public void onPageFinished(WebView view, String url) {
				
				Log.d("asfasfasf", "Runns on finnished");
				
				if (setupLayout) {
					setupLayout = false;
					viewHeight = view.getHeight();
					FrameLayout.LayoutParams webviewParams = (FrameLayout.LayoutParams) view
							.getLayoutParams();

					FrameLayout.LayoutParams flipperParams = (FrameLayout.LayoutParams) bookFlipper
							.getLayoutParams();

					flipperParams.setMargins(0, 0, 0,
							(int) (viewHeight % (18 * 1.5)));

					webviewParams.setMargins(0, 0, 0,
							(int) (viewHeight % (18 * 1.5)));
					viewHeight -= viewHeight % (18 * 1.5);
					view.setLayoutParams(webviewParams);
					bookFlipper.setLayoutParams(flipperParams);
					view.requestLayout();
					bookFlipper.requestLayout();
					// webView.setAlpha(WEBVIEW_TRANSPARENCY_VALUE);
					// webVie

				}

				if (currentPosition != 0) {
					bookFlipper.setSelection(0);
				}

				pagerAdapter.setCurrent(Helper.getBitmapFromView(view));
				view.scrollTo(0, view.getScrollY() + viewHeight);
				pagerAdapter.setNext(Helper.getBitmapFromView(view));
				view.scrollTo(0, 0);
				pagerAdapter.notifyDataSetInvalidated();
				pagerAdapter.notifyDataSetChanged();
				dialog.dismiss();

			}
		});

		webView.setOnTouchListener(new View.OnTouchListener() {

			

			public boolean onTouch(View v, MotionEvent event) {
				
				
				if(!menuShown){
					bookFlipper.onTouchEvent(event);
				}

				switch (event.getAction()) {

				case MotionEvent.ACTION_MOVE:
					if(!menuShown){
						webView.setVisibility(View.INVISIBLE);
					}
					// bookFlipper.setVisibility(View.VISIBLE);
					return true;
				case MotionEvent.ACTION_DOWN:
					lastDownX = event.getX();
					webviewOnTouch = true;
					break;
				case MotionEvent.ACTION_UP:
					webviewOnTouch = false;
					float diff = event.getX() / screenWidth;
					float diffOld = lastDownX / screenWidth;
					if (diff <= 0.33 && diffOld <= 0.33 && !menuShown) { // left
						prevPage();
					} else if (diff >= 0.66 && diffOld >= 0.66 && !menuShown) {// right
						nextPage();
					} else if (diff < 0.66 && diff > 0.33 && diffOld < 0.66
							&& diffOld > 0.33) { // middle
						showOverlay(!menuShown);
					}
					break;
				default:
					break;
				}
				return false;
			}
		});

		// Handle Schtuff
		IntentType type = (IntentType) getIntent().getSerializableExtra(
				IntentKey.INTENT_TYPE.toString());

		switch (type) {
		case READ_BOOK_NOT_IN_LIBRARY:
			break;
		case READ_BOOK_FROM_LIBRARY:
			Log.d("3", "reading from files and shit!");
			
			AssetManager assetManager = getAssets();
			String fileName = (String) getIntent().getSerializableExtra(
					IntentKey.FILE_PATH.toString());
			// int lastIndex = getFromDatabase.lastChapterForUserDude(); // TODO
			// implement this plz
			int lastIndex = 2;

			
			try { // Open and store ye olde book
				long t1 = System.currentTimeMillis();
				InputStream epubInputStream = new FileInputStream(fileName);
				/*InputStream epubInputStream = assetManager.open("books/"
						+ fileName);*/
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
				stream = new EpubContentStream(MyBook.get().book(), this);
				long t2 = System.currentTimeMillis();
				Log.d("3", "opening book took " + (t2-t1) + "ms.");
			
			} catch (FileNotFoundException e){
				Log.e("3", "ReadActivity FNFE: " + e.getMessage() );
			} catch (IOException e) {
				Log.e("3", "ReadActivity IOE: " + e.getMessage());
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
			int id = (int) (Integer) getIntent().getSerializableExtra(
					IntentKey.TOC_INDEX.toString());
			String anchor = (String) getIntent().getSerializableExtra(
					IntentKey.TOC_ANCHOR.toString());
			display(id, anchor);
			break;
		}

	}
	
	private void fastFlip(int direction){
		new Runnable() {
			public void run() {
				
				int loopCount = 0;
				
				while(webviewOnTouch){
					try {
						int sleepVal = 500-(25*loopCount);
						Log.d("Should sleep in fastflip as", "Sleep value = "+String.valueOf(sleepVal));
						Thread.sleep(sleepVal);
						///XXX penis
						Toast.makeText(ReadActivity.this, String.valueOf(loopCount+1), Toast.LENGTH_SHORT).show();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					loopCount++;
				}
			}
		};
	}

	private void showOverlay(boolean show) {
		menuShown = show;
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
			webView.setVisibility(View.INVISIBLE);
			bookFlipper.nextScreen();
		} else {
			// /TODO nextChapter
		}
	}

	private void prevPage() {
		if (currentPosition != 0) {
			webView.setVisibility(View.INVISIBLE);
			bookFlipper.prevousScreen();
		}
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
			// Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_bookmark:
			// selectText();
			// setFullScreen(false);
			break;
		case R.id.menu_day_night_mode:
			break;
		case R.id.menu_settings:
			break;
		case R.id.menu_zoom_in:
			showOverlay(false);
			webView.zoomIn();
			break;
		case R.id.menu_zoom_out:
			showOverlay(false);
			webView.zoomOut();
			break;
		case R.id.menu_table_of_contents:
			Intent tocIntent = new Intent(webView.getContext(),
					TocActivity.class);
			int GET_SECTION_REFERENCE = 1;
			startActivityForResult(tocIntent, GET_SECTION_REFERENCE);
			break;
		case R.id.menu_overflow:
			openOptionsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!menuShown && (keyCode == 25 || keyCode == 24)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// Log.d("asfasf", String.valueOf(keyCode));
		if (keyCode == 82) {
			if (!menuShown) {
				openOptionsMenu();
				showOverlay(true);
			} else {
				showOverlay(false);
			}

			return true;
		} else if (keyCode == 4 && menuShown) {
			showOverlay(false);
			return true;
		} else if (keyCode == 25 && !menuShown) {
			prevPage();
			return true;
		} else if (keyCode == 24 && !menuShown) {
			nextPage();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	class JsInterface {
		public void fireImageIntent(String fileName) {
			Log.d("3", "Firing external image intent!");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(fileName), "image/*");
			startActivity(intent);
			Log.d("3", "Done firing external image intent");
		}
	}

}