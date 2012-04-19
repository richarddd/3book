package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.model.Book;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentBookAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<Book> books = new ArrayList<Book>();
	private Context context;

	public RecentBookAdapter(Context context, List<Book> books) {
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

		Book b = books.get(position);
		holder.text.setText(b.getTitle());
		Bitmap bm = b.getCover();
		
		if(bm == null) { 
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.recent_book_cover);
		}
		
		holder.imgView.setImageBitmap(bm);
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
