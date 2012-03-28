package se.chalmers.threebook.html;

import java.util.List;

import android.graphics.Bitmap;

public class RenderedPage {
	
	private List<CharPosition> positionList;
	private Bitmap bitmap;
	private int nodePosition;
	
	public RenderedPage(Bitmap bitmap, int nodePosition,
			List<CharPosition> positionList) {
		this.bitmap = bitmap;
		this.nodePosition = nodePosition;
		this.positionList = positionList;
	}
	public List<CharPosition> getPositionList() {
		return positionList;
	}
	public void setPositionList(List<CharPosition> positionList) {
		this.positionList = positionList;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public int getNodePosition() {
		return nodePosition;
	}
	public void setNodePosition(int nodePosition) {
		this.nodePosition = nodePosition;
	}
}
