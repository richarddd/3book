package se.chalmers.threebook;

import java.util.LinkedList;
import java.util.List;

import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
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
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class ReadActivity extends ActionBarActivity {

	private WebView webView;
	private FrameLayout layout;
	private Boolean selectionMode = false;
	private ImageView imgBook;
	private float screenWidth;
	private float screenHeight;
	private LinkedList<Bitmap> forwardCache = new LinkedList<Bitmap>();
	private LinkedList<Bitmap> backwardCache = new LinkedList<Bitmap>();
	private int maxCacheCount = 2;
	private ProgressDialog dialog;

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

		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		setFullScreen(true);

		if (Helper.SupportsNewApi()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// ActionBarHelper helper = getActionBarHelper();

		webView = (WebView) findViewById(R.id.web_book);
		layout = (FrameLayout) findViewById(R.id.lay_read);
		imgBook = (ImageView) findViewById(R.id.img_book);

		imgBook.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					float diff = event.getX() / screenWidth;
					Log.d("Read Activity", String.valueOf(diff));
					if (diff <= 0.33) { // left
						prevPage();
					} else if (diff >= 0.66) {// right
						nextPage();
					} else { // middle
						setFullScreen(false);
					}
					break;
				default:
					break;
				}
				return false;
			}

		});

		webView.loadUrl("file:///android_asset/lorem.html");
		
		dialog = ProgressDialog.show(this, "", 
				this.getString(R.string.loading_please_wait), true);

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);

		webView.setPictureListener(new PictureListener() {
			public void onNewPicture(WebView view, Picture picture) {
				forwardCache.add(getBitmapFromView(view));
				imgBook.setImageBitmap(forwardCache.get(0));
				generateWebViewCache(true);
				generateWebViewCache(false);
				dialog.dismiss();
				view.setPictureListener(null);
			}
		});

		webView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return selectionMode ? false
						: (event.getAction() == MotionEvent.ACTION_MOVE);
			}
		});

	}

	private void generateWebViewCache(boolean forward) {
		if (forward) {
			while (forwardCache.size() < maxCacheCount) {
				webView.pageDown(false);
				webView.pageDown(false);
				forwardCache.add(getBitmapFromView(webView));
			}
		} else {
			while (backwardCache.size() < maxCacheCount) {
				webView.pageUp(false);
				webView.pageUp(false);
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
		imgBook.setImageBitmap(forwardCache.get(0));
		generateWebViewCache(true);
	}

	private void prevPage() {
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
		imgBook.setImageBitmap(backwardCache.get(0));
		generateWebViewCache(false);
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
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.read, menu);
		return super.onCreateOptionsMenu(menu);
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
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}