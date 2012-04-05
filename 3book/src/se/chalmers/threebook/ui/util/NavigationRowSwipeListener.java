package se.chalmers.threebook.ui.util;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class NavigationRowSwipeListener implements OnTouchListener {

	private int offset;
	private boolean snapToTop = false;
	private int depth;
	private BookNavigationRow row;
	private int rowHeight;
	private final int screenHeight;
	private View previousView;
	private View currentView;
	private String tag = "NavigationRowSwipeListener";
	
	private RelativeLayout.LayoutParams curParams;
	private RelativeLayout.LayoutParams prevParams;
	
	
	
	public NavigationRowSwipeListener(int screenHeight ,int rowHeight){
		this.screenHeight = screenHeight;
		this.rowHeight = rowHeight;
	}
	
	public void setCurrentView(View currentView){
		this.currentView = currentView;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public BookNavigationRow getRow() {
		return row;
	}

	public void setRow(BookNavigationRow row) {
		this.row = row;
		//this.currentView = row.getContainer();
		//this.previousView = row.getPreviousView();
		this.prevParams = (LayoutParams) previousView
				.getLayoutParams();
		this.curParams = (LayoutParams) currentView
				.getLayoutParams();
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			offset = rowHeight-(int) event.getY();
			

			break;

		case MotionEvent.ACTION_MOVE:
			if (previousView != null) {
				int value = (screenHeight-(int) event.getRawY())-offset;
				if(value <= (rowHeight * depth) && depth > 0){  //we should move the current view
					curParams.bottomMargin = value;
					//prevParams.bottomMargin = value;
					currentView.setLayoutParams(curParams);
					if(previousView.getVisibility() == View.VISIBLE){
						previousView.setVisibility(View.INVISIBLE);
					}
					//previousView.setLayoutParams(prevParams);
				}else{ //we should move the underlying view
					if(curParams.bottomMargin != rowHeight*(depth)){ //snap current view to correct height
						curParams.bottomMargin = rowHeight*(depth);
						currentView.setLayoutParams(curParams);
					}
					
					if(previousView.getVisibility() == View.INVISIBLE){
						previousView.setVisibility(View.VISIBLE);
					}
						
					prevParams.bottomMargin = value;
					
					if (value >= (rowHeight * depth)
							+ (rowHeight * 0.5)) {
						snapToTop = true;
					} else {
						snapToTop = false;
					}
					if(value >= (rowHeight * (depth+1))){
						prevParams.bottomMargin = rowHeight * (depth+1);
					}
					previousView.setLayoutParams(prevParams);
				}
				
			}
			break;
		case MotionEvent.ACTION_UP:
			
			if (snapToTop) {
				snapToTop();
			} else {
				snapToBottom();
			}
			break;
		}
		return false;
	}

	private void snapToBottom() {
		prevParams.bottomMargin = rowHeight*(depth);
		row.snapToBottom();
		previousView.setLayoutParams(prevParams);
	}

	private void snapToTop() {
		prevParams.bottomMargin = rowHeight*(depth+1);
		previousView.setLayoutParams(prevParams);
		row.snapToTop();
		depth++;
		//row.setTouchListener(null);
		//setRow(row.getPreviousRow());
		//row.setTouchListener(this);
	}
}
