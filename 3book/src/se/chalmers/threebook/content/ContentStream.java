package se.chalmers.threebook.content;

import java.io.IOException;
import java.util.List;

import se.chalmers.threebook.model.Bookmark;

public interface ContentStream {
	public String next() throws IOException;
	public String previous() throws IOException;
	public String jumpTo(Position position) throws IOException;
	public String jumpTo(int index) throws IOException;
	public List<String> getToc();
	public List<Bookmark> getBookmarks();
}
