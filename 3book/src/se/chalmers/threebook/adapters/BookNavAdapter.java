package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.chalmers.threebook.R;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.util.BookNavItem;
import se.chalmers.threebook.util.Helper;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class BookNavAdapter extends BaseAdapter {

	private Context context;
	private List<BookNavItem> items = new ArrayList<BookNavItem>();
	private final int itemWidth;
	private final int itemHeight;
	
	public BookNavAdapter(Context context, float itemWidth, float itemHeight) {
		this.context = context;
		this.itemWidth = (int) itemWidth;
		this.itemHeight = (int) itemHeight;
	}

	
	public List<BookNavItem> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public BookNavItem getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new TextView(context);	
			convertView.setLayoutParams(new Gallery.LayoutParams(itemWidth,itemHeight));
			((TextView)convertView).setGravity(android.view.Gravity.CENTER);
			convertView.setBackgroundResource(R.color.default_background);
		}
		
		((TextView)convertView).setText(items.get(position).getTitle());
		return convertView;
	}

}
