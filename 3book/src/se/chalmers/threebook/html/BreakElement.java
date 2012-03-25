package se.chalmers.threebook.html;

public class BreakElement extends RenderElement {
	
	private int span;

	public BreakElement(float span) {
		super();
		this.span = (int)span;
	}

	public int getSpan() {
		return span;
	}

	public void setSpan(int span) {
		this.span = span;
	}
}
