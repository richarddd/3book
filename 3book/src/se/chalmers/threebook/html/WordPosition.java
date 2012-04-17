package se.chalmers.threebook.html;

import android.graphics.Rect;

public class WordPosition {
	
	public Rect area;
	public String word;
	
	public WordPosition(float left, float top, float right, float bottom, String word) {
		this.area = new Rect((int)left, (int)top, (int)right, (int)bottom);
		this.word = word;
	}
}
