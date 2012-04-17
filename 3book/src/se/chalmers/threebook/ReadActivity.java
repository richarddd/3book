package se.chalmers.threebook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.adapters.BookNavAdapter;
import se.chalmers.threebook.adapters.BookPageAdapter;
import se.chalmers.threebook.content.ContentStream;
import se.chalmers.threebook.content.EpubContentStream;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.html.WordPosition;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.ui.FlipperView;
import se.chalmers.threebook.ui.FlipperView.ViewSwitchListener;
import se.chalmers.threebook.ui.GalleryFix;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.ui.util.BookNavItem;
import se.chalmers.threebook.ui.util.BookNavigationRow;
import se.chalmers.threebook.util.AnimationHelper;
import se.chalmers.threebook.util.Helper;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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
	private FrameLayout layoutView;
	// private HorizontalListView chapterListView;

	// private Gallery galleryChapters;

	private BookPageAdapter pagerAdapter;
	private FlipperView bookFlipper;
	private int currentPosition = 0;
	private boolean endOfFile = false;
	private float lastDownX;
	private float lastDownY;
	private boolean touchingPage = false;
	private boolean movingPage = false;
	private int firstSelectionIndex = 0;
	// private TextView txtBookNavChapterNo;
	// private TextView txtBookNavChapterTitle

	private int navRowHeight;
	private List<BookNavigationRow> navigationList;

	private ContentStream stream = null;

	private HtmlRenderer render;
	private RenderedPage renderedPage;
	private int chapterSize;

	RelativeLayout.LayoutParams handleParams;

	// private Object navigationSelectListener;

	private int handleHeight;

	//selection stuff
	private ImageView imgBackground;
	private Canvas selectionCanvas;
	private Paint selectionPaint;
	private ImageView sectionHandleRight;
	private ImageView sectionHandleLeft;

	private Bitmap selectionBitmap;

	public enum IntentType {
		READ_BOOK_NOT_IN_LIBRARY, READ_BOOK_FROM_LIBRARY, GO_TO_TOC_INDEX;
	}

	public enum IntentKey {
		INTENT_TYPE("MYINTENTTYPE"), TOC_INDEX("GETTOCINDEX"), TOC_ANCHOR(
				"GETTOCANCHOR"), // optional
									// anchor
									// information
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

			Point p = Helper.getDisplaySize(this);
			render = new HtmlRenderer(this, p.x, p.y);
			render.setHtmlSource(Helper.streamToString(new FileInputStream(
					stream.jumpTo(index))));
			pagerAdapter = new BookPageAdapter(this, render,
					bookFlipper.getSideBuffer());
			bookFlipper.setOnViewSwitchListener(new ViewSwitchListener() {
				public void onSwitched(View view, int position) {

				}
			});
			bookFlipper
					.setAdapter(pagerAdapter, BookPageAdapter.START_POSITION);

			dialog.dismiss();

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

		layoutView = (FrameLayout) findViewById(R.id.lay_read);
		layoutOverlay = (RelativeLayout) findViewById(R.id.lay_book_overlay);
		bookFlipper = (FlipperView) findViewById(R.id.pgr_book);
		imgBackground = (ImageView) findViewById(R.id.img_book_bg);
		
		/*
		Bitmap handleBitmapRight = BitmapFactory.decodeResource(getResources(), R.styleable.);
		Bitmap handleBitmapLeft;
		FrameLayout.LayoutParams selectionParams = new FrameLayout.LayoutParams(width, height)*/
		
		sectionHandleRight = new ImageView(this);
		sectionHandleLeft = new ImageView(this);

		navigationList = new ArrayList<BookNavigationRow>(3);
		navRowHeight = (int) getResources().getDimension(
				R.dimen.book_nav_height);
		Point size = Helper.getDisplaySize(this);
		screenWidth = size.x;
		screenHeight = size.y;

		selectionBitmap = Bitmap.createBitmap((int) screenWidth,
				(int) screenHeight, Bitmap.Config.ARGB_4444);
		selectionCanvas = new Canvas(selectionBitmap);
		selectionPaint = new Paint();
		selectionPaint.setColor(Color.YELLOW);
		selectionPaint.setAntiAlias(true);
		selectionPaint.setStyle(Paint.Style.FILL);

		imgBackground.setImageBitmap(selectionBitmap);

		setFullScreen(true);

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		dialog = ProgressDialog.show(this, "",
				this.getString(R.string.loading_please_wait), true);

		if (currentPosition != 0) {
			pagerAdapter.notifyDataSetInvalidated();
			bookFlipper.setSelection(0);
		}

		bookFlipper.setLongClickable(true);
		// layoutView.setFocusable(true);

		// TODO longclick fastflipp
		bookFlipper.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (!movingPage) {
					Log.d(tag, "Long press invoked!");
					selectText();
				}
				return true;
			}
		});

		bookFlipper.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (!menuShown && !selectionMode) {
					bookFlipper.touchReceived(event);
				}

				switch (event.getAction()) {

				case MotionEvent.ACTION_MOVE:
					if (selectionMode) {
						int index = 0;
						List<WordPosition> posList = pagerAdapter.getItem(0).getPositionList();
						for (WordPosition cp : posList) {
							index++;
							if (cp.area.contains((int) lastDownX, (int) lastDownY)) {	
								break;
							}
						}
						int from = (index >= firstSelectionIndex?firstSelectionIndex:index);
						for(int i = from; (int) i < from+Math.abs(index-firstSelectionIndex);i++){
							selectionCanvas.drawRect(posList.get(i).area, selectionPaint);
							imgBackground.setImageBitmap(selectionBitmap);
						}
						
					}
					movingPage = true;
					break;
				case MotionEvent.ACTION_DOWN:
					lastDownX = event.getX();
					lastDownY = event.getY();
					touchingPage = true;
					break;
				case MotionEvent.ACTION_UP:
					movingPage = false;
					touchingPage = false;
					if (!selectionMode) {
						float diff = event.getX() / screenWidth;
						float diffOld = lastDownX / screenWidth;
						if (diff <= 0.33 && diffOld <= 0.33 && !menuShown) { // left
							prevPage();
						} else if (diff >= 0.66 && diffOld >= 0.66
								&& !menuShown) {// right
							nextPage();
						} else if (diff < 0.66 && diff > 0.33 && diffOld < 0.66
								&& diffOld > 0.33) { // middle
							showOverlay(!menuShown);
						}
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
			int lastIndex = 7;

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

			LayoutInflater layoutInflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			int itemSize = (int) getResources().getDimension(
					R.dimen.book_nav_list_height);

			View view = null;

			final ImageView handle = new ImageView(this);
			handle.setBackgroundResource(R.drawable.action_bar_top_background);
			handle.setImageResource(R.drawable.handle_bar_fg);
			handle.setScaleType(ScaleType.CENTER);

			final int loopSize = 3; // XXX levels down

			for (int i = loopSize - 1; i > -1; i--) {

				view = layoutInflater.inflate(R.layout.view_navigation_row,
						null);

				GalleryFix gallery = (GalleryFix) view
						.findViewById(R.id.gal_navigation);
				TextView txtTitle = (TextView) view
						.findViewById(R.id.txt_book_nav_title);
				TextView txtNumbering = (TextView) view
						.findViewById(R.id.txt_book_nav_no);
				BookNavAdapter adapter = new BookNavAdapter(this, itemSize,
						itemSize);

				List<BookNavItem> chapters = adapter.getItems();
				for (String title : stream.getTocNames()) {
					chapters.add(new BookNavItem(title, null));
				}

				gallery.setAdapter(adapter);
				gallery.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						layoutOverlay.onTouchEvent(event);
						return false;
					}
				});

				BookNavigationRow navRow = new BookNavigationRow(view, gallery,
						txtTitle, txtNumbering, adapter);

				navigationList.add(navRow);
				RelativeLayout.LayoutParams rowParams = new LayoutParams(
						android.view.ViewGroup.LayoutParams.FILL_PARENT,
						navRowHeight);
				rowParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

				layoutOverlay.addView(view, rowParams);
			}

			navigationList.get(loopSize - 1).getView()
					.setVisibility(View.VISIBLE);// make
													// first
													// row
													// visible

			handleParams = new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					(int) getResources().getDimension(
							R.dimen.book_nav_handle_height));
			handleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			handleHeight = (int) getResources().getDimension(
					R.dimen.book_nav_handle_height);
			handleParams.bottomMargin = navRowHeight;
			layoutOverlay.addView(handle, handleParams);

			handle.setOnTouchListener(new OnTouchListener() {
				int offset;
				int oldInstanceNo = -1;
				BookNavigationRow currentRow;
				View currentView;
				RelativeLayout.LayoutParams viewParams;
				boolean isInBottom = true;

				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						offset = handleHeight - (int) event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						int value = (int) ((screenHeight - event.getRawY()) - offset);

						if (value <= navRowHeight) { // dont drag less then 1st
														// row
							value = navRowHeight;
							isInBottom = true;
							if (currentView != null) { // make 1st invisible
								currentView.setVisibility(View.INVISIBLE);
							}
						} else if (value >= navRowHeight * loopSize) { // and no
																		// longer
																		// then
																		// 3
							value = navRowHeight * loopSize;
						} else {
							isInBottom = false;
						}

						// if 1st is invisible
						if (!isInBottom
								&& currentView != null
								&& currentView.getVisibility() == View.INVISIBLE) {
							currentView.setVisibility(View.VISIBLE);
						}

						handleParams.bottomMargin = value;
						handle.setLayoutParams(handleParams);
						int instanceNo = loopSize
								- (int) Math.floor(value / navRowHeight) - 1;

						if (instanceNo < 0) {
							instanceNo = 0;
						}

						if (instanceNo != oldInstanceNo) {

							// snap last view
							if (currentView != null) {
								viewParams.bottomMargin = oldInstanceNo
										* navRowHeight;
								currentView.setLayoutParams(viewParams);
								if (instanceNo > oldInstanceNo) {
									currentView.setVisibility(View.INVISIBLE);
								}
							}
							BookNavigationRow r = navigationList
									.get(instanceNo);
							currentRow = r == null ? currentRow : r;
							currentView = currentRow.getView();
							currentView.setVisibility(View.VISIBLE);
							viewParams = (LayoutParams) currentView
									.getLayoutParams();
						}

						viewParams.bottomMargin = value - navRowHeight;
						currentView.setLayoutParams(viewParams);

						oldInstanceNo = instanceNo;

						break;
					case MotionEvent.ACTION_UP:
						int upValue = (int) ((screenHeight - event.getRawY()) - offset);
						int row = (int) Math.floor(upValue / navRowHeight) - 1;
						if (upValue <= navRowHeight * loopSize
								&& upValue >= navRowHeight) { // if
																// we
																// are
																// not
																// in
																// the
																// end
							if ((upValue - navRowHeight) >= (navRowHeight * 0.5)
									+ (navRowHeight * row)) { // snap to top
								snapTo(navRowHeight * (row + 2), navRowHeight
										* (row + 1));
							} else { // snap to bottom
								snapTo(navRowHeight * (row + 1), navRowHeight
										* row);
								currentView.setVisibility(View.INVISIBLE);
							}
						}

						break;
					}
					return true;
				}

				private void snapTo(int handleTarget, final int viewTarget) {
					AnimationHelper.TargetMargin[] ta = { AnimationHelper.TargetMargin.BOTTOM };
					currentView.startAnimation(AnimationHelper.animateMargin(
							currentView, viewParams.bottomMargin, viewTarget,
							250, ta));
					handle.startAnimation(AnimationHelper.animateMargin(handle,
							handleParams.bottomMargin, handleTarget, 250, ta));
				}

			});

			display(lastIndex);
			break;
		case GO_TO_TOC_INDEX:
			int id = (Integer) getIntent().getSerializableExtra(
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
				while (touchingPage) {
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
			bookFlipper.nextScreen();
		} else {
			// /TODO nextChapter
		}
	}

	private void selectText() {
		firstSelectionIndex = 0;
		for (WordPosition cp : pagerAdapter.getItem(0).getPositionList()) {
			firstSelectionIndex++;
			if (cp.area.contains((int) lastDownX, (int) lastDownY)) {
				Log.d(tag, "Touched " + cp.word);
				selectionCanvas.drawRect(cp.area, selectionPaint);
				imgBackground.setImageBitmap(selectionBitmap);
				break;
			}
		}
		selectionMode = true;
	}

	private void prevPage() {
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
			Toast.makeText(this, "Tapped overflow", Toast.LENGTH_SHORT).show();

			// closeOptionsMenu();
			// openOptionsMenu();
			// getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	// disable volume buttons
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!menuShown && (keyCode == 25 || keyCode == 24)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == 4 && menuShown) {
			showOverlay(false);
			return true;
		} else if (keyCode == 4 && selectionMode) {
			selectionMode = false;
			selectionCanvas.drawColor(Color.WHITE);
			imgBackground.setImageBitmap(selectionBitmap);
			// TODO clear selection
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