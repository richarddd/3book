package se.chalmers.threebook.content;

import java.io.IOException;

import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.model.TocReference;

public interface BookNavigator {
	public RenderedPage nextPage() throws IOException;
	public RenderedPage prevPage() throws IOException;
	public RenderedPage nextSource() throws IOException;
	public RenderedPage prevSource() throws IOException;
	public RenderedPage toSection(TocReference section) throws IOException;
	public TableOfContents getToc();
}
