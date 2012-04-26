package se.chalmers.threebook.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Color;

public class RendererConfig {
	
	private static RendererConfig instance;
	
	private int baseTextSize;
	private int bookObjectHeight;
	private int xMargin;
	private int yMargin;
	private int rowMargin;
	private int textColor;
	private Bitmap backgroundBitmap;
	
	private Set<RenderConfigListener> listeners = new HashSet<RenderConfigListener>();
	
	public static RendererConfig instance(){
		if (instance == null){
			instance = new RendererConfig();
		}
		return instance;
	}
	
	private RendererConfig(){
		// default values
		// TODO: pulling these from a config file could be neat rather than hardcoding them.
		baseTextSize = 26;
		yMargin = 10;
		xMargin = 10;
		textColor = Color.BLACK;
		rowMargin = (int) (baseTextSize * 0.2);
	}
	
	public void configure(int baseTextSize, int bookObjectHeight, int xMargin, int yMargin, int rowMargin, int textColor, Bitmap backgroundBitmap){
		this.baseTextSize = baseTextSize;
		this.xMargin = xMargin;
		this.yMargin = yMargin;
		this.textColor = textColor;
		configure(bookObjectHeight, backgroundBitmap);
	}
	
	public void configure(int bookObjectHeight, Bitmap backgroundBitmap){
		this.backgroundBitmap = backgroundBitmap;
		configure(bookObjectHeight);
	}
	
	public void configure(int bookObjectHeight){
			this.bookObjectHeight = bookObjectHeight;
			updateListeners();
	}
	
	private void updateListeners(){
		for (RenderConfigListener l : listeners){
			l.onConfigChanged(baseTextSize, bookObjectHeight, xMargin, yMargin, rowMargin, textColor, backgroundBitmap);
		}
	}
	
	public void addRenderConfigListner(RenderConfigListener l){
		listeners.add(l);
	}	
	public interface RenderConfigListener{
		void onConfigChanged(int baseTextSize, int bookObjectHeight, int xMargin, int yMargin, int rowMargin, int textColor, Bitmap backgroundBitmap);
	}
	
}
