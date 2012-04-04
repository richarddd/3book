package se.chalmers.threebook.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import nl.siegmann.epublib.domain.TOCReference;

public final class EpubTocReference implements TocReference {

	private static final String tag = EpubTocReference.class.toString();
	
	private final TocReference parent;
	private final int depth;
	private final int psuedoHash; // XXX might not be a good hash! 
	private final TOCReference ref;
	private final List<TocReference> children;
	
	public EpubTocReference(TOCReference ref){
		this(ref, 0);
	}
	
	public EpubTocReference(TOCReference ref, int depth){
		this(ref, depth, null);
	}
	
	private EpubTocReference(TOCReference ref, int depth, TocReference parent){
		this.parent = parent;
		this.depth = depth;
		this.ref = ref;
		psuedoHash = (ref.getCompleteHref()+ref.getFragmentId()).hashCode(); 
		if (ref.getChildren().size() > 0){
			children = new ArrayList<TocReference>(ref.getChildren().size());
			for (TOCReference cr : ref.getChildren()){
				children.add(new EpubTocReference(cr, (depth+1), parent));
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
		return getResourceContents(ref);
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
	private static String getResourceContents(TOCReference ref) throws IOException{
		char[] cb = new char[(int) ref.getResource().getSize()]; // XXX size is long, this could go bad-bad?
    	
    	int charsRead = ref.getResource().getReader().read(cb);
    	Log.d(tag, "getResourceContents read " + charsRead + "bytes into buffer of size" + cb.length + ".");
    	return String.copyValueOf(cb);
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
}
