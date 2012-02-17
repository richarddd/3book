package parser.epub;

import java.util.ArrayList;
import java.util.List;

import parser.ThreeBook;

import nl.siegmann.epublib.domain.Book;

public class EpubBook implements ThreeBook {
	Book book; // The underlying epublib book
	List<EpubChapter> chapters;
	
	public EpubBook(Book epub){
		book = epub;
		chapters = new ArrayList<EpubChapter>(book.getSpine().size()+1);
	}
	
	
}
