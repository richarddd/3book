package se.chalmers.threebook.ui.util;

public class RecentBook {

	private String title;
	private int imageResource;
	public RecentBook(String title, int imageResource) {
		super();
		this.title = title;
		this.imageResource = imageResource;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getImageResource() {
		return imageResource;
	}
	public void setImageResource(int imageResource) {
		this.imageResource = imageResource;
	}
}
