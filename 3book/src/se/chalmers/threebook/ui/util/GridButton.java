package se.chalmers.threebook.ui.util;


public class GridButton {

	private String text;
	private int imageResource;
	
	public GridButton(String text, int imageResource) {
		super();
		this.text = text;
		this.imageResource = imageResource;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getImageResource() {
		return imageResource;
	}

	public void setImageResource(int imageResource) {
		this.imageResource = imageResource;
	}

}
