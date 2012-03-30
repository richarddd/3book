package se.chalmers.threebook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.adapters.BookNavAdapter;
import se.chalmers.threebook.adapters.BookPageAdapter;
import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.ui.FlipperView;
import se.chalmers.threebook.ui.FlipperView.ViewSwitchListener;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.ui.util.BookNavItem;
import se.chalmers.threebook.util.Helper;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends ActionBarActivity {

	private String tag = "ReadActivity";

	// private WebView webView;
	private Boolean selectionMode = false;
	private float screenWidth;
	private float screenHeight;
	private ProgressDialog dialog;

	private boolean menuShown = false;
	private RelativeLayout layoutOverlay;
	private HorizontalListView chapterListView;
	private BookNavAdapter chapterAdapter;
	private BookPageAdapter pagerAdapter;
	private FlipperView bookFlipper;
	private int currentPosition = 0;
	private boolean endOfFile = false;
	private float lastDownX;
	private boolean webviewOnTouch = false;

	private ContentStream stream = null;

	private ImageView imgPageRender;
	private HtmlRenderer render;
	private RenderedPage renderedPage;

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

		try {
			Log.d(tag, "Display called, index and anchor: " + index +","+anchor);
			Point p = Helper.getDisplaySize(this);
			if (render == null){
				long t1 = System.currentTimeMillis();
				Log.d(tag, "Initializing renderer.");
				render = new HtmlRenderer(p.x, p.y);
			render.setHtmlSource(Helper.streamToString(new FileInputStream(
					stream.jumpTo(index))));
				long t2 = System.currentTimeMillis();
				Log.d(tag, "Fetching data and rendering the HTML took " + (t2-t1) + "ms.");
			}
			if (anchor != null && anchor != ""){
				Log.d(tag, "Trying to go to anchor. Anchor: " + anchor);
				render.getRenderedPage(anchor);
			}
			
			pagerAdapter = new BookPageAdapter(this, render, bookFlipper.getSideBuffer());
			
			bookFlipper.setOnViewSwitchListener(new ViewSwitchListener() {
				public void onSwitched(View view, int position) {
					renderedPage = pagerAdapter.getItem(0);
					if(renderedPage != null){
						imgPageRender.setImageBitmap(renderedPage.getBitmap());
					}
					imgPageRender.setVisibility(View.VISIBLE);

				}
			});

			bookFlipper.setAdapter(pagerAdapter, BookPageAdapter.START_POSITION);
			
			renderedPage = pagerAdapter.getItem(0);
			imgPageRender.setImageBitmap(renderedPage.getBitmap());
			
		} catch (FileNotFoundException e) {
			Log.d("3", "FNFE in display: " + e.getMessage());
		} catch (IOException e) {
			Log.d("3", "IOE in display: " + e.getMessage());
		}
		
		// XXX remember to remove this shit
		dialog.dismiss();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);

		Log.d("ReadActivity", "View is created");

		Point size = Helper.getDisplaySize(this);
		screenWidth = size.x;
		screenHeight = size.y;
		setFullScreen(true);

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		layoutOverlay = (RelativeLayout) findViewById(R.id.lay_book_overlay);

		imgPageRender = (ImageView) findViewById(R.id.img_page_render);

		// bookView = (BookView) findViewById(R.id.view_book_view);

		chapterListView = (HorizontalListView) findViewById(R.id.lst_chapters);
		bookFlipper = (FlipperView) findViewById(R.id.pgr_book);

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
				Log.d(tag, "Clicking in chapscroller, id is: " + stream.getToc().getTocReferences().get((int)id).getId());
				display((int) id, stream.getToc().getTocReferences().get((int)id).getId());
				showOverlay(false);

			}

		});

		// TODO longclick fastflipp
		imgPageRender.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				Log.d("bookView", "long press invoked");
				return true;
			}
		});

		chapterListView.setAdapter(chapterAdapter);
		chapterListView.setOnScrollListener(chapterAdapter);

		dialog = ProgressDialog.show(this, "",
				this.getString(R.string.loading_please_wait), true);

		

		if (currentPosition != 0) {
			pagerAdapter.notifyDataSetInvalidated();
			bookFlipper.setSelection(0);
		}

		imgPageRender.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (!menuShown) {
					bookFlipper.onTouchEvent(event);
				}

				switch (event.getAction()) {

				case MotionEvent.ACTION_MOVE:
					if (!menuShown) {
						if (imgPageRender.getVisibility() != View.INVISIBLE) {
							imgPageRender.setVisibility(View.INVISIBLE);
						}
					}
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
				/*
				 * InputStream epubInputStream = assetManager.open("books/" +
				 * fileName);
				 */
				MyBook.setBook(new EpubReader().readEpub(epubInputStream));
				// stream = new EpubContentStream(MyBook.get().book(), this);
				stream = new EpubContentStream(MyBook.get().book(),
						getCacheDir());
				long t2 = System.currentTimeMillis();
				Log.d("3", "opening book took " + (t2 - t1) + "ms.");

			} catch (FileNotFoundException e) {
				Log.e("3", "ReadActivity FNFE: " + e.getMessage());
			} catch (IOException e) {
				Log.e("3", "ReadActivity IOE: " + e.getMessage());
			}

			// Set up listeners and data for the overlay chapter-scroller

			List<BookNavItem> chapters = chapterAdapter.getItems();
			for (String title : stream.getTocNames()) {
				chapters.add(new BookNavItem(title, null));
			}

			((TextView) findViewById(R.id.txt_book_nav_chapter_no))
					.setText(lastIndex + "/" + stream.getToc().size());

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

	private void fastFlip(int direction) {
		new Runnable() {
			public void run() {
				int loopCount = 0;
				while (webviewOnTouch) {
					try {
						int sleepVal = 500 - (25 * loopCount);
						Log.d("Should sleep in fastflip as", "Sleep value = "
								+ String.valueOf(sleepVal));
						Thread.sleep(sleepVal);
						// /XXX penis
						Toast.makeText(ReadActivity.this,
								String.valueOf(loopCount + 1),
								Toast.LENGTH_SHORT).show();
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
			imgPageRender.setVisibility(View.INVISIBLE);
			bookFlipper.nextScreen();
		} else {
			// /TODO nextChapter
		}
	}

	private void prevPage() {
		imgPageRender.setVisibility(View.INVISIBLE);
		bookFlipper.prevousScreen();
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
			break;
		case R.id.menu_zoom_out:
			showOverlay(false);
			break;
		case R.id.menu_table_of_contents:
			Intent tocIntent = new Intent(this, TocActivity.class);
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

}