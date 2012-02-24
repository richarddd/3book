package parser.epub;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import parser.ThreeBook;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;

public class EpubBook implements ThreeBook {
	Book book; // The underlying epublib book
	List<EpubChapter> chapters;
	
	public EpubBook(Book epub){
		book = epub;
		chapters = new ArrayList<EpubChapter>(book.getSpine().size()+1);
	}
	
	
	public String getWrappedFileText(Resource textFile) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append(HTML_START).append(EpubBook.getBodyContents(getFileText(textFile))).append(HTML_END);
		
		return sb.toString();
	}
	
	/**
	 * Returns the text of the file listed in the nth position of the spine 
	 * @param index the spine position to load
	 * @return the text contained in the file
	 * @throws IOException 
	 */
	public String getTextBySpineIndex(int index) throws IOException{
		return getFileText(book.getSpine().getResource(index));
	}
	
	/**
	 * Returns the full xhtml of an epub file 
	 * 
	 * @throws IllegalArgumentException if textFile.getMediaType().getName() != "application/xhtml+xml"
	 * @throws IOException  
	 * @param textFile a file of media type application/xhtml+xml
	 * @return
	 */
	public String getFileText(Resource textFile) throws IOException{
		if (textFile.getMediaType().getName().equals("application/xthml+xml")){
    		throw new IllegalArgumentException("Spine content was not xthml, wtf? Media type: " + textFile.getMediaType().getName());
    	}
    	char[] cb = new char[(int) textFile.getSize()]; // XXX size is long, this could go bad-bad?
    	Reader reader = textFile.getReader();
    	
    	int charsRead = reader.read(cb);
    	Log.d("3", "getFileText read " + charsRead + "bytes into buffer of size" + cb.length + ".");
    	return String.copyValueOf(cb);
    
	}
	
	/**
	 * Returns the contents of the html body tag
	 * @param xhtml The xhtml to parse
	 * @return the contents of the body tag
	 */
	public static String getBodyContents(String xhtml){
		// Crappy implementation, make fixings.
		// This helper method could probably move out of here. 
    	int end = xhtml.indexOf("</body>");
		int st1 = xhtml.indexOf("<body");
		int st2 = xhtml.indexOf(">", st1);
		return xhtml.substring(st2+1, end);
    }
	
	// TEMPORARY HELPER METHOD
	public static final String HTML_START = "<html><head></head><body>\n"; 
	public static final String HTML_END = "\n</body></html>";
	public static String wrapInSimpleHtml(String body){
		return HTML_START+body+HTML_END;
	}
	
}
