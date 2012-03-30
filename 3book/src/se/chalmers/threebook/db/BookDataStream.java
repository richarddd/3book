package se.chalmers.threebook.db;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.model.Author;
import se.chalmers.threebook.model.Book;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BookDataStream {
//	private SQLiteDatabase database;
//	private DBHelper dbHelper;
//	private String[] allColumns = { BookTable.COLUMN_ID,
//			BookTable.COLUMN_TITLE };
//	private Context context;
//
//	public BookDataStream(Context context) {
//		dbHelper = new DBHelper(context);
//	}
//
//	public void open() throws SQLException {
//		database = dbHelper.getWritableDatabase();
//	}
//
//	public void close() {
//		dbHelper.close();
//	}
//
//	public Book persistBook(Book book) {
//		//Persist simple values
//		ContentValues simpleValues = new ContentValues();
//		simpleValues.put(BookTable.COLUMN_TITLE, book.getTitle());
//		long newBookId = database.insert(BookTable.TABLE_BOOKS, null, simpleValues);
//		
//		//Persist authors
//		int i = 0;
//		for(Author author : book.getAuthors()) {
//			//Author table
//			AuthorDataStream ads = new AuthorDataStream(context);
//			ads.open();
//			Author a = ads.persistAuthor(author);
//			ads.close();
//			long newAuthorId = a.getId();
//			
//			//Book/author join table
//			ContentValues bookAuthorsValues = new ContentValues();
//			bookAuthorsValues.put(BookAuthorsTable.COLUMN_BOOK, newBookId);
//			bookAuthorsValues.put(BookAuthorsTable.COLUMN_AUTHOR, newAuthorId);
//			bookAuthorsValues.put(BookAuthorsTable.COLUMN_ORDER, i++);
//			database.insert(BookAuthorsTable.TABLE_BOOK_AUTHORS, null, bookAuthorsValues);
//		}
//		
//		//return persisted book
//		Cursor newBookCursor = database.query(BookTable.TABLE_BOOKS, allColumns,
//				BookTable.COLUMN_ID + " = ?", new String[]{String.valueOf(newBookId)}, null, null,
//				null);
//		newBookCursor.moveToFirst();
//		Book newBook = cursorToBook(newBookCursor);
//		newBookCursor.close();
//		return newBook;
//	}
//
//	public void deleteBook(Book book) {
//		long id = book.getId();
//		Log.i("BookDataStream", "Deleting the book \"" + book.getTitle() + "\"");
//		database.delete(BookTable.TABLE_BOOKS, BookTable.COLUMN_ID
//				+ " = " + book.getId(), null);
//	}
//
//	public List<Book> getAllBooks() {
//		List<Book> books = new ArrayList<Book>();
//
//		Cursor cursor = database.query(BookTable.TABLE_BOOKS, allColumns,
//				null, null, null, null, null);
//
//		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//			books.add(cursorToBook(cursor));
//		}
//		cursor.close();
//		return books;
//	}
//
//	private Book cursorToBook(Cursor cursor) {
//		long bookId = cursor.getLong(0);
//		
//		Book book = new Book()
//		.setId(bookId)
//		.setTitle(cursor.getString(1));
//		
//		String[] allColumns = {
//				BookAuthorsTable.COLUMN_BOOK,
//				BookAuthorsTable.COLUMN_AUTHOR
//		};
//		
//		Cursor bookAuthorCursor = database.query(
//				BookAuthorsTable.TABLE_BOOK_AUTHORS
//				, allColumns
//				, BookAuthorsTable.COLUMN_BOOK + " = ?"
//				, new String[]{String.valueOf(bookId)}
//				, null
//				, null
//				, BookAuthorsTable.COLUMN_ORDER);
//		
//		AuthorDataStream ads = new AuthorDataStream(context);
//		ads.open();
//		List<Author> authors = book.getAuthors();
//		for(bookAuthorCursor.moveToFirst(); !bookAuthorCursor.isAfterLast(); bookAuthorCursor.moveToNext()) {
//			long authorId = bookAuthorCursor.getLong(1);
//			Author author = ads.getAuthor(authorId);
//			
//			if(author != null) {
//				authors.add(author);
//			}
//		}
//		
//		ads.close();
//		return book;
//	}
}
