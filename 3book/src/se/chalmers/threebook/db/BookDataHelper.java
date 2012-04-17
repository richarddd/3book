package se.chalmers.threebook.db;

import java.util.ArrayList;
import java.util.List;
import se.chalmers.threebook.contentprovider.ThreeBookContentProvider;
import se.chalmers.threebook.model.Author;
import se.chalmers.threebook.model.Book;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BookDataHelper {
	
	public static List<Book> getBooks(Context context) {
		return getBooks(context, null, null, null, null);
	}
	
	public static Book getBook(Context context, Long id) {
		List<Book> books = getBooks(context, null, BookTable.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null);
		return (books.size() > 0) ? books.get(0) : null;
	}	
	
	private static List<Book> getBooks(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor bookCursor = context.getContentResolver().query(ThreeBookContentProvider.BOOK_URI, projection, selection, selectionArgs, sortOrder);
		List<Book> books = new ArrayList<Book>(bookCursor.getCount());
		
		if (bookCursor != null) {
			for (bookCursor.moveToFirst(); !bookCursor.isAfterLast(); bookCursor.moveToNext()) {
				Book book = new Book()
					.setId(bookCursor.getLong(bookCursor.getColumnIndex(BookTable.COLUMN_ID)))
					.setTitle(bookCursor.getString(bookCursor.getColumnIndexOrThrow(BookTable.COLUMN_TITLE)))
					.setSource(bookCursor.getString(bookCursor.getColumnIndexOrThrow(BookTable.COLUMN_SOURCE)));
				
				Cursor authorCursor = context.getContentResolver().query(Uri.withAppendedPath(ThreeBookContentProvider.BOOK_AUTHORS_URI, String.valueOf(book.getId())), null, null, null, null);
				List<Author> authors = book.getAuthors();
				
				for(authorCursor.moveToFirst(); !authorCursor.isAfterLast(); authorCursor.moveToNext()) {
					Author author = new Author()
						.setId(authorCursor.getLong(authorCursor.getColumnIndex(AuthorTable.COLUMN_ID)))
						.setFirstName(authorCursor.getString(authorCursor.getColumnIndex(AuthorTable.COLUMN_FIRSTNAME)))
						.setLastName(authorCursor.getString(authorCursor.getColumnIndex(AuthorTable.COLUMN_LASTNAME)));
					authors.add(author);
				}
			}
		}
		return books;
	}
}
