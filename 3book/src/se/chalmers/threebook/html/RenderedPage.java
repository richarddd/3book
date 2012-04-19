package se.chalmers.threebook.html;

import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.view.View;

public class RenderedPage {
	
	private List<WordPosition> positionList;
	private Bitmap bitmap;
	private int nodePosition;
	
	private View imageContainer;
	private Map<Integer, RenderElement> specialObjectsMap;
	
	public RenderedPage(Bitmap bitmap, int nodePosition,
			List<WordPosition> positionList, Map<Integer, RenderElement> specialObjectsMap) {
		this.bitmap = bitmap;
		this.nodePosition = nodePosition;
		this.positionList = positionList;
		this.specialObjectsMap = specialObjectsMap;
	}
	
	public List<WordPosition> getPositionList() {
		return positionList;
	}
	public void setPositionList(List<WordPosition> positionList) {
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
	public Map<Integer, RenderElement> getSpecialObjectsMap(){
		return specialObjectsMap;
	}
}
