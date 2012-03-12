package se.chalmers.threebook.content;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.chalmers.threebook.util.HtmlParser;
import se.chalmers.threebook.util.WriterHelper;

import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import se.chalmers.threebook.model.Bookmark;
import android.app.Activity;
import android.util.Log;

public class EpubContentStream implements ContentStream {

	private Navigator nav;
//	private Map<String,Resource> tocCache;
	private Activity parent; 
	
	public EpubContentStream(Book book, Activity parent){
		nav = new Navigator(book);
		this.parent = parent;
//		tocCache = new HashMap<String,Resource>((int) (nav.getBook().getTableOfContents().size() * 1.25));
	}
	
	/**
	 * Returns content of next spine entry, or current sections content if at end
	 */
	public String next() throws IOException{
		nav.gotoNextSpineSection(this);
		return stripHeadFromHtml(getStringFromResource(nav.getCurrentResource()));
	}

	/**
	 * Returns content of previous spine entry, or current section content if at
	 * beginning
	 */
	public String previous() throws IOException{
		nav.gotoPreviousSpineSection(this);
		return stripHeadFromHtml(getStringFromResource(nav.getCurrentResource()));
	}

	public String jumpToToc(TOCReference ref) throws IOException, FileNotFoundException {
		Resource chapter = ref.getResource();
		nav.gotoResource(chapter, 0, ref.getFragmentId(), this);
		
		if (WriterHelper.chapterCached(nav.getBook().getTitle(), ref.getTitle())){
			Log.d("3", "file is cached, returning like a boss!");
			return WriterHelper.getCachedFileName(nav.getBook().getTitle(), ref.getTitle());
		}
		//TODO: Inject anchor-jumping javascript or other anchor solution
		String data = stripHeadFromHtml(getStringFromResource(nav.getCurrentResource()));
		// PERFORM STRING PROCESSING
		HtmlParser p = new HtmlParser(data);
		p.injectCss(HtmlParser.basicStyle);
		List<String> imageNames = p.getImg();
		Map <String, String> headers = p.getHeadings();
		if (headers.size() > 0){
			for (Entry<String,String> e : headers.entrySet()){
				Log.d("3", "Heading key/value: " + e.getKey() + "/" + e.getValue());
			}
		}
		data = p.getModifiedHtml(); // rewritten HTML
		Log.d("3", "Html data: \n" + data.substring(0,8000));
		
		// UNZIP AND PLACE IMAGES AS NEEDED
		for (String s : imageNames){
			Log.d("3","trying to get image resource by href: ");
			Resource r = nav.getBook().getResources().getByHref(s);
			if (r != null){
				Log.d("3", "title of resource is: " + r.getTitle());
				WriterHelper.writeImage(r.getData(), nav.getBook().getTitle(), s, parent);
			}
		}
		// FINALLY WRITE THE DAMN HTML FILE WITH THE BOOK
		return WriterHelper.writeFile(data, nav.getBook().getTitle(), ref.getTitle(), parent); // GET PARENT!
	}
	
	
	public String jumpTo(int index) throws IOException, FileNotFoundException{
		return jumpToToc(nav.getBook().getTableOfContents().getTocReferences().get(index));
	}
	
	public String jumpTo(Position position){
		Log.d("3", "jumpTo(Position position) called, empty string returned - method not implemented yet");
		
		return "";
	}

	public List<String> getToc() {
		return getTopLevelToc(nav.getBook().getTableOfContents().getTocReferences());
	}
	

	public List<Bookmark> getBookmarks() {
		Log.d("3", "GetBookmakrs called, empty list returned - method not implemented yet");
		return new ArrayList<Bookmark>();
	}

	private String getStringFromResource(Resource res) throws IOException{
		char[] cb = new char[(int) res.getSize()]; // XXX size is long, this could go bad-bad?
    	Reader reader = res.getReader();
    	
    	int charsRead = reader.read(cb);
    	Log.d("3", "EpubContentStream getStringFromResource read " + charsRead + "bytes into buffer of size" + cb.length + ".");
    	return String.copyValueOf(cb);
	}
	
	private String stripHeadFromHtml(String xhtml){
		StringBuilder sb = new StringBuilder();
		return sb.append(HTML_START).append(stripBody(xhtml)).append(HTML_END).toString();
	}
	
	private String stripBody(String xhtml){
		// Crappy implementation, make fixings.
		// This helper method could probably move out of here. 
    	int end = xhtml.indexOf("</body>");
		int st1 = xhtml.indexOf("<body");
		int st2 = xhtml.indexOf(">", st1);
		return xhtml.substring(st2+1, end);
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
	
	public static final String HTML_START = "<html><head></head><body>\n"; 
	public static final String HTML_END = "\n</body></html>";
}
