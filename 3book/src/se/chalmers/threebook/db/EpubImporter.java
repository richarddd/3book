package se.chalmers.threebook.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import nl.siegmann.epublib.epub.EpubReader;
import se.chalmers.threebook.model.Author;
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
	
	public List<Author> readAuthors() {
		List<nl.siegmann.epublib.domain.Author> epubAuthors = epubBook.getMetadata().getAuthors();
		
		List<Author> authors = new LinkedList<Author>();
		
		for(nl.siegmann.epublib.domain.Author a : epubAuthors) {
			authors.add(new Author()
				.setFirstName(a.getLastname().replaceAll(",", ""))
				.setLastName(a.getFirstname().replaceAll(",", ""))
			);
		}
		
		
		return authors;
	}

	public Book createBook() {
		Book book = new Book()
			.setTitle(readTitle())
			.setSource(path.getPath());
		
		List<Author> authors = book.getAuthors();
		for(Author a : readAuthors()) {
			authors.add(a);
		}
		
		return book;
	}
}
