package se.chalmers.threebook.content;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;

/**
 * Cache for epub files unzipped and parsed during the reader lifecycle
 * 
 * TODO: add check for files already on disk, retrieve from disk if so 
 * (e.g. utilze cache cross-session persistence)
 * 
 *  TODO: decouple this from Book and Resource by being more clever upstream.
 */
public class EpubCache implements BookCache {

	
	private static Map<String, File> cache = new HashMap<String, File>();
	private static Map<String, Integer> fullSize = new HashMap<String, Integer>();
	
	private String bookName;
	private File bookDir;
	private Book book; // XXX keeping this for the images, not liking it a lot.
	
	/**
	 * Creates a new cache for the book indicated by the unique identifier bookIdentifier 
	 * @param bookIdentifier a string identifier for a book that must be unique 
	 * @param cacheDir		the application's cache directory
	 * @param book			the book we're working with (for images)
	 * @throws IOException	when directories cannot be created in the cache dir
	 */
	public EpubCache(String bookIdentifier, File cacheDir, Book book) throws IOException{
		bookName = bookIdentifier;
		//this.cacheDir = cacheDir;
		bookDir = new File(cacheDir, bookIdentifier);
		bookDir.mkdirs();
		if (!bookDir.exists()){
			throw new IOException("Could not ensure presence of book cache dir: " + bookDir.getAbsolutePath());
		}
	}
	
	public void cacheImage(String imageHref) throws IOException {
		Resource r = book.getResources().getByHref(imageHref);
		cache(r.getData(), imageHref);
	}
	
	//@Override
	public File cache(byte[] data, String identifier) throws FileNotFoundException, IOException {
		if (exists(identifier)){
			return cache.get(identifier);
		}
		
		FileOutputStream out = null;
		File cacheFile = null;
		try {
			
			// XXX TODO remove one Daniel fixes the HTML parser to hash image names
			//File imageLocation = new File(bookCacheLocation, href);
			int separator = identifier.lastIndexOf("/");
			if (separator != -1){ // we need to make some dirs!
				String path = identifier.substring(0, separator);;
				boolean dirSuccess = new File(bookDir, path).mkdirs(); // TODO handle failure 'ere!
			}
			
			cacheFile = new File(bookDir, identifier);
			
			// END xxx todo
			
			
			//cacheFile = new File(bookDir, identifier);// TODO change this back!
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
	public String retrieve(String identifier) throws IOException{
		return retrieve(identifier, -1);
	}
	
	public String retrieve(String identifier, int bytes) throws IOException {
		if (!exists(identifier)){
			throw new IllegalArgumentException("File with identifier " + identifier + " does not exist in cache for " + bookName + ".");
		}
		
		//return cache.get(identifier);
		String text;
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new FileReader(cache.get(identifier)));
			
			if (!fullSize.containsKey(identifier) && bytes < 1){
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = buf.readLine()) != null){
					sb.append(line);
				}
				text = sb.toString();
				fullSize.put(identifier, sb.length()); // store length for faster retrieval later
			} else {
				char[] cb = new char[bytes > 0 ? bytes : fullSize.get(identifier)];
				buf.read(cb);
				text = String.copyValueOf(cb);
			}
		
		} finally {
			if (buf != null){buf.close();}
		}
		
		return text;
		
	}

	
	public boolean exists(String itemIdentifier) {
		return cache.containsKey(itemIdentifier);
	}

}
