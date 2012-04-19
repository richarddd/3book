package se.chalmers.threebook.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.chalmers.threebook.content.BookCache;
import se.chalmers.threebook.util.HtmlParser;

import android.util.Log;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

public final class EpubTocReference implements TocReference {

	private static final String tag = EpubTocReference.class.toString();
	
	private final TocReference parent;
	private final int depth;
	private final int psuedoHash; // XXX might not be a good hash! 
	private final TOCReference ref;
	private final List<TocReference> children;
	private final BookCache cache;
	
	public EpubTocReference(TOCReference ref, BookCache cache){
		this(ref, 0, cache);
	}
	
	public EpubTocReference(TOCReference ref, int depth, BookCache cache){
		this(ref, depth, null, cache);
	}
	
	private EpubTocReference(TOCReference ref, int depth, TocReference parent, BookCache cache){
		this.parent = parent;
		this.depth = depth;
		this.ref = ref;
		this.cache = cache;
		psuedoHash = (ref.getCompleteHref()+ref.getFragmentId()).hashCode(); 
		if (ref.getChildren().size() > 0){
			children = new ArrayList<TocReference>(ref.getChildren().size());
			for (TOCReference cr : ref.getChildren()){
				children.add(new EpubTocReference(cr, (depth+1), parent, cache));
			}
		} else {
			children = new ArrayList<TocReference>(0);
		}
	}
	
	public List<TocReference> getChildren() {
		return children;
	}

	public String getTitle() {
		return ref.getTitle();
	}

	public String getHtmlId() {
		return ref.getFragmentId();
	}

	public String getBaseFileName() {
		// this is the same as calling Resource.getHref(), e.g. no fragmentId.
		String cHref = ref.getCompleteHref();
		int hashPos = cHref.lastIndexOf("#");
		return hashPos < 0 ? cHref : cHref.substring(0, hashPos);  
	}
	

	public String getHtml() throws IOException {
		return getResourceContents();
	}
	
	public int getUniqueIdentifier() {
		return psuedoHash;
	}

	public String getId() {
		return ref.getResource().getId();
	}
	
	/**
	 * Returns the level of the reference in the TOC tree
	 * @return the level of the reference in the TOC tree
	 */
	public int getDepth(){
		return depth;
	}
	
	/**
	 * Returns the parent of the reference, or null if top-level element
	 * @return the parent of the reference, or null if top-level element
	 */
	public TocReference getParent(){
		return parent;
	}
	
	// XXX consider moving this out of the class - should it really be able to unpack itself?
	private String getResourceContents() throws IOException{
		String id = String.valueOf(getUniqueIdentifier()); // hashcode as string
		if (cache.exists(id)){
			return cache.retrieve(id);
		}
		
		String data; // 
		{
			char[] cb = new char[(int) ref.getResource().getSize()]; // XXX size is long, this could go bad-bad?
	    
	    	int charsRead = ref.getResource().getReader().read(cb);
	    	Log.d(tag, "getResourceContents for " + ref.getTitle() + " read " + charsRead + "bytes into buffer of size" + cb.length + ".");
	    	data = String.copyValueOf(cb);
		}
    	
		// PERFORM STRING PROCESSING
		HtmlParser p = new HtmlParser(data);
		p.injectCss(HtmlParser.BASIC_STYLE);
		List<String> imageNames = p.getImg();
		Map <String, String> headers = p.getHeadings();
		data = p.getModifiedHtml(); // rewritten HTML
		cache.cache(data, id); // Place the data in the cache
		
		// UNZIP AND PLACE IMAGES AS NEEDED
		for (String imgName : imageNames){
			if (!cache.exists(imgName)){  
				cache.cacheImage(imgName);
			} 
		}
		
    	return data;
	}
	
	// XXX look into equals and hashCode with the help of Bloch when less tired!
	@Override
	public boolean equals(Object o){
		if (!(o instanceof EpubTocReference)) return false;
		return hashCode() == o.hashCode();
	}
	
	@Override
	public int hashCode(){
		return getUniqueIdentifier();
	}

	public Object getBackingObject() {
		return ref;
	}
}
