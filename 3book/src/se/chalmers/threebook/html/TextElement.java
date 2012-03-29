package se.chalmers.threebook.html;

public class TextElement extends RenderElement {
	
	private String text;
	private StyleFlag style;
	
	public TextElement(String text, StyleFlag style){
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
