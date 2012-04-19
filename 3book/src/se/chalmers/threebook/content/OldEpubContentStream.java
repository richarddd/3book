package se.chalmers.threebook.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import se.chalmers.threebook.model.Bookmark;
import se.chalmers.threebook.util.HtmlParser;
import android.util.Log;

public class OldEpubContentStream implements ContentStream {

	private Navigator nav;
	private BookCache cache;
	private se.chalmers.threebook.content.TableOfContents toc;
	
	/**
	 * 
	 * @param book
	 * @param cacheDir
	 * @throws IOException if cache directories cannot be created
	 */
	public OldEpubContentStream(Book book, File cacheDir) throws IOException{
		nav = new Navigator(book);
		//cache = new EpubCache(book.getTitle(), cacheDir);
		//toc = new EpubTableOfContents(book.getTableOfContents());
		
	}
	
	/**
	 * Returns content of next spine entry, or current sections content if at end
	 */
	public String next() throws IOException{
		throw new UnsupportedOperationException("next() not implemented yet");
	}

	/**
	 * Returns content of previous spine entry, or current section content if at
	 * beginning
	 */
	public String previous() throws IOException{
		throw new UnsupportedOperationException("previous() not implemented yet");
	}

	public String jumpToToc(TOCReference ref) throws IOException, FileNotFoundException {
		Resource chapter = ref.getResource();
		nav.gotoResource(chapter, 0, ref.getFragmentId(), this);
		String chapIdent = ref.getTitle();
		
		if (cache.exists(chapIdent)){ //TODO: use more unique identifier!  
		//	return cache.retrieve(ref.getTitle()).getAbsolutePath();
			// if we have cached the file before the images were also dealt with
		}
		
		// PERFORM STRING PROCESSING
		String data = getStringFromResource(nav.getCurrentResource());
		HtmlParser p = new HtmlParser(data);
		//p.injectCss(HtmlParser.BASIC_STYLE); // CSS no longer used
		List<String> imageNames = p.getImg();
		Map <String, String> headers = p.getHeadings();
		data = p.getModifiedHtml(); // rewritten HTML
		
		// UNZIP AND PLACE IMAGES AS NEEDED
		for (String s : imageNames){
			if (cache.exists(s)){continue;} // some other file has already cached this image
			Resource r = nav.getBook().getResources().getByHref(s);
			if (r == null){
				Log.d("3","ERRORRRR: We thought we had an image, but it ran away. THIS IS BAD! Tell Axel! name was: " + s);
				continue;
			}
			
			cache.cache(r.getData(), s);
		}
		
		// FINALLY WRITE THE DAMN HTML FILE WITH THE BOOK
		return cache.cache(data, chapIdent).getAbsolutePath();
	}
	
	
	public String jumpTo(int index) throws IOException, FileNotFoundException{
		return jumpToToc(nav.getBook().getTableOfContents().getTocReferences().get(index));
	}
	
	public String jumpTo(Position position){
		throw new UnsupportedOperationException("jumpTo(Position p) not implemented yet");
	}

	public List<String> getTocNames() {
		return getTopLevelToc(nav.getBook().getTableOfContents().getTocReferences());
	}
	
	public List<Bookmark> getBookmarks() {
		throw new UnsupportedOperationException("getBookmarks() not implemented yet");
	}

	private String getStringFromResource(Resource res) throws IOException{
		char[] cb = new char[(int) res.getSize()]; // XXX size is long, this could go bad-bad?
    	
    	int charsRead = res.getReader().read(cb);
    	Log.d("3", "EpubContentStream getStringFromResource read " + charsRead + "bytes into buffer of size" + cb.length + ".");
    	return String.copyValueOf(cb);
	}

	public TableOfContents getToc() {
		return toc;
	}
	
	

	/**
	 * Return the top-level wi
	 * 
	 * @param tocReferences
	 *            TODO: Implement true nested toc using a Tree later
	 */
	private List<String> getTopLevelToc(List<TOCReference> tocReferences) {
		List<String> list = new ArrayList<String>(20);
		
		for (TOCReference ref : tocReferences){
			list.add(ref.getTitle());
			
		}
		return list; 
	}
	
}
