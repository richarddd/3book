package se.chalmers.threebook.content;

import java.util.List;

import se.chalmers.threebook.model.TocReference;

public interface TableOfContents {
	public int size();
	public List<TocReference> getTocReferences();
	public List<TocReference> getLinearToc();
	public TocReference getSection(int index);
}
