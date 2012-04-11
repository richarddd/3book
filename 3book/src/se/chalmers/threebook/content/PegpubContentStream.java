package se.chalmers.threebook.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.model.Bookmark;
import se.chalmers.threebook.model.EpubTocReference;
import se.chalmers.threebook.model.TocReference;
import se.chalmers.threebook.util.HtmlParser;
import android.util.Log;

public class PegpubContentStream {

	//private Navigator nav;
	private BookCache cache;
	private Book book;	// yay, we can use a real book!
	private TableOfContents toc;
	private Map<String, Integer> hrefOrder; // used by TocRefs to find their 
											// place in the TocRef partial order
	
	private Map<Integer, TocReference> sourceOrder; // Used to find TocRef based
													// on their place in partial order

	
	/**
	 * 
	 * @param bookFile	the complete path to the book
	 * @param cacheDir	the path to the cache, provided by an android view
	 * @throws IOException if cache directories cannot be created 
	 * @throws IOException if book cannot be opened
	 */
	public PegpubContentStream(String bookFile, File cacheDir) throws IOException{
		
		book = new EpubReader().readEpub(new FileInputStream(bookFile)); 
		cache = new EpubCache(book.getTitle(), cacheDir);
		toc = new EpubTableOfContents(book.getTableOfContents(), cache);
		
		{ 	int i = 0;
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
				} while (head.getBaseFileName() != source.getHref());
				sourceOrder.put(i, head); // establish mapping between order and start of href source
				i++;
		}}
		
		
		//nav = new Navigator(book);
	}
	
	public boolean hasNextSource(TocReference section){
		return hrefOrder.get(section.getBaseFileName()) < hrefOrder.size(); 
		
	}
	
	public boolean hasPrevSource(TocReference section){
		return hrefOrder.get(section.getBaseFileName()) >= 0;
	}
	
	public TocReference getPreviousSource(TocReference relativeTo){
		return sourceOrder.get(hrefOrder.get(relativeTo.getBaseFileName())-1);
	}
	
	public TocReference getNextSource(TocReference relativeTo){
		return sourceOrder.get(hrefOrder.get(relativeTo.getBaseFileName())+1);
	}	
	
	private String jumpToToc(TOCReference ref) throws IOException, FileNotFoundException {
		Resource chapter = ref.getResource();
		//nav.gotoResource(chapter, 0, ref.getFragmentId(), this);
		String chapIdent = ref.getTitle();
		
		if (cache.exists(chapIdent)){ //TODO: use more unique identifier!  
			return cache.retrieve(ref.getTitle()).getAbsolutePath();
			// if we have cached the file before the images were also dealt with
		}
		
		// PERFORM STRING PROCESSING
		String data = getStringFromResource(chapter);
		HtmlParser p = new HtmlParser(data);
		p.injectCss(HtmlParser.BASIC_STYLE);
		List<String> imageNames = p.getImg();
		Map <String, String> headers = p.getHeadings();
		data = p.getModifiedHtml(); // rewritten HTML
		
		// UNZIP AND PLACE IMAGES AS NEEDED
		for (String s : imageNames){
			if (cache.exists(s)){continue;} // some other file has already cached this image
			//Resource r = nav.getBook().getResources().getByHref(s);
			Resource r = book.getResources().getByHref(s);
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
		//return jumpToToc(nav.getBook().getTableOfContents().getTocReferences().get(index));
		return jumpToToc(book.getTableOfContents().getTocReferences().get(index));
	}
	
	public String jumpTo(Position position){
		throw new UnsupportedOperationException("jumpTo(Position p) not implemented yet");
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
	
}
