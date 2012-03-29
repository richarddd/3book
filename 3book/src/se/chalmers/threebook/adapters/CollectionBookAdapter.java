package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.model.Book;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class CollectionBookAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<Book> items = new ArrayList<Book>();

	public CollectionBookAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<Book> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public Book getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.listview_book, null);
			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.txt_book_title),
					(TextView) convertView.findViewById(R.id.txt_book_author),
					(RatingBar) convertView.findViewById(R.id.bar_book_rating),
					(ImageView) convertView.findViewById(R.id.img_book_cover));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Book b = items.get(position);

		holder.title.setText(b.getTitle());
//		holder.author.setText(b.getAuthor());
//		holder.ratingBar.setRating(b.getRating());
		// holder.imgView.setImageBitmap(bm);
		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView author;
		RatingBar ratingBar;
		ImageView imgView;

		public ViewHolder(TextView title, TextView author, RatingBar ratingBar,
				ImageView imgView) {
			this.title = title;
			this.author = author;
			this.ratingBar = ratingBar;
			this.imgView = imgView;
		}
	}

}
