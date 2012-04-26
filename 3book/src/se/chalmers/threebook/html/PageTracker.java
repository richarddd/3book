package se.chalmers.threebook.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for the HTML-renderer, caching data about where pages begin and where sources end.
 * 
 * Terminology: eos is "end of source", when the contents of a source file has all been rendered.
 */
class PageTracker {
	
	private static PageTracker instance;
	
	private PageTracker(){}
	public static PageTracker instance(){
		if (instance == null){
			instance = new PageTracker();
		}
		
		return instance;
	}
	
	private static final int PAGE_COUNT_INITIAL_CAPACITY = 20;
	
	
	private Map<Integer, Integer> eosPageNumCache = new HashMap<Integer, Integer>();
	private Map<Integer, List<Integer>> pageStartCache = new HashMap<Integer, List<Integer>>();
	
	/**
	 * Invalidates the entire cache, throwing everything out
	 * 
	 * Call this when something is changed that impacts how much will fit on a 
	 * page, for instance when the font size has been changed.
	 */
	public void invalidate(){
		eosPageNumCache.clear();
		pageStartCache.clear();
	}
	
	/**
	 * Returns the page where the source ends or -1 if this is unknown
	 * @param sourceIdent an integer uniquely representing the source
	 * @return the page where the source ends or -1 if this is unknown
	 */
	public int getEosPageNum(int sourceIdent){
		return eosPageNumCache.containsKey(sourceIdent) ? eosPageNumCache.get(sourceIdent) : -1;
	}
	
	/**
	 * Stores the page where the source ends
	 * @param sourceIdent an integer uniquely representing the source
	 * @param value the page at where the source ends
	 */
	public void setEosPageNum(int sourceIdent, int value){
		eosPageNumCache.put(sourceIdent, value);
	}
	
	/**
	 * Returns the list of start words for source or a new list if none exists
	 * @param sourceIdent an integer uniquely representing the source
	 * @return the list of start words for source or a new list if none exists
	 * 
	 * Callers should keep the reference and update it; when no longer needed
	 * they can simply drop it and it will be stored 'ere. 
	 */
	public List<Integer> getPageStartList(int sourceIdent){
		List<Integer> list = pageStartCache.get(sourceIdent); 
		if (list == null){
			list = new ArrayList<Integer>(PAGE_COUNT_INITIAL_CAPACITY);
			list.add(0);
		}
		return list; // the idea is that this value will be modified by caller
	}
	
}
