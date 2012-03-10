package se.chalmers.threebook.ui.util;

import android.graphics.Bitmap;

public class BookNavItem {

	private Bitmap bitmap;
	private String title;

	public BookNavItem(String title, Bitmap bitmap) {
		this.title = title;
		this.bitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		
	}

}
