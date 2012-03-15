package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.ui.HorizontalListView;
import se.chalmers.threebook.ui.util.BookNavItem;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
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
	
	
/* XXX axel test code - probably not right! */
	
	public void initialize(TextView chapNameView, TextView chapNoView, int startPos){
		//
	}
	
	private TextView chapterNameTextView;
	private TextView chapterNoTextView;
	public void setChapterNameTextView(TextView view){
		chapterNameTextView = view;
		//
	}
	public void setChapterNoTextView(TextView view){
		chapterNoTextView = view;
	}
	
	// XXX rather ugly thingie.
	public void setCurrentChapterNumber(int chapterIndex, int chapterCount){
		chapterNoTextView.setText((chapterIndex+1)+"/"+chapterCount);
	}
	
	public void setCurrentChapterName(String text){
		chapterNameTextView.setText(text);
	}
	
	/**
	 * Sets the onItemClickListener for the contained listView
	 */
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		listView.setOnItemClickListener(listener);
	}
	
/* end axel test code */
	
	
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
		/* XXX Axel hacking! */
		//holder.textView.setText(String.valueOf(position));
		holder.textView.setText(items.get(position).getTitle());
		Log.d("3", "Trying to figure out view size for smallys. W/H: ");
		Log.d("3", ((TextView) convertView.findViewById(R.id.txt_book_nav_item)).getWidth()+"/"+((TextView) convertView.findViewById(R.id.txt_book_nav_item)).getHeight());
		
		/* XXX End axel hacking */ 
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
		/*for (int i = 0; i < visibleItemCount; i++) {
			int middle;
			if (visibleItemCount % 2 == 0) {
				middle = visibleItemCount / 2;
			} else {
				middle = (int) Math.ceil(visibleItemCount / 2);
			}
		} */
		/* XXX axel hacking */
		//Log.d("3", "1stVis/visibleCount/totalCount: " + firstVisibleItem + "/" + visibleItemCount + "/" + totalItemCount);
		int position = 0; 
		position = firstVisibleItem + (int)(visibleItemCount % 2 == 0 ? visibleItemCount/2 : Math.ceil(visibleItemCount/2));
		if (chapterNameTextView != null){
			setCurrentChapterName(items.get(position).getTitle());
		}
		if (chapterNoTextView != null){
			// position+1 for CS-zeroindex->human-oneindex
			setCurrentChapterNumber(position, totalItemCount);
		}
		
		/* end axel hacking */
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

}
