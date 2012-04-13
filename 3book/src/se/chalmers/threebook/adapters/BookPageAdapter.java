package se.chalmers.threebook.adapters;

import java.io.File;
import java.util.Map;

import se.chalmers.threebook.ImageViewActivity;
import se.chalmers.threebook.R;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.ImageElement;
import se.chalmers.threebook.html.RenderElement;
import se.chalmers.threebook.html.RenderedPage;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class BookPageAdapter extends BaseAdapter {

	private String tag = "BookPageAdapter";

	public static final int START_POSITION = (int) (Integer.MAX_VALUE * 0.5);

	private int lastPosition = 0;
	private LayoutInflater mInflater;
	private HtmlRenderer render;
	private RenderedPage[] pageCache;
	private int objectsBuffered;
	private int bookObjectHeight;
	private int bookObjectSideMargin;
	private Context context;
	
	private String imageString; //TODO add more support for strings here

	public BookPageAdapter(Context context, HtmlRenderer render, int sideBuffer) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.render = render;
		this.context = context;
		this.imageString = context.getResources().getString(R.string.image);
		this.bookObjectHeight = render.getBookObjectHeight();
		this.bookObjectSideMargin = render.getWidthMargin();
		pageCache = new RenderedPage[(sideBuffer * 2) + 1];
	}

	public HtmlRenderer getRenderer() {
		return render;
	}

	public int getCount() {
		return Integer.MAX_VALUE;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		int offset = position - BookPageAdapter.START_POSITION;
		int direction = position - lastPosition; // on first run, this will be
													// positive as lastPos=0

		RenderedPage curPage = null;
		if (objectsBuffered < pageCache.length) {
			objectsBuffered++;
			int insertAtIndex = ((pageCache.length / 2)) + offset;
			pageCache[insertAtIndex] = offset < 0 ? null : render
					.getRenderedPage(offset);
			curPage = pageCache[insertAtIndex];

		} else {
			if (direction > 0) {
				for (int i = 0; i < pageCache.length - 1; i++) {
					pageCache[i] = pageCache[i + 1];
				}
				pageCache[pageCache.length - 1] = render
						.getRenderedPage(offset);
				curPage = pageCache[pageCache.length - 1];
			} else {
				for (int i = pageCache.length - 1; i > 0; i--) {
					pageCache[i] = pageCache[i - 1];
				}

				pageCache[0] = offset < 0 ? null : render
						.getRenderedPage(offset); // TODO previous chapter if
													// offset = 0;
				curPage = pageCache[0];
			}
		}

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.view_book_page, null);
			holder = new ViewHolder(
					(ImageView) convertView
							.findViewById(R.id.img_book_page_image));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (curPage != null) {
			holder.img.setImageBitmap(curPage.getBitmap());
			
			Map<Integer, RenderElement> specialObjectsMap = curPage.getSpecialObjectsMap();
			
			for(Integer i : specialObjectsMap.keySet()){
				
				View bookObject = mInflater.inflate(R.layout.view_book_object, null);	
				RelativeLayout.LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, bookObjectHeight);
				params.topMargin = i;
				params.leftMargin = bookObjectSideMargin;
				params.rightMargin = bookObjectSideMargin;
				ImageView img = (ImageView) bookObject.findViewById(R.id.img_book_object);		
				RenderElement element = specialObjectsMap.get(i);
				Button btn = (Button) bookObject.findViewById(R.id.btn_view_book_object);
				if(element instanceof ImageElement){
					String imgFilePath = new File(context.getCacheDir(), MyBook.get().book().getTitle()+"/"+((ImageElement)element).getUrl()).getAbsolutePath();	
					img.setImageBitmap(BitmapFactory.decodeFile(imgFilePath));
					btn.setText(btn.getText()+ " "+imageString);
					btn.setOnClickListener(new ImageObjectClickListener(imgFilePath));		
				}	
				bookObject.setLayoutParams(params);
				((ViewGroup)convertView).addView(bookObject, params);
			}
			
		}
		lastPosition = position;

		return convertView;
	}
	
	private class ImageObjectClickListener implements OnClickListener{	
		private String imagePath;
		
		public ImageObjectClickListener(String imagePath){
			this.imagePath = imagePath;
		}

		public void onClick(View v){
			Intent intent = new Intent();
	        intent.setClass(context, ImageViewActivity.class);
	        intent.putExtra("imagePath", imagePath);
	        context.startActivity(intent);
		}	
	}
	
	public void clear(){
		lastPosition = 0;
		objectsBuffered = 0;
	}

	private static class ViewHolder {
		ImageView img;

		public ViewHolder(ImageView img) {
			this.img = img;
		}
	}

	public RenderedPage getItem(int direction) {

		return pageCache[pageCache.length / 2];
	}
}
