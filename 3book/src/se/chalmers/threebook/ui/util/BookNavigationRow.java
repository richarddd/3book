package se.chalmers.threebook.ui.util;

import se.chalmers.threebook.R;
import se.chalmers.threebook.adapters.BookNavAdapter;
import se.chalmers.threebook.ui.GalleryFix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Scroller;
import android.widget.TextView;

public class BookNavigationRow {
	
	private String tag = "BookNavigationRow";

	private GalleryFix gallery;
	private TextView txtTitle;
	private TextView txtNumbering;
	private BookNavAdapter adapter;
	private View container;
	private int size = 0;

	public BookNavigationRow(View container, GalleryFix gallery,
			TextView txtTitle, TextView txtNumbering, BookNavAdapter adapter) {
		super();
		this.gallery = gallery;
		this.txtTitle = txtTitle;
		this.txtNumbering = txtNumbering;
		this.adapter = adapter;
		this.container = container;
		this.size = adapter.getCount();
		
		this.gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				BookNavigationRow.this.txtTitle.setText(BookNavigationRow.this.adapter.getItem(position).getTitle());
				BookNavigationRow.this.txtNumbering.setText((position+1)+"/"+size);
				//view.setBackgroundResource(R.color.default_background);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void setAdapter(BookNavAdapter adapter){
		this.adapter = adapter;
		this.size = adapter.getCount();
		this.gallery.setAdapter(adapter);
	}

	public void snapToTop() {
		
		
		Log.d(tag, "Should snapp to top");
	}

	public void snapToBottom() {
		Log.d(tag, "Should snapp to bottom");
	}

	public View getView() {
		return container;
	}
}
