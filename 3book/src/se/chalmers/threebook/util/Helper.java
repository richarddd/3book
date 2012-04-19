package se.chalmers.threebook.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import se.chalmers.threebook.ReadActivity;
import se.chalmers.threebook.contentprovider.ThreeBookContentProvider;
import se.chalmers.threebook.db.BookDataHelper;
import se.chalmers.threebook.db.BookTable;
import se.chalmers.threebook.model.Book;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class Helper {

	public static boolean SupportsNewApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	public static float dpToPx(Context c, float dp) {
		float px = dp
				* (c.getResources().getDisplayMetrics().densityDpi / 160f);
		return px;
	}

	public static float pxToDp(Context c, float px) {
		float dp = px
				/ (c.getResources().getDisplayMetrics().densityDpi / 160f);
		return dp;
	}

	public static Point getDisplaySize(Context context) {
		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point p = new Point();

		if (Helper.SupportsNewApi()) {
			display.getSize(p);
		} else {
			p.x = display.getWidth();
			p.y = display.getHeight();
		}
		return p;
	}
	
	

	public static String streamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Returns a bitmap from the views drawing cache. Warining: disables drawing
	 * cache after usage.
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.setDrawingCacheEnabled(true);
		view.layout(0, 0, view.getWidth(), view.getBottom());
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		return bitmap;
	}

	public static void openBook(Context context, File file) {
		if (file.getName().endsWith(".epub")) {
		    openBook(context, new Book().setSource(file.getPath()));
		}
	}
	
	public static void openBook(Context context, Book book) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		Date date = new Date();
		ContentValues values = new ContentValues(); 
		values.put(BookTable.COLUMN_LASTREAD, dateFormat.format(date));
		
		int nrUpdated = context.getContentResolver().update(ThreeBookContentProvider.BOOK_URI, values, BookTable.COLUMN_ID + "=?", new String[]{String.valueOf(book.getId())});
		
		Intent displayBook = new Intent(context, ReadActivity.class);
		displayBook.putExtra(ReadActivity.IntentKey.FILE_PATH.toString(),
		    book.getSource());
		
		if(nrUpdated == 0) {
			displayBook.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(),
					ReadActivity.IntentType.READ_BOOK_NOT_IN_LIBRARY);
		} else {
			displayBook.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(),
					ReadActivity.IntentType.READ_BOOK_FROM_LIBRARY);
		}
		
		context.startActivity(displayBook);
	}
	
}
