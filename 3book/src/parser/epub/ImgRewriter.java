package parser.epub;

import android.util.Log;

public class ImgRewriter {
/**
 * Ugly, slow as all hell naive implementation; written to see if the approach is at all tenable.
 * Didn't want to dig into regexp to do this. SHould have. god damn it.
 */
	
	public static String rewriteImages(String text, String baseFileUrl){
		// I actually feel bad writing this.
		// This will fail in tons of circumstances too. 
		// God damn shit fuck cock.
		int openTag = text.indexOf("<img src=\"");
		if (openTag == -1){
			Log.d("3", "there are no images, fuck this");
			return text; // EARLY BAIL - GET ME OUT OF THIS SHITTY CODE SHIT SHIT SHIT
		}
		Log.d("3", "there are images, god help us all!");
		int openFnutt = openTag+10; 
		int closeFnutt = text.indexOf("\"", (openTag+10+1));
		int closeTag = text.indexOf(">", closeFnutt);
		
		String fileName = text.substring(openFnutt, closeFnutt);
		String oldTag = text.substring(openTag, ++closeTag);
		Log.d("3", "The file name should be: " + fileName);
		Log.d("3", "The tag name should be: " + oldTag);
		String newTag = "<img src=\""+baseFileUrl+fileName+"\"/>";
		Log.d("3", "The masterfully crafted new tag is :"+ newTag);
		
		text = text.replace(oldTag, newTag);
		
		return text;
		
	}
}
