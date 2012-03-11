package se.chalmers.threebook.content.epub;

import java.io.File;
import java.util.Map;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

/**
 * Analyzes epub files to figure out the correct cache mapping
 *
 */
public class EpubResourceIndex {

	
	private Map<Resource, File> tocIndexToFile;
	
	public EpubResourceIndex(Book book){
		for (TOCReference r : book.getTableOfContents().getTocReferences()){
			//r.getResource().ge
		}
	}
	
	private void createFileIndex(TOCReference r){
		
	}
	
	public File getFileByTocIndex(int index){
		return tocIndexToFile.get(index);
	}
}
