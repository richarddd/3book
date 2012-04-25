package se.chalmers.threebook.adapters;

import java.io.IOException;
import java.util.Map;

import se.chalmers.threebook.ImageViewActivity;
import se.chalmers.threebook.R;
import se.chalmers.threebook.content.BookNavigator;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.ImageElement;
import se.chalmers.threebook.html.RenderElement;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.util.Helper;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class BookPageAdapter extends BaseAdapter{

	private String tag = "BookPageAdapter";

	public static final int START_POSITION = (int) (Integer.MAX_VALUE * 0.5);

	private int lastPosition = 0;
	private LayoutInflater mInflater;
	private RenderedPage[] pageCache;

	private int lateOffset; // used for non-zero jump buffering
							// re-initialization

	private int objectsBuffered;
	private int bookObjectHeight;
	private int bookObjectSideMargin;
	private Context context;
	private Integer basicViewCount;

	private String imageString; // TODO add more support for strings here
	private BookNavigator navigator;

	/**
	 * 
	 * @param context
	 *            the context
	 * @param render
	 *            the renderer providing images
	 * @param sideBuffer
	 *            amount of pages to cache back and ahead
	 * 
	 *            Total number of cached files will be (sideBuffer*2+1)
	 */
	public BookPageAdapter(Context context, BookNavigator navigator, int sideBuffer){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.navigator = navigator;
		this.context = context;
		this.imageString = context.getResources().getString(R.string.image);
		this.bookObjectHeight = (int) Helper.dpToPx(context, HtmlRenderer.BOOK_OBJECT_HEIGHT_DP);
		this.bookObjectSideMargin = 10; // XXX TODO width margin of objects is
										// hard coded, thats bad!
		pageCache = new RenderedPage[(sideBuffer * 2) + 1];
	}

	public int getCount(){
		return Integer.MAX_VALUE;
	}

	public long getItemId(int position){
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent){

		int offset = position - BookPageAdapter.START_POSITION;
		int direction = position - lastPosition; // on first run, this will be
		
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.view_book_page, null);
			holder = new ViewHolder(
					(ImageView) convertView.findViewById(R.id.img_book_page_image));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

		try{// positive as lastPos=0

			RenderedPage curPage = null;
			if(objectsBuffered < pageCache.length){
				if(objectsBuffered == 0){
					lateOffset = -offset;
				}
				objectsBuffered++;

				int insertAtIndex = ((pageCache.length / 2)) + (offset + lateOffset);
				pageCache[insertAtIndex] = offset < 0?null:navigator.bufferNextPage();

				curPage = pageCache[insertAtIndex];

			}else{
				if(direction > 0){
					for(int i = 0; i < pageCache.length - 1; i++){
						pageCache[i] = pageCache[i + 1];
					}
					pageCache[pageCache.length - 1] = navigator.nextPage();
					curPage = pageCache[pageCache.length - 1];
				}else{
					for(int i = pageCache.length - 1; i > 0; i--){
						pageCache[i] = pageCache[i - 1];
					}

					pageCache[0] = offset < 0?null:navigator.prevPage();
					curPage = pageCache[0];
				}
			}

			

			if(basicViewCount == null){
				basicViewCount = ((ViewGroup) convertView).getChildCount();
			}

			if(curPage != null){
				holder.img.setImageBitmap(curPage.getBitmap());

				Map<Integer, RenderElement> specialObjectsMap = curPage.getSpecialObjectsMap();

				if(specialObjectsMap.isEmpty()
						&& ((ViewGroup) convertView).getChildCount() != basicViewCount){
					((ViewGroup) convertView).removeViews(basicViewCount,
							((ViewGroup) convertView).getChildCount() - 1);
				}else{
					for(Integer i : specialObjectsMap.keySet()){

						View bookObject = mInflater.inflate(R.layout.view_book_object, null);
						RelativeLayout.LayoutParams params = new LayoutParams(
								android.view.ViewGroup.LayoutParams.FILL_PARENT, bookObjectHeight);
						params.topMargin = i;
						params.leftMargin = bookObjectSideMargin;
						params.rightMargin = bookObjectSideMargin;
						ImageView img = (ImageView) bookObject.findViewById(R.id.img_book_object);
						RenderElement element = specialObjectsMap.get(i);
						Button btn = (Button) bookObject.findViewById(R.id.btn_view_book_object);
						if(element instanceof ImageElement){
							img.setImageBitmap(((ImageElement) element).getBitmap());
							btn.setText(btn.getText() + " " + imageString);
							btn.setOnClickListener(new ImageObjectClickListener(
									((ImageElement) element).getAbsoluteUrl()));
						}
						bookObject.setLayoutParams(params);
						((ViewGroup) convertView).addView(bookObject, params);
					}
				}

			}
		}catch(IOException  e){
			
			Log.e(tag, "IOE: " + e.getMessage());
			e.printStackTrace(); // TODO: remove once debugged
			TextView errorTextView = new TextView(context);
			errorTextView.setText(context.getString(R.string.err_render_page));
			errorTextView.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams params = new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
			((ViewGroup) convertView).addView(errorTextView, params);
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

	private static class ViewHolder{
		ImageView img;

		public ViewHolder(ImageView img){
			this.img = img;
		}
	}

	public RenderedPage getItem(int direction){

		return pageCache[pageCache.length / 2];
	}
}
