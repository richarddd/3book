package se.chalmers.threebook.html;

import android.graphics.Bitmap;

public class ImageElement extends RenderElement{
	
	private String relativeUrl;
	private String absoluteUrl;
	private int height;
	private int width;
	private Bitmap bitmap;
	
	public ImageElement(String relativeUrl, int width, int height){
		this.relativeUrl = relativeUrl;
		this.width = width;
		this.height = height;
	}

	public String getUrl(){
		return relativeUrl;
	}
	
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap(){
		return bitmap;
	}

	public String getAbsoluteUrl(){
		return absoluteUrl;
	}

	public void setAbsoluteUrl(String absoluteUrl){
		this.absoluteUrl = absoluteUrl;
	}
}
