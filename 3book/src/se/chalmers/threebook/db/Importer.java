package se.chalmers.threebook.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import se.chalmers.threebook.model.Author;
import se.chalmers.threebook.model.Book;

public interface Importer {
		
		public abstract void focusOn(File path) throws FileNotFoundException, IOException;
		
		public abstract String readTitle();
		
		public abstract List<Author> readAuthors();
		
		public abstract Book createBook();

}
