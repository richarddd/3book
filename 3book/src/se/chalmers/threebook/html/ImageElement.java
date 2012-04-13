package se.chalmers.threebook.html;

public class ImageElement extends RenderElement{
	
	private String url;
	private int height;
	private int width;
	
	public ImageElement(String url, int width, int height){
		this.url = url;
		this.width = width;
		this.height = height;
	}

	public String getUrl(){
		return url;
	}
}
