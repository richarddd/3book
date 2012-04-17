package se.chalmers.threebook.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.util.Log;

public class Position implements Serializable {
	private static final long serialVersionUID = 8729783581635536152L;
	private String resourcePath;
	private int currentNode;
	
	public String getResourcePath() {
		return resourcePath;
	}
	public Position setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}
	public int getCurrentNode() {
		return currentNode;
	}
	public Position setCurrentNode(int currentNode) {
		this.currentNode = currentNode;
		return this;
	}
	
	public byte[] getBlob() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();

		return baos.toByteArray();
	}
	
	/*
	 * TODO: Perhaps this should just return null if conversion is not possible...
	 */
	public static Position fromBlob(byte[] blob) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			ObjectInputStream oos = new ObjectInputStream(bais);

			Object o = oos.readObject();

			if (o instanceof Position) {
				return (Position) o;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e("Position", "Couldn't convert blob to Position.");
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "X marks the spot";
	}
}
