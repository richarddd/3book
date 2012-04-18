package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.model.Book;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class CollectionBookAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<Book> books = new ArrayList<Book>();
	private Context context;

	public CollectionBookAdapter(Context context, List<Book> books) {
		this.context = context;
		this.books = books;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<Book> getItems() {
		return books;
	}

	public int getCount() {
		return books.size();
	}

	public Book getItem(int position) {
		return books.get(position);
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

		Book b = books.get(position);

		holder.title.setText(b.getTitle());
//		holder.author.setText(b.getAuthor());
//		holder.ratingBar.setRating(b.getRating());
		Bitmap bm = b.getCover();
		
		if(bm == null) {
			bm = BitmapFactory.decodeResource(context.getResources(), R.id.img_book_cover);
		}
		
		holder.imgView.setImageBitmap(b.getCover());
		
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
