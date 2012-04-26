package se.chalmers.threebook.content;

import java.util.Arrays;

import se.chalmers.threebook.html.HtmlRenderer;

public class HtmlRendererCache { // implements RendererCache ?
	
	private HtmlRenderer[] cache;
	private int sideBufferLen;
	private int center; // index for center of cache
	private int fwdBufCount; // number of active renderers buffered "to the right"
	private int rwdBufCount; // number of active renderers buffered "to the left"
	
	public HtmlRendererCache (HtmlRenderer centerRenderer, int sideBufferLength){
		this.sideBufferLen = sideBufferLength;
		cache = new HtmlRenderer[sideBufferLength*2+1];
		center = cache.length / 2;
		setCenterRenderer(centerRenderer);
	}

	public void addNextRenderer(HtmlRenderer render){
		// rwdBufCount is here set to either the closest-to-center unoccupied 
		// right buffer slot or the furthest-to-the-right slot.
		fwdBufCount = (fwdBufCount+1 > sideBufferLen) ? sideBufferLen : fwdBufCount+1;
		cache[fwdBufCount] = render;
	}
	
	public void addPreviousRenderer(HtmlRenderer render){
		// rwdBufCount is here set to either the closest-to-center unoccupied 
		// left buffer slot or the furthest-to-the-left slot.
		rwdBufCount = (rwdBufCount+1 > sideBufferLen) ? sideBufferLen : rwdBufCount+1;
		cache[rwdBufCount] = render;
	}
	
	public void setCenterRenderer(HtmlRenderer render){
		for (int i = 0, len = cache.length; i < len; i++){
			// uninitialize (drop the heavy loads) for all renderers 
			// unless one of them is the new center renderer, in which case
			// we keep it as not to have to re-parse everything

			// If we can figure out a neat way to check if other renderers will
			// be reused, that would be neat as well. Probably a worthwhile
			// endeavour, given that most renderers will be heavy. TODO
			if (cache[i] != null && !cache[i].equals(render)){
				cache[i] = null; // as long as no one is sloppy with their references, this should help out free memory.
				// note: left null-check in above as this behaviour might change and the unwary programmer could
				// forget to re-add the null-check.
			}
		}
		cache[center] = render;
		fwdBufCount = 0;
		rwdBufCount = 0;
	}
	
	public void clear(){
		for (int i = 0, len = cache.length; i < len; i++){
			cache[i] = null;
		}
		fwdBufCount = 0;
		rwdBufCount = 0;
	}
}
