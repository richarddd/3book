package se.chalmers.threebook.content;

import java.io.File;
import java.io.IOException;

import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.model.TocReference;

/**
 * Epub implementation of book-navigation facade
 * @author monkeycid
 *
 */
public class EpubNavigator implements BookNavigator {
	
	
	private EpubContentStream content;
	private HtmlRenderer renderer;
	private TocReference curSection;
	private int curPage;
	private RenderedPage curRender;
	
	public EpubNavigator(String bookFileName, File cacheDir, HtmlRenderer renderer) throws IOException{
		content = new EpubContentStream(bookFileName, cacheDir);
		this.renderer = renderer;
	}
	
	// TODO for all methods returning renderedPage - figure out whether they need to return 
	// a rendered page and how they should interact with the adapter. 
	
	public RenderedPage nextPage() throws IOException{
		if (renderer.isEndOfSource((curPage+1))){ 
			return nextSource();
		}
		curRender = renderer.getRenderedPage(++curPage);
		return curRender;
	}
	
	public RenderedPage prevPage() throws IOException{
		if (curPage <= 0){ 
			return prevSource(); 
		}
		curRender = renderer.getRenderedPage(--curPage); 
		return curRender;
	}
	
	public RenderedPage nextSource() throws IOException{
		if (!content.hasNextSource(curSection)){ // at end of book! 
			return curRender;
		}
		curPage = 0;
		curSection = content.getNextSource(curSection);
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		curRender = renderer.getRenderedPage(0);
		return curRender;
	}
	
	public RenderedPage prevSource() throws IOException{
		if (!content.hasPrevSource(curSection)){ // at start of book!
			return curRender; 
		}
		curPage = 0;
		curSection = content.getPreviousSource(curSection);
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		curRender = renderer.getRenderedPage(0);
		return curRender;
	}
	
	public RenderedPage toSection(TocReference section) throws IOException{
		curSection = section;
		renderer.setHtmlSource(curSection.getHtml(), curSection.getUniqueIdentifier());
		curPage = renderer.getPageNumber(curSection.getHtmlId());
		return nextPage(); // XXX due to renderer currently returning previous page!
		// TODO figure out a good way to distribute renders and page numbers - maybe send out renderEvents? 
	}
	
	public TableOfContents getToc(){
		return content.getToc();
	}
	
}
