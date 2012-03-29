package se.chalmers.threebook.db;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.model.Book;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BookDataStream {
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private String[] allColumns = {
				DBHelper.TABLE_BOOKS_ID,
				DBHelper.TABLE_BOOKS_TITLE
			};

	public BookDataStream(Context context) {
		dbHelper = new DBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Book persistBook(Book book) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.TABLE_BOOKS_TITLE, book.getTitle());
		long insertId = database.insert(DBHelper.TABLE_BOOKS, null, values);
		Cursor cursor = database.query(DBHelper.TABLE_BOOKS, allColumns,
				DBHelper.TABLE_BOOKS_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		Book newBook = cursorToBook(cursor);
		cursor.close();
		return newBook;
	}

	public void deleteBook(Book book) {
		long id = book.getId();
		Log.i("BookDataStream", "Deleting the book \"" + book.getTitle() + "\"");
		database.delete(DBHelper.TABLE_BOOKS, DBHelper.TABLE_BOOKS_ID + " = "
				+ book.getId(), null);
	}

	public List<Book> getAllBooks() {
		List<Book> books = new ArrayList<Book>();

		Cursor cursor = database.query(DBHelper.TABLE_BOOKS, allColumns, null,
				null, null, null, null);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()) {
			books.add(cursorToBook(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return books;
	}

	private Book cursorToBook(Cursor cursor) {
		Book book = new Book();
		book.setId(cursor.getLong(0));
		book.setTitle(cursor.getString(1));
		return book;
	}
}
