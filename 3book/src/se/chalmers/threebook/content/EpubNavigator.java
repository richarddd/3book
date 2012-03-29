package se.chalmers.threebook.content;

import se.chalmers.threebook.html.HtmlRenderer;
import se.chalmers.threebook.html.RenderedPage;

public class EpubNavigator { /*implements BookNavigator {
	
	private List<NavigationListener> listeners;
	private ContentStream stream;
	private HtmlRenderer renderer;
	private ChapterIdentifier curChap;
	private int curPage;
		
	public RenderedPage nextPage(){
		if (renderer.atEndOfSource()){
			renderer.setHtmlSource(stream.nextSource());
			curPage = -1; // will be set to 0 in return call 
		}
		return renderer.getRenderedPage(++curPage);
	}
	
	public RenderedPage previousPage(){
		if (renderer.atStartOfSource()){
			renderer.setHtmlSource(stream.prevSource());
			curPage = 1; // // will be set to 0 in return call 
		}
		return renderer.getRenderedPage(--curPage);
	}
	
	
	private void notifyChapterChange(ChapterIdentifier ident){
		for (NavigationListener l : navListeners){
			l.atNewChapter(ident.chapMetadata());
		}
	}
	
	public RenderedPage nextChapter(){
		
		
	}
	
	public RenderedPage prevChapter(){
		
	}
	
	public RenderedPage toChapter(ChapterIdentifier){
		
	}
	
	public RenderedPage toSection(SectionIdentifier){
		
	}
	
	// not sure this method makes sense!
	/* public RenderedPage toPage(int page){
		if (page < 0){
			throw new IllegalArgumentException("Target page must be > 0. page: " + page);
		}
		
		if (page == curPage){
			return;
		} else if 
		
		while(curPage != page )
	} */
	
	
	
}
