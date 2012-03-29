package se.chalmers.threebook.content;

import se.chalmers.threebook.html.RenderedPage;

public interface BookNavigator {
	public RenderedPage nextPage();
	public RenderedPage previousPage();
	public RenderedPage toPage(int page);
	public RenderedPage nextChapter();
}
