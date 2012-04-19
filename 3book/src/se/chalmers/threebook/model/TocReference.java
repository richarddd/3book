package se.chalmers.threebook.model;

import java.io.IOException;
import java.util.List;

import nl.siegmann.epublib.domain.Resource;

public interface TocReference {
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object o);
	
	public List<TocReference> getChildren();
	public String getTitle();
	
	/**
	 * Returns a unique identifier that can be used to refer to the section
	 * @return a unique identifier that can be used to refer to the section
	 * 
	 * XXX This exists as Axel hasn't figured out a fast way to implement 
	 * a hashCode that respects the contract of the equals method  
	 */
	public int getUniqueIdentifier();
	
	/**
	 * Returns a string ID supplied by the backing format
	 * 
	 * for ePubs this is the result of calling backingResource.getId() 
	 * 
	 * XXX figure out whether this makes sense and is worthwhile
	 * 		right now it's pretty crap. Currently a hack to support
	 * 		the navigation layer.
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Returns the #id (sans #) reference points to, e.g. "preface" or "ch1_s4"
	 * @return the #id (sans #) reference points to, e.g. "preface" or "ch1_s4"
	 */
	public String getHtmlId();
	
	/**
	 * Returns the relative file name of the file backing the reference
	 * @return the relative file name of the file backing the reference
	 */
	public String getBaseFileName();
	
	/**
	 * Returns the raw HTML of the file backing the reference
	 * @return the raw HTML of the file backing the reference
	 * @throws IOException if the file can't be read
	 */
	public String getHtml() throws IOException;

	public Object getBackingObject();
}
