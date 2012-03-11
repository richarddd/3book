package se.chalmers.threebook.adapters;

import se.chalmers.threebook.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BookPageAdapter extends BaseAdapter {

	private int lastPosition = 0;
	private LayoutInflater mInflater;
	// private LinkedList<Bitmap> items = new LinkedList<Bitmap>();
	// private ListIterator<Bitmap> iterator = items.listIterator();
	private Bitmap current;
	private Bitmap next;
	private Bitmap previous;

	public BookPageAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return current == null || next == null ? 0 : Integer.MAX_VALUE;
	}

	public int getCurrentItem() {
		return lastPosition;
	}

	public void setCurrentItem(int currentItem) {
		this.lastPosition = currentItem;
	}

	public Bitmap getCurrent() {
		return current;
	}

	public void setCurrent(Bitmap current) {
		this.current = current;
	}

	public Bitmap getNext() {
		return next;
	}

	public void setNext(Bitmap next) {
		this.next = next;
	}

	public Bitmap getPrevious() {
		return previous;
	}

	public void setPrevious(Bitmap previous) {
		this.previous = previous;
	}

	public Bitmap getItem(int position) {
		Bitmap item;
		if (position == 0) {
			item = current;
		} else {
			if (position > lastPosition) {
				Log.d("BookpagerAdapter", "Next adapter 'inflate' at: "
						+ String.valueOf(position));
				item = next;
			} else {
				Log.d("BookpagerAdapter", "Prev adapter 'inflate' at: "
						+ String.valueOf(position));
				item = previous;
			}
		}
		lastPosition = position;
		return item;
	}

	/*
	 * Log.i(this.getClass().toString(),
	 * "Adapter pos: "+String.valueOf(position)); Bitmap item; if(position ==
	 * 0){ item = items.getFirst(); }else{ if (position > currentItem) { //item
	 * = items.get(items.indexOf(items.get(position))+1); items.get
	 * 
	 * } else { item = items.get(items.indexOf(items.get(position))-1); } }
	 * currentItem = position; return item;
	 */

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.fragment_book_image, null);
			holder = new ViewHolder(
					(ImageView) convertView
							.findViewById(R.id.img_book_page_image));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.img.setImageBitmap(getItem(position));
		return convertView;

	}

	private static class ViewHolder {
		ImageView img;

		public ViewHolder(ImageView img) {
			this.img = img;
		}
	}

}
