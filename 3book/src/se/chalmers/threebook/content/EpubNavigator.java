package se.chalmers.threebook.content;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.model.Book;
import se.chalmers.threebook.model.TocReference;

/**
 * Epub implementation of book-navigation facade
 * @author monkeycid
 *
 */
public class EpubNavigator implements BookNavigator {
	
	//private List<HtmlRenderer> renderers;
	
	private EpubContentStream content;
	private HtmlRenderer renderer;
	private TocReference curSection;
	
	private int rwdOffset;
	private int fwdOffset;
	
	private int displayedPage;
	
	private int center; 
	
	private RenderedPage curRender;
	private String tag = "EpubNavigator";
	private Book metaBook;
	
	private int bufferOffset; // keeps track of buffer requests
	private boolean hasBuffered = false;
	private int fwdBufferOffset;
	private int rwdBufferOffset;
	private int basePage;
	
	public EpubNavigator(String bookFileName, File cacheDir, int viewWidth, int viewHeight, int bookObjectHeight, int imageHeightLimit) throws IOException{
		content = new EpubContentStream(bookFileName, cacheDir);
		
		
		this.metaBook = new Book();
		metaBook.setTitle(content.getBook().getTitle());
		this.renderer = new HtmlRenderer(cacheDir, viewWidth, viewHeight, bookObjectHeight, imageHeightLimit, metaBook.getTitle());
	}
	
	// TODO for all methods returning renderedPage - figure out whether they need to return 
	// a rendered page and how they should interact with the adapter. 
	/*
	public RenderedPage getPage(int pNum) throws IOException {
		// TODO: add end/start of source checks
		fwdOffset = 0; 
		rwdOffset = 0; 
		displayedPage = pNum; 
		curRender = renderer.getRenderedPage(pNum);
		return curRender; 
	} */
	
	public RenderedPage movePage(int offset, boolean moveBase) throws IOException {
		Log.d(tag, "movePage called with offset, moveBase: " + offset + ", " + moveBase);
		Log.d(tag, "basePage is:" + basePage);
		if (offset == 0){
			return curRender != null ? curRender : renderer.getRenderedPage(basePage); 
		}
		
		int targetPage = basePage+offset;
		if (renderer.isEndOfSource(targetPage)){
			// handle .. and this is a bitch when straddling
			Log.d(tag, "Reached end of source - not handled yet though.");
			return curRender;
		} else if (targetPage < 0){
			// handle .. and this is a bitch when straddling
			Log.d(tag, "Reached start of page - not handled yet though.");
			return curRender;
		}
		
		curRender = renderer.getRenderedPage(targetPage);
		if (moveBase) { basePage += (offset > 0) ? 1 : -1; }
		
		return curRender; 
	}
	
	/**
	 * 
	 * @param offset must not be larger than the recieving buffer's side
	 * @return
	 * @throws IOException
	 */
	public RenderedPage forwardPage(int offset, boolean moveBase) throws IOException {
		Log.d(tag, "nextPage called with offset " + offset + ", currently on page: " + displayedPage);
		int targetPage = basePage+offset;

		if (renderer.isEndOfSource((targetPage))){ 
			Log.d(tag , "Navigator detected end of source, trying to switch to next source!");
			nextSource(); // TODO detect wide offsets crossing several sources
		}
		
		curRender = renderer.getRenderedPage(targetPage);
		if (moveBase){ basePage++; }
		return curRender;
	} 
	
	/**
	 * 
	 * @param offset must not be larger than the recieving buffer's side
	 * @return
	 * @throws IOException
	 */
	public RenderedPage prevPage(int offset) throws IOException {
		
		return null;
	}
	
	public RenderedPage nextPage() throws IOException{
		Log.d(tag, "nextPage called, currently on page: " + displayedPage);
		
		if (renderer.isEndOfSource((displayedPage+1))){ 
			Log.d(tag , "Navigator detected end of source, trying to switch to next source!");
			return nextSource();
		}
		//curRender = renderer.getRenderedPage(hasBuffered ? curPage+fwdBufferOffset : ++curPage);
		curRender = renderer.getRenderedPage(++displayedPage);
		hasBuffered = false; 
		return curRender;
	}
	
	public RenderedPage bufferNextPage() throws IOException{
		fwdBufferOffset++;
		hasBuffered = true; 
		return nextPage();
	}
	
	public RenderedPage bufferPrevPage() throws IOException{
		rwdBufferOffset++;
		hasBuffered = true; 
		return prevPage();
	}
	
	public RenderedPage prevPage() throws IOException{
		Log.d(tag, "prevPage called, currently on page: " + displayedPage);
		
//		if (!hasBuffered){
//			rwdBufferOffset = 0;
//		}
		
		if (displayedPage <= 0){ 
			Log.d(tag, "Navigator detected start of source, trying to switch to previous source!");
			return prevSource(); 
		}
		//curRender = renderer.getRenderedPage(hasBuffered ? curPage-rwdBufferOffset : --curPage);
		curRender = renderer.getRenderedPage(--displayedPage);
		hasBuffered = false; 
		return curRender;
	}
	
	public RenderedPage nextSource() throws IOException{
		if (!content.hasNextSource(curSection)){ // at end of book! 
			return curRender;
		}
		displayedPage = 0;
		curSection = content.getNextSource(curSection);
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		curRender = renderer.getRenderedPage(0);
		return curRender;
	}
	
	public RenderedPage prevSource() throws IOException{
		if (!content.hasPrevSource(curSection)){ // at start of book!
			Log.d(tag, "PrevSource called but at beginning of book - returning last render.");
			return curRender; 
		}
		displayedPage = 0;
		curSection = content.getPreviousSource(curSection);
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		curRender = renderer.getRenderedPage(0);
		return curRender;
	}
	
	public int toSection(TocReference section) throws IOException{
		curSection = section;
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		displayedPage = renderer.getPageNumber(curSection.getHtmlId()); // TODO check if getHtmlId makes any sense!
		return displayedPage;
		//return nextPage(); // XXX due to renderer currently returning previous page!
		// TODO figure out a good way to distribute renders and page numbers - maybe send out renderEvents? 
	}
	
	public TableOfContents getToc(){
		return content.getToc();
	}

	public Book getBook() {
		return metaBook;
	}
	
}
