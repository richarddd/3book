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

public class WriterHelper {
	
	private static Map<String, File> cache = new HashMap<String, File>();

	/**
	 * 
	 * @param data
	 * @param hash
	 * @param parent
	 * @return the filename of the new file
	 */
	public static String writeFile(String data, String bookName, String chapterName, Activity parent) 
			throws FileNotFoundException, IOException {
		chapterName +=".html"; // Append HTML to chapter name for proper filename shiznitz
		// TODO: Make sure multi-TOCEntry-in-file does not result in multiple cache entries!!!
		// Coz it's stupid!!!
		
		String fileHash = ((Integer)(bookName+chapterName).hashCode()).toString(); // XXX this is retarded 
		
		if (cache.containsKey(fileHash)){
			Log.d("3", "Found file in cache, returning. Key: " + fileHash);
			return cache.get(fileHash).getAbsolutePath();
		}
		Log.d("3", "File not found in cache. Key: " + fileHash);
		
		File androidCache = parent.getCacheDir();
		File bookCacheDir = new File (androidCache, bookName);
		
		boolean dirExists = bookCacheDir.exists() ? true : bookCacheDir.mkdir();
		
		File cacheFile = null;
		if (dirExists){
			cacheFile = new File(bookCacheDir, fileHash+".htm");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));
			out.append(data);
			out.flush();
			out.close();
			cache.put(fileHash, cacheFile);
		} else {
			throw new IOException("Could not make / access cache dir :/ ");
		}
		
		return cacheFile.getAbsolutePath();
	}
	
	/**
	 * 
	 * @param bookName
	 * @param chapterName
	 * @param parent
	 * @return
	 */
	public static BufferedReader getReaderForFile(String bookName, String chapterName, Activity parent) 
			throws FileNotFoundException, IOException {
		String fileName = Constants.TEMP_BOOK_STORAGE_BASE_PATH.value()+bookName+"/"+chapterName;
		FileInputStream fis = parent.openFileInput(fileName);
		return new BufferedReader(new InputStreamReader(fis, Charset.defaultCharset()));
	}
	
}
