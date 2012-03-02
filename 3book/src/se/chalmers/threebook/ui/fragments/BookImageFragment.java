package se.chalmers.threebook.ui.fragments;

import se.chalmers.threebook.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class BookImageFragment extends Fragment {
	//TODO possible optimization here
	
	private ImageView image;
	private int scrollValue;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			
		}
		container = (LinearLayout)inflater.inflate(R.layout.fragment_book_image, container, false);
		image = (ImageView)container.findViewById(R.id.img_book_page_image);
		return container;
	}

	public ImageView getImage() {
		return image;
	}

	public int getScrollValue() {
		return scrollValue;
	}

	public void setScrollValue(int scrollValue) {
		this.scrollValue = scrollValue;
	}
}
