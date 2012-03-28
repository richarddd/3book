package se.chalmers.threebook.adapters;

import se.chalmers.threebook.R;
import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BookPageAdapter extends BaseAdapter {

	private String tag = "BookPageAdapter";

	public static final int START_POSITION = (int) (Integer.MAX_VALUE * 0.5);

	private int lastPosition = 0;
	private LayoutInflater mInflater;
	private HtmlRenderer render;
	private RenderedPage[] pageCache;

	private int objectsBuffered; //

	public BookPageAdapter(Context context, HtmlRenderer render, int sideBuffer) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.render = render;
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
			int insertAtIndex = ((int) (pageCache.length / 2)) + offset;
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
			convertView = mInflater.inflate(R.layout.fragment_book_image, null);
			holder = new ViewHolder(
					(ImageView) convertView
							.findViewById(R.id.img_book_page_image));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (curPage != null) {
			holder.img.setImageBitmap(curPage.getBitmap());
		}
		lastPosition = position;

		return convertView;
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
