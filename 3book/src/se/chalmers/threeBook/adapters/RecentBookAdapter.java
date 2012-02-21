package se.chalmers.threeBook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threeBook.R;
import se.chalmers.threeBook.ui.RecentBook;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentBookAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<RecentBook> items = new ArrayList<RecentBook>();

	public RecentBookAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<RecentBook> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public RecentBook getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.listview_recent_book,
					null);
			holder = new ViewHolder(
					(TextView) convertView
							.findViewById(R.id.txt_recent_book_title),
					(ImageView) convertView
							.findViewById(R.id.img_recent_book_cover));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(items.get(position).getTitle());
		holder.imgView.setImageResource(items.get(position).getImageResource());
		return convertView;
	}

	static class ViewHolder {
		TextView text;
		ImageView imgView;

		public ViewHolder(TextView text, ImageView imgView) {
			super();
			this.text = text;
			this.imgView = imgView;
		}
	}

}
