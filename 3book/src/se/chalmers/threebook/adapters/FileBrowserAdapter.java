package se.chalmers.threebook.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBrowserAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<File> items = new ArrayList<File>();

	private static final int FILETYPE_FOLDER = R.drawable.ic_folder;
	private static final int FILETYPE_FILE = R.drawable.ic_file;
	private static final int FILETYPE_EBOOK = R.drawable.ic_ebook;

	public FileBrowserAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<File> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public File getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.listview_file_browser, null);
			holder = new ViewHolder(
					(TextView) convertView
							.findViewById(R.id.txt_file_item_title),
					(ImageView) convertView.findViewById(R.id.img_file_icon));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String name = items.get(position).getName();
		int imageId;
		holder.text.setText(name);
		if (items.get(position).isDirectory()) {
			imageId = FILETYPE_FOLDER;
		} else if (name.endsWith(".zip") || name.endsWith(".epub")
				|| name.endsWith(".mobi")) {
			imageId = FILETYPE_EBOOK;
		} else {
			imageId = FILETYPE_FILE;
		}

		holder.imgView.setImageResource(imageId);
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
