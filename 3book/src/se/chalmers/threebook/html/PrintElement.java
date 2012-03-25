package se.chalmers.threebook.html;

public class PrintElement extends RenderElement {
	
	private String text;
	private StyleFlag style;
	
	public PrintElement(String text, StyleFlag style) {
		super();
		this.text = text;
		this.style = style;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public StyleFlag getStyle() {
		return style;
	}
}
