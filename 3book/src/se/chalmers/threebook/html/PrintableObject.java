package se.chalmers.threebook.html;

import java.util.EnumSet;
import java.util.Set;

import org.jsoup.nodes.Element;

public class PrintableObject {

	private Element element;
	private Set<StyleFlag> flags;

	public static PrintableObject makeRoot(Element element) {
		return new PrintableObject(element);
	}

	private PrintableObject(Element element) {
		this.flags = EnumSet.noneOf(StyleFlag.class);
		this.element = element;
	}

	public PrintableObject(Element element, StyleFlag newFlag,
			Set<StyleFlag> oldFlags) {
		this.element = element;
		this.flags = EnumSet.copyOf(oldFlags);
		this.flags.add(newFlag);
	}

	public Element getElement() {
		return element;
	}

	public Set<StyleFlag> getFlags() {
		return flags;
	}

	/*
	 * 
	 * private PrintableObject parent = null; private List<PrintableObject>
	 * children = new LinkedList<PrintableObject>(); private String text;
	 * private Set<StyleFlag> flags; private int pos;
	 * 
	 * public PrintableObject(){
	 * 
	 * }
	 * 
	 * public PrintableObject(PrintableObject parent, List<PrintableObject>
	 * children, String text, Set<StyleFlag> flags, int pos) { super();
	 * this.parent = parent; this.children = children; this.text = text;
	 * if(isRoot()){ this.flags = EnumSet.copyOf(flags); }else{ this.flags =
	 * EnumSet.copyOf(parent.getFlags()); this.flags.addAll(flags); } this.pos =
	 * pos; }
	 * 
	 * public boolean isRoot(){ return parent == null; }
	 * 
	 * public boolean isLeaf() { return children.size() == 0; }
	 * 
	 * public String getText(){ return this.text; }
	 * 
	 * public Set<StyleFlag> getFlags(){ return this.flags; }
	 * 
	 * public void setChildren(List<PrintableObject> children){ this.children =
	 * children; }
	 */
}
