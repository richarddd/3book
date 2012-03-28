package se.chalmers.threebook.adapters;

import se.chalmers.threebook.R;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.util.Helper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BookPageAdapter extends BaseAdapter {
	
	private String tag = "BookPageAdapter";
	
	public static final int START_POSITION = (int) (Integer.MAX_VALUE*0.5);
	
	private int lastPosition = START_POSITION;
	private LayoutInflater mInflater;
	private RenderedPage next;
	private RenderedPage previous;
	private HtmlRenderer render;
	
	private RenderedPage[] pageCache = new RenderedPage[3];
	private boolean cacheEmpty = true;

	public BookPageAdapter(Context context, HtmlRenderer render) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.render = render;
	}
	
	public HtmlRenderer getRenderer(){
		return render;
	}

	public int getCount() {
		return Integer.MAX_VALUE;
	}

	public int getCurrentItem() {
		return lastPosition;
	}

	public void setCurrentItem(int currentItem) {
		this.lastPosition = currentItem;
	}

	public RenderedPage getNext() {
		return next;
	}

	public void setNext(RenderedPage next) {
		this.next = next;
	}

	public RenderedPage getPrevious() {
		return previous;
	}

	public void setPrevious(RenderedPage previous) {
		this.previous = previous;
	}

	public RenderedPage getItem(int position) {
		RenderedPage item;
		if (position > lastPosition) {
			item = next;
		} else {
			item = previous;
		}
		lastPosition = position;
		return item;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		int offset = position-BookPageAdapter.START_POSITION;
		
		//RenderedPage page = 
		
		//Log.d(tag, "Inflating position: "+position);
		//Log.d(tag, "Inflating offset: "+());
		
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
		Bitmap bitmap;
		
		bitmap = getItem(position).getBitmap();
		
		if(bitmap != null){
			holder.img.setImageBitmap(bitmap);
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView img;

		public ViewHolder(ImageView img) {
			this.img = img;
		}
	}

}
