package se.chalmers.threebook.content.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.content.BookCache;
import se.chalmers.threebook.content.TableOfContents;
import se.chalmers.threebook.model.Bookmark;
import se.chalmers.threebook.model.TocReference;
import android.util.Log;

public class EpubContentStream { //implements ContentStream {

	//private Navigator nav;
	private BookCache cache;
	private Book book;	// yay, we can use a real book!
	private TableOfContents toc;
	private Map<String, Integer> hrefOrder =	// used by TocRefs to find their 
		new HashMap<String,Integer>();			// place in the TocRef partial order
	
	private Map<Integer, TocReference> sourceOrder =  	// Used to find TocRef based
		new HashMap<Integer,TocReference>();			// on their place in partial order
	private String tag = "EpubContentStream";

	
	/**
	 * 
	 * @param bookFile	the complete path to the book
	 * @param cacheDir	the path to the cache, provided by an android view
	 * @throws IOException if cache directories cannot be created 
	 * @throws IOException if book cannot be opened
	 */
	public EpubContentStream(String bookFile, File cacheDir) throws IOException{
		
		book = new EpubReader().readEpub(new FileInputStream(bookFile)); 
		cache = new EpubCache(book.getTitle(), cacheDir, book);
		toc = new EpubTableOfContents(book.getTableOfContents(), cache);
		
		{ 	int i = 0;
			int debugI = 0; // TODO remove
			// Do note that we're iterating over the TOC, which is not guaranteed to be the right order
			// At some point we will have to figure out how to balance TOC, Spine and Guide.
			// This looks like O(n*m) but should be more like O(n+m+C){n=tocSize, m=uniqueRefs}
			for (Resource source : book.getTableOfContents().getAllUniqueResources()){
				hrefOrder.put(source.getHref(), i);	// establish mapping between HREF and order
				
				Queue<TocReference> tocDeque = new LinkedList<TocReference>(toc.getLinearToc());
				TocReference head = null;
				do {
					// strip away til we match
					head = tocDeque.poll(); 
					debugI++;
				} while (tocDeque.size() != 0 && !(head.getBaseFileName().equals(source.getHref()))); // TODO remove size once we've figured this out.
				sourceOrder.put(i, head); // establish mapping between order and start of href source
				i++;  
		}
			Log.d("EpubContentStream", i + " resources processed against " + toc.getLinearToc().size() + "records, for at most " + (i*toc.getLinearToc().size()) + "iterations. ACtual iterations: " + debugI);
		}
		
		
		//nav = new Navigator(book);
	}
	
	public boolean hasNextSource(TocReference section){
		return hrefOrder.get(section.getBaseFileName()) < hrefOrder.size(); 
		
	} 
	
	public boolean hasPrevSource(TocReference section){
		Log.d(tag , "Checking whether previous resource exists. Current resource has index: " + book.getSpine().getResourceIndex((	(TOCReference) section.getBackingObject()).getResource()));
		return book.getSpine().getResourceIndex((	(TOCReference) section.getBackingObject()).getResource()) > 0; // XXX haxx 
		//return hrefOrder.get(section.getBaseFileName()) >= 0;
	}
	
	public TocReference getPreviousSource(TocReference relativeTo){
		return sourceOrder.get(hrefOrder.get(relativeTo.getBaseFileName())-1);
	}
	
	public TocReference getNextSource(TocReference relativeTo){
		return sourceOrder.get(hrefOrder.get(relativeTo.getBaseFileName())+1);
	}	

	public List<String> getTocNames() {
		//return getTopLevelToc(nav.getBook().getTableOfContents().getTocReferences());
		return getTopLevelToc(book.getTableOfContents().getTocReferences());
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
	
	public Book getBook(){
		return book;
	}
	
}
