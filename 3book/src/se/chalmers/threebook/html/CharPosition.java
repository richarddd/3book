package se.chalmers.threebook.html;

import android.graphics.Rect;

public class CharPosition {
	
	public Rect area;
	public char character;
	
	public CharPosition(float left, float top, float right, float bottom, char character) {
		this.area = new Rect((int)left, (int)top, (int)right, (int)bottom);
		this.character = character;
	}
}
