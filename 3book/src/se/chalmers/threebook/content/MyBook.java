package se.chalmers.threebook.content;

import nl.siegmann.epublib.domain.Book;

/**
 * Shitty class just to bypass transaction limit
 * for testing purposes only
 * probably never a good idea. 
 */
public class MyBook {
	private static final MyBook myBook = new MyBook(); //XXX t�nk p� att denna lever genom hela appen axel �ven fast dess skapar d�das
	private Book book = null;
	
	private MyBook(){}
	
	public static void setBook(Book book){get().book = book;}
	public static MyBook get(){return myBook;}
	
	public Book book(){
		return book;
	}
}
