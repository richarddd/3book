package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.util.BookNavItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookNavAdapter extends BaseAdapter implements OnScrollListener {

	private LayoutInflater layoutInflater;
	private List<BookNavItem> items = new ArrayList<BookNavItem>();
	private HorizontalListView listView;
	private Context context;

	public BookNavAdapter(Context context, HorizontalListView listView) {
		this.context = context;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.listView = listView;
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
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.listview_nav_item,
					null);
			holder = new ViewHolder(
					(ImageView) convertView
							.findViewById(R.id.img_book_nav_item),
					(TextView) convertView.findViewById(R.id.txt_book_nav_item));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (items.get(position).getBitmap() != null) {
			holder.imgView.setImageBitmap(items.get(position).getBitmap());
		}
		holder.textView.setText(String.valueOf(position));
		return convertView;
	}

	static class ViewHolder {
		ImageView imgView;
		TextView textView;

		public ViewHolder(ImageView imgView, TextView textView) {
			super();
			this.imgView = imgView;
			this.textView = textView;
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		for (int i = 0; i < visibleItemCount; i++) {
			int middle;
			if (visibleItemCount % 2 == 0) {
				middle = visibleItemCount / 2;
			} else {
				middle = (int) Math.ceil(visibleItemCount / 2);
			}
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

}
