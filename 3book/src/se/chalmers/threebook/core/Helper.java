package se.chalmers.threebook.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

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
	
	public static float dpToPx(Context c, float dp){
	    float px = dp * (c.getResources().getDisplayMetrics().densityDpi/160f);
	    return px;
	}
	
	public static float pxToDp(Context c, float px){
		float dp = px / (c.getResources().getDisplayMetrics().densityDpi/160f);
	    return dp;
	}
	
	/**
	 * Returns a bitmap from the views drawing cache. Warining: disables drawing cache after usage.
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

}
