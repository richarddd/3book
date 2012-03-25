package se.chalmers.threebook;

import se.chalmers.threebook.ui.BookView;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

public class CanvasActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout linear = new LinearLayout(this);
		linear.setOrientation(LinearLayout.VERTICAL);
		BookView bookView = new BookView(this);
		bookView.setBackgroundColor(Color.WHITE);
		linear.addView(bookView, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));
		setContentView(linear);
	}

}
