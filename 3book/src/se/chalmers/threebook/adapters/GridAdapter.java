package se.chalmers.threebook.adapters;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.R;
import se.chalmers.threebook.ui.GridButton;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private List<GridButton> items = new ArrayList<GridButton>();

	public GridAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<GridButton> getItems() {
		return items;
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.listview_grid_button,
					null);
		}
		
		TextView title = (TextView) convertView .findViewById(R.id.txt_grid_button_title);
		ImageView img =  (ImageView) convertView.findViewById(R.id.img_grid_button_image);
		
		title.setText(items.get(position).getText());
		img.setImageResource(items.get(position).getImageResource());
		
		return convertView;
	}

}
