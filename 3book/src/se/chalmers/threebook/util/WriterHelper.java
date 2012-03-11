package se.chalmers.threebook.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * TODO: REFACTOR! It's likely best to make class non-static, it started out with a lot less stuff.  
 */
public class WriterHelper {
	
	private static void ensurecachefile(Activity a){
		if (appCacheLocation == null){
			appCacheLocation = a.getCacheDir();
		}
	}
	
	private static File appCacheLocation = null;
	private static File bookCacheLocation = null;
	private static Map<String, File> cache = new HashMap<String, File>();
	private static Map<String, File> imgCache = new HashMap<String, File>();
	
	private static boolean ensureBookCache(String bookName){
		if (bookCacheLocation == null){
			File bookCacheDir = new File (appCacheLocation, bookName);
			bookCacheLocation = bookCacheDir;
			return bookCacheDir.exists() ? true : bookCacheDir.mkdirs();
		} else {
			return true;
		}
		
	}
	
	public static boolean chapterCached(String bookName, String chapterName){
		String fileHash = ((Integer)(bookName+chapterName).hashCode()).toString(); // XXX this is retarded
		Log.d("3", "chapterCached returning :" + cache.containsKey(fileHash));
		return cache.containsKey(fileHash);
	}
	
	public static String getCachedFileName(String bookName, String chapterName){
		
		if (!chapterCached(bookName, chapterName)){
			throw new IllegalArgumentException("Trying to get uncached filename. Callers must ensure target is in cache by calling chapterCached before calling this. Also, refactor this fucking shit.");
		}
		String fileHash = ((Integer)(bookName+chapterName).hashCode()).toString(); // XXX this is retarded
		Log.d("3", "Returning fun cached file thingy");
		return cache.get(fileHash).getAbsolutePath();
	}
	
	public static boolean imageCached(String href){
		return imgCache.containsKey(href);
	}
	
	
	public static String writeFile(String data, String bookName, String chapterName) throws FileNotFoundException, IOException {
		chapterName +=".html"; // Append HTML to chapter name for proper filename shiznitz
		// TODO: Make sure multi-TOCEntry-in-file does not result in multiple cache entries!!!
		// Coz it's stupid!!!
		
		String fileHash = ((Integer)(bookName+chapterName).hashCode()).toString(); // XXX this is retarded 
		
		if (cache.containsKey(fileHash)){
			Log.d("3", "Found file in cache, returning. Key: " + fileHash);
			return cache.get(fileHash).getAbsolutePath();
		}
		Log.d("3", "File not found in cache. Key: " + fileHash);
		
		File cacheFile = null;
		BufferedWriter out = null;
		if (ensureBookCache(bookName)){
			try {
				Log.d("3", "bookCacheLocatioN: " + bookCacheLocation.getAbsolutePath());
				cacheFile = new File(bookCacheLocation, fileHash+".htm");
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));
				out.append(data);
				out.flush();
				cache.put(fileHash, cacheFile);
			} finally {
				if (out != null) { out.close(); }
			}
		} else {
			throw new IOException("Could not make / access cache dir :/ ");
		}
		
		return cacheFile.getAbsolutePath();
	}
	
	// TODO remove this ASAP
	public static String writeFile(String data, String bookName, String chapterName, Activity parent) 
			throws FileNotFoundException, IOException {
		ensurecachefile(parent); // TODO remove once refactored to set up params on start of application
		return writeFile(data, bookName, chapterName);
	}
	
	public static boolean writeImage(byte[] data, String bookName, String href) throws FileNotFoundException, IOException {
		if (imageCached(href)){
			return true;
		}
		
		File cacheFile = null;
		FileOutputStream out = null;
		if (ensureBookCache(bookName)){
			try {
				
				//File imageLocation = new File(bookCacheLocation, href);
				int separator = href.lastIndexOf("/");
				if (separator != -1){ // we need to make some dirs!
					String path = href.substring(0, separator);;
					boolean dirSuccess = new File(bookCacheLocation, path).mkdirs(); // TODO handle failure 'ere!
				}
				
				File imageLocation = new File(bookCacheLocation, href);
				out = new FileOutputStream(imageLocation, false);
				out.write(data);
				out.flush();
				cache.put(href, cacheFile);
			} catch (Exception e){
				Log.d("3", "Caught imageexception: " + e.getMessage());
				Log.d("3", "Trace: " + e.getStackTrace().toString());
				e.printStackTrace();
			}
			finally {
				if (out != null) { out.close(); }
			}
		} else {
			throw new IOException("Could not make / access cache dir :/ ");
		}

		return true;
	}
	
	// TODO remove this asap
	public static boolean writeImage(byte[] data, String bookName, String href, Activity parent) throws FileNotFoundException, IOException {
		ensurecachefile(parent);
		return writeImage(data, bookName, href);
	}
	
	
	
}
