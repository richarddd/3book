package se.chalmers.threebook.content;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for epub files unzipped and parsed during the reader lifecycle
 */
public class EpubCache implements BookCache {

	
	private static Map<String, File> cache = new HashMap<String, File>();
	
	private String bookName;
	private File bookDir;
	
	/**
	 * Creates a new cache for the book indicated by the unique identifier bookIdentifier 
	 * @param bookIdentifier a string identifier for a book that must be unique 
	 * @param cacheDir		the application's cache directory
	 * @throws IOException	when directories cannot be created in the cache dir
	 */
	public EpubCache(String bookIdentifier, File cacheDir) throws IOException{
		bookName = bookIdentifier;
		//this.cacheDir = cacheDir;
		bookDir = new File(cacheDir, bookIdentifier);
		boolean dirs = bookDir.mkdirs();
		if (!dirs){
			throw new IOException("Could not ensure presence of book cache dir: " + bookDir.getAbsolutePath());
		}
	}
	
	//@Override
	public File cache(byte[] data, String identifier) throws FileNotFoundException, IOException {
		if (exists(identifier)){
			return cache.get(identifier);
		}
		
		FileOutputStream out = null;
		File cacheFile = null;
		try {
			cacheFile = new File(bookDir, identifier);
			out = new FileOutputStream(cacheFile, false);
			out.write(data);
			out.flush();
			cache.put(identifier, cacheFile);
		} finally {
			if (out != null){out.close();}
		}
		
		return cacheFile;
		
	}

	//@Override
	public File cache(String words, String identifier) throws IOException {
		if (exists(identifier)){
			return cache.get(identifier);
		}
		BufferedWriter out = null;
		File cacheFile = null; 
		try {
			cacheFile = new File(bookDir, identifier);
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));
			out.append(words);
			out.flush();
			cache.put(identifier, cacheFile);
		} finally {
			if (out != null){out.close();}
		}
		return cacheFile;
	}

	//@Override
	public File retrieve(String identifier) {
		if (!exists(identifier)){
			throw new IllegalArgumentException("File with identifier " + identifier + " does not exist in cache for " + bookName + ".");
		}
		return cache.get(identifier);
	}

	public boolean exists(String itemIdentifier) {
		return cache.containsKey(itemIdentifier);
	}

}
