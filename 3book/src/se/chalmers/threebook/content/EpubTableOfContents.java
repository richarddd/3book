package se.chalmers.threebook.content;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.model.EpubTocReference;
import se.chalmers.threebook.model.TocReference;

public class EpubTableOfContents implements TableOfContents{
	private List<TocReference> tocReferences;

	private int size;
	private List<TocReference> linearToc = null;
	
	public EpubTableOfContents(nl.siegmann.epublib.domain.TableOfContents toc, BookCache cache){
		size = toc.size();
		tocReferences = new ArrayList<TocReference>(toc.getTocReferences().size());
		for (nl.siegmann.epublib.domain.TOCReference ref : toc.getTocReferences()){
			tocReferences.add(new EpubTocReference(ref, cache));
		}
	}
	
	public int size() {
		return size;
	}

	public List<TocReference> getTocReferences() {
		return tocReferences;
	}
	
	public List<TocReference> getLinearToc(){
		if (linearToc == null){
			/* Recursively build a linear TOC */
			linearToc = new ArrayList<TocReference>(size);
			buildLinearTocFrom(tocReferences);
		}
		
		return linearToc;
	}
	
	private void buildLinearTocFrom(List<TocReference> refs){
		for (TocReference r: refs){
			linearToc.add(r);
			
			// This is a little silly, rebuild with for-each later when less tired TODO
			if (r.getChildren() != null && r.getChildren().size() > 0){
				buildLinearTocFrom(r.getChildren());
			}
		}
	}
	
}
