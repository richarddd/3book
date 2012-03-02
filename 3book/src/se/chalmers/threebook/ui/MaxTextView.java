package se.chalmers.threebook.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class MaxTextView extends TextView {
	
	private boolean filled = false;
	private OnDrawListener drawListener;

	public MaxTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MaxTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MaxTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	    
		super.onDraw(canvas);

	    if(getHeight() < computeVerticalScrollRange()){
	    	filled = true;
	    	
	    }
	    drawListener.onDraw(this);
	}
	
	

	public OnDrawListener getOnDrawListener() {
		return drawListener;
	}

	public void addOnDrawListener(OnDrawListener drawListener) {
		this.drawListener = drawListener;
	}

	public boolean isFilled() {
		return filled;
	}
}
