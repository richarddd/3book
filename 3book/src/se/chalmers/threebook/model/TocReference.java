package se.chalmers.threebook.model;

import java.util.List;

public interface TocReference {
	List<TocReference> getChildren();
	String getTitle();
	String getId();
}
