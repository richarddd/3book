package se.chalmers.threebook.content;

import se.chalmers.threebook.html.HtmlRenderer;

/**
 * A class that stores lazy references to HTMLRenderers in sufficient number 
 * to potentially offer one renderer per buffered page. This is required when
 * chapters are very short. 
 * 
 */
public class HtmlRendererBuffer {

	private HtmlRenderer[] buffer;
	
	public HtmlRendererBuffer(int sideBuffer){
		buffer = new HtmlRenderer[1+sideBuffer*2];
	}
}
