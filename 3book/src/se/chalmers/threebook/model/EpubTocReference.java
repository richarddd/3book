package se.chalmers.threebook.model;

import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.TOCReference;

public class EpubTocReference implements TocReference {

	private TOCReference ref;
	private List<TocReference> children = new ArrayList<TocReference>(0);
	
	public EpubTocReference(TOCReference ref){
		this.ref = ref;
		if (ref.getChildren().size() > 0){
			children = new ArrayList<TocReference>(ref.getChildren().size());
			for (TOCReference cr : ref.getChildren()){
				children.add(new EpubTocReference(cr));
			}
		}
		
	}
	
	public List<TocReference> getChildren() {
		return children;
	}

	public String getTitle() {
		return ref.getTitle();
	}

	public String getId() {
		return ref.getFragmentId();
	}
	
}
