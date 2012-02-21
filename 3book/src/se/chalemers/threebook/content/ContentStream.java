package se.chalemers.threebook.content;

import java.util.List;

import se.chalmers.threebook.model.Bookmark;

public interface ContentStream {
	public String next();
	public String previous();
	public String jumpTo(Position position);
	public List<String> getTOC();
	public List<Bookmark> getBookmarks();
}
