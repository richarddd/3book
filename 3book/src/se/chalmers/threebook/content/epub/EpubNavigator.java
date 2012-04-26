package se.chalmers.threebook.content.epub;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import se.chalmers.threebook.content.BookNavigator;
import se.chalmers.threebook.content.HtmlRendererCache;
import se.chalmers.threebook.content.TableOfContents;
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
	private HtmlRendererCache renderCache;
	
	private EpubContentStream content;
	private HtmlRenderer renderer;
	private TocReference curSection;
	
	private HtmlRenderer nextRenderer, prevRenderer;
	
	private int nextSourcePagesRendered = 0;
	private int prevSourcePagesRendered = 0;
	
	private int curPage;
	
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
		renderCache = new HtmlRendererCache(renderer, 2); // TODO: remove magic number 2!
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
		Log.d(tag, "movePage called with basePage/offset, moveBase: " + basePage +"/"+ offset + ", " + moveBase);
		Log.d(tag, "basePage is:" + basePage);
		if (offset == 0){
			return curRender != null ? curRender : renderer.getRenderedPage(basePage); 
		}
		
		int targetPage = basePage+offset;
		
		if (!renderer.hasPage(targetPage)){
			Log.d(tag, "next page requested is not in current source. Must work magic to swap in new renderer.");
			curRender = nextRenderer.getRenderedPage(nextSourcePagesRendered++);
			
			int magicNumber = 1; // XXX remove magic number with programmatical sidebuffer-1
			if (nextSourcePagesRendered > magicNumber){ nextSource(); } // time to move on!
			
			
		} else if (targetPage < 0){
			Log.d(tag, "previous page requested is not in current source. Must work magic to swap in new renderer.");
			curRender = prevRenderer.getRenderedPage(prevSourcePagesRendered++);
			
			int magicNumber = 1; // XXX remove magic number with programmatical sidebuffer-1
			if (prevSourcePagesRendered > magicNumber){ prevSource(); } // time to move on!
		
		} else {
			curRender = renderer.getRenderedPage(targetPage);
		}
		
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
		Log.d(tag, "nextPage called with offset " + offset + ", currently on page: " + curPage);
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
		Log.d(tag, "nextPage called, currently on page: " + curPage);
		
		if (renderer.isEndOfSource((curPage+1))){ 
			Log.d(tag , "Navigator detected end of source, trying to switch to next source!");
			return nextSource();
		}
		//curRender = renderer.getRenderedPage(hasBuffered ? curPage+fwdBufferOffset : ++curPage);
		curRender = renderer.getRenderedPage(++curPage);
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
		Log.d(tag, "prevPage called, currently on page: " + curPage);
		
//		if (!hasBuffered){
//			rwdBufferOffset = 0;
//		}
		
		if (curPage <= 0){ 
			Log.d(tag, "Navigator detected start of source, trying to switch to previous source!");
			return prevSource(); 
		}
		//curRender = renderer.getRenderedPage(hasBuffered ? curPage-rwdBufferOffset : --curPage);
		curRender = renderer.getRenderedPage(--curPage);
		hasBuffered = false; 
		return curRender;
	}
	
	public RenderedPage nextSource() throws IOException{
		Log.d(tag, "nextSource() called - this should be blazing fast!");
		if (!content.hasNextSource(curSection)){ // at end of book! 
			return curRender;
		}
		curPage = 0;
//		curSection = content.getNextSource(curSection);
//		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		setRenderersRelativeTo(curSection, 1); 
		curRender = renderer.getRenderedPage(0);
		return curRender;
	}
	
	public RenderedPage prevSource() throws IOException{
		Log.d(tag, "prevSource() called - this might take some rendering...");
		if (!content.hasPrevSource(curSection)){ // at start of book!
			Log.d(tag, "PrevSource called but at beginning of book - returning last render.");
			return curRender; 
		}
		curPage = 0;
		//curSection = content.getPreviousSource(curSection);
		//renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		setRenderersRelativeTo(curSection, -1);  
		curRender = renderer.getRenderedPage(renderer.getEosPageNumber());
		return curRender;
	}
	
	public int toSection(TocReference section) throws IOException{
		curSection = section;
		
		setRenderersRelativeTo(curSection, 0);
		curPage = renderer.getPageNumber(curSection.getHtmlId()); // TODO check if getHtmlId makes any sense!
		
		return curPage;
		 
	}
	
	/**
	 * Sets up forward and backward renderers next to passed section
	 * @param section
	 * @param offset the position of the section: -1 = "was prev" 0 = "new center" 1="was right" 
	 * @throws IOException
	 */
	private void setRenderersRelativeTo(TocReference section, int offset) throws IOException{
		
		TocReference prevSource = null;
		TocReference nextSource = null;
		
		switch (offset){
		case 0: // we're setting an entirely new renderer-set
			prevRenderer = renderer.getBlankRenderer();
			nextRenderer = renderer.getBlankRenderer();
			renderer.setHtmlSource(section.getHtml(), section.getUniqueIdentifier());
			prevSource = content.getPreviousSource(curSection);
			prevRenderer.setHtmlSource(prevSource.getHtml(), prevSource.getUniqueIdentifier());
			nextSource = content.getNextSource(curSection);
			nextRenderer.setHtmlSource(nextSource.getHtml(), nextSource.getUniqueIdentifier());
			break;
		case -1: // we're moving the renderers one step backward 
			// old nextRenderer is overwritten
			// a new prevRenderer is needed
			nextRenderer = renderer;
			renderer = prevRenderer;
			prevRenderer = renderer.getBlankRenderer();
			prevSource = content.getPreviousSource(curSection);
			prevRenderer.setHtmlSource(prevSource.getHtml(), prevSource.getUniqueIdentifier());
			break;
		case 1: // we're moving the renderers one step forward
			// old prevRenderer is overwritten
			// a new nextRenderer is required
			prevRenderer = renderer;
			renderer = nextRenderer;
			nextRenderer = renderer.getBlankRenderer();
			nextSource = content.getNextSource(curSection);
			nextRenderer.setHtmlSource(nextSource.getHtml(), nextSource.getUniqueIdentifier());
			break;
		}
		
	}
	
	public TableOfContents getToc(){
		return content.getToc();
	}

	public Book getBook() {
		return metaBook;
	}
	
}
