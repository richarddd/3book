package se.chalmers.threebook.content;

import java.io.IOException;

import se.chalmers.threebook.html.RenderedPage;
import se.chalmers.threebook.model.Book;
import se.chalmers.threebook.model.TocReference;

public interface BookNavigator {
	public RenderedPage nextPage() throws IOException;
	public RenderedPage prevPage() throws IOException;
	public RenderedPage bufferNextPage() throws IOException;
	public RenderedPage bufferPrevPage() throws IOException;
	public RenderedPage nextSource() throws IOException;
	public RenderedPage prevSource() throws IOException;
	public int toSection(TocReference section) throws IOException;
	public TableOfContents getToc();
	public Book getBook();
}
