package se.chalmers.threebook.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;

public class AnimationHelper {
	
	public static enum TargetMargin{
    	TOP,
    	LEFT,
    	BOTTOM,
    	RIGHT;
    }
	
	public static TranslateAnimation animateMargin(final View view, final int fromMargin, final int toMargin, int duration, final TargetMargin[] targets)
	{
		//TODO generalize this to support all directions or factor it out
	    TranslateAnimation animation = new TranslateAnimation(0, 0, 0, fromMargin - toMargin);
	    animation.setDuration(duration);
	    animation.setAnimationListener(new Animation.AnimationListener()
	    {
	        public void onAnimationEnd(Animation animation)
	        {
	        	view.clearAnimation();
	        	
	        	LayoutParams params = (LayoutParams) view.getLayoutParams();
	        	
	        	
	        	for(TargetMargin t : targets){
	        		switch(t){
	        		case BOTTOM:
	        			params.bottomMargin = toMargin;
	        			break;
	        		case TOP:
	        			params.topMargin = toMargin;
	        			break;
	        		case LEFT:
	        			params.leftMargin = toMargin;
	        			break;
	        		case RIGHT:
	        			params.rightMargin = toMargin;
	        			break;
	        		}
	        		
	        	}
	            // Set the new bottom margin.
	        	
	        	view.setLayoutParams(params);
	        }

	        public void onAnimationStart(Animation animation) {}

	        public void onAnimationRepeat(Animation animation) {}
	    });
	    return animation;
	}

}
