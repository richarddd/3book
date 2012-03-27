package se.chalmers.threebook.html;

import android.graphics.Rect;

public class CharPosition {
	
	public Rect area;
	public char character;
	
	public CharPosition(int left, int top, int right, int bottom, char character) {
		this.area = new Rect(left, top, right, bottom);
		this.character = character;
	}
}
