package se.chalmers.threebook.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface BookCache {
	
	public void cacheImage(String imageName) throws IOException;
	
	/**
	 * Stores a stream of bytes on disk accessible by itemIdentifier
	 * 
	 * If the item already exists in cache the method simply returns - 
	 * overwrite is currently not supported.
	 * 
	 * @param data the data to write
	 * @param itemIdentifier the identifier for the data
	 * @throws FileNotFoundException If the file cannot be written
	 * @throws IOException If file cannot be written to
	 */
	public File cache (byte[] data, String itemIdentifier) throws FileNotFoundException, IOException;;
	
	/**
	 * Stores a String on disk accessible by itemIdentifier
	 * 
	 * If the item already exists in cache the method simply returns - 
	 * overwrite is currently not supported.
	 * 
	 * @param words the string to write
	 * @param itemIdentifier the identifier for the string
	 * @throws FileNotFoundException If the file cannot be written
	 * @throws IOException If file cannot be written to
	 */
	public File cache (String words,String itemIdentifier) throws FileNotFoundException, IOException;
	
	/**
	 * Returns the String contents of the desired item
	 * @throws IOException 
	 * 
	 * @throws IllegalArgumentException if file is not in cache
	 */
	public String retrieve(String itemIdentifier) throws IOException;
	
	public boolean exists(String itemIdentifier);
}
