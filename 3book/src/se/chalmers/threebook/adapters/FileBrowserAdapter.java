package se.chalmers.threebook.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.FileBrowserActivity.FileSelect;
import se.chalmers.threebook.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBrowserAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<FileSelect> items = new ArrayList<FileSelect>();

	private static final int FILETYPE_FOLDER = R.drawable.ic_folder;
	private static final int FILETYPE_FILE = R.drawable.ic_file;
	private static final int FILETYPE_EBOOK = R.drawable.ic_ebook;
	private static final String[] SUPPORTED_FILES = {"epub"}; //TODO implement more formats

	public FileBrowserAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<FileSelect> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public FileSelect getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.listview_file_browser, null);
			holder = new ViewHolder(
					(TextView) convertView
							.findViewById(R.id.txt_file_item_title),
					(ImageView) convertView.findViewById(R.id.img_file_icon), (CheckBox)convertView.findViewById(R.id.chk_file_browser));
			
			holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					FileSelect fs = (FileSelect) holder.checkbox
							.getTag();
					fs.setSelected(buttonView.isChecked());
				}
			});
			convertView.setTag(holder);
			holder.checkbox.setTag(items.get(position));
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		FileSelect fs = items.get(position);

		String name = fs.getFile().getName();
		int imageId;
		holder.text.setText(name);
		if (fs.getFile().isDirectory()) {
			imageId = FILETYPE_FOLDER;
		} else if (fileSupported(name)) {
			holder.checkbox.setVisibility(CheckBox.VISIBLE);
			imageId = FILETYPE_EBOOK;
		} else {
			imageId = FILETYPE_FILE;
			fs.setEnabled(false);
		}

		holder.checkbox.setChecked(fs.isSelected());
		holder.imgView.setImageResource(imageId);
		return convertView;
	}
	
	private static boolean fileSupported(String name){
		for(String val : SUPPORTED_FILES){
			if(name.endsWith(val)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return (items.get(position).getEnabled());
	}

	private static class ViewHolder {
		TextView text;
		ImageView imgView;
		CheckBox checkbox;

		public ViewHolder(TextView text, ImageView imgView, CheckBox checkbox) {
			super();
			this.text = text;
			this.imgView = imgView;
			this.checkbox = checkbox;
		}
	}

}
