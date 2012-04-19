package se.chalmers.threebook.ui;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class GalleryFix extends Gallery {

	public GalleryFix(Context context) {
		super(context);
	}

	public GalleryFix(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GalleryFix(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private long mLastScrollEvent;
	 
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
	  long now = SystemClock.uptimeMillis();
	  if (Math.abs(now - mLastScrollEvent) > 200) {
	    super.onLayout(changed, l, t, r, b);
	  }
	}
	 
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	    float distanceY)
	{
	  mLastScrollEvent = SystemClock.uptimeMillis();
	  return super.onScroll(e1, e2, distanceX, distanceY);
	}
	
	
	/*
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		//t.clear();
		//t.set
		child.setBackgroundResource(R.color.nav_item_background);
		return true;
	}*/

}
