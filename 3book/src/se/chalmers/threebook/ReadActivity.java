package se.chalmers.threebook;

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
import android.os.Bundle;
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

	public enum IntentType {
		READ_BOOK_NOT_IN_LIBRARY, READ_BOOK_FROM_LIBRARY, GO_TO_TOC_INDEX;
	}

	public enum IntentKey {
		INTENT_TYPE("MYINTENTTYPE"), TOC_INDEX("GETTOCINDEX"), FILE_PATH(
				"GETFILETYPE");

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

	/** TODO XXX: refactor this method to somewhere nicer! */
	public void goToToc(int index) {
		Intent displayChapter = new Intent(this, ReadActivity.class);

		displayChapter.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(),
				ReadActivity.IntentType.GO_TO_TOC_INDEX);
		displayChapter.putExtra(ReadActivity.IntentKey.TOC_INDEX.toString(),
				index);
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

		// Handle Schtuff
		IntentType type = (IntentType) getIntent().getSerializableExtra(
				IntentKey.INTENT_TYPE.toString());
		ContentStream stream = null;

		switch (type) {
		case READ_BOOK_NOT_IN_LIBRARY:
			break;
		case READ_BOOK_FROM_LIBRARY:
			AssetManager assetManager = getAssets();
			String fileName = (String) getIntent().getSerializableExtra(
					IntentKey.FILE_PATH.toString());

			try {
				InputStream epubInputStream = assetManager.open("books/"
						+ fileName);
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
				stream = new EpubContentStream(MyBook.get().book());
				webView.loadData(stream.jumpTo(0), "application/xhtml+xml",
						"UTF-8");
			} catch (IOException e) {
				Log.e("3", "IOE: could not open book :/ " + e.getMessage());
			}
			break;
		case GO_TO_TOC_INDEX:
			int id = (int) (Integer) getIntent().getSerializableExtra(
					IntentKey.TOC_INDEX.toString());
			stream = new EpubContentStream(MyBook.get().book());
			try {
				webView.loadData(stream.jumpTo(id), "application/xhtml+xml",
						"UTF-8");
			} catch (IOException e) {
				Log.e("3",
						"IOE: IOE: could not display chapter: "
								+ e.getMessage());
			}
			break;
		}

		chapterAdapter = new BookNavAdapter(this, chapterListView);
		chapterAdapter
				.setChapterNameTextView((TextView) findViewById(R.id.txt_book_nav_chapter_title));

		List<BookNavItem> chapters = chapterAdapter.getItems();
		for (String title : stream.getToc()) {
			chapters.add(new BookNavItem(title, null));
		}

		/* This is the adapter for each individual chapter in the list */
		chapterAdapter.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("3", "Chapter click registered. pos/id: " + position
						+ "/" + id);
				goToToc((int) id);
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
					if (diff <= 0.33 && diffOld <= 0.33) { // left
						prevPage();
					} else if (diff >= 0.66 && diffOld >= 0.66) {// right
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
		}
		return super.onOptionsItemSelected(item);
	}
}