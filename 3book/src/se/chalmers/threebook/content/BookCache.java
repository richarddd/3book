package se.chalmers.threebook.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface BookCache {
	
	
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
	 * Returns a File pointing to the desired item
	 * 
	 * @throws IllegalArgumentException if file is not in cache
	 */
	public File retrieve(String itemIdentifier);
	
	public boolean exists(String itemIdentifier);
}
