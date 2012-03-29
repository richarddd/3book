package se.chalmers.threebook.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.model.Book;

public class EpubImporter implements Importer {

	private File path;

	private nl.siegmann.epublib.domain.Book epubBook;

	// String title
	// String author
	// Date published
	// Format format
	// File cover

	public EpubImporter() {
	}

	public void focusOn(File path) throws FileNotFoundException,
			IOException {
		this.path = path;
		
		epubBook = new EpubReader().readEpub(new FileInputStream(path));
	}

	public String readTitle() {
		return epubBook.getMetadata().getFirstTitle();
	}

	public Book createBook() {
		Book book = new Book();
		book.setTitle(readTitle());
		
		return book;
	}
}
