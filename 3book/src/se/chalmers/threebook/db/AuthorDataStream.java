package se.chalmers.threebook.db;

import java.util.ArrayList;
import java.util.List;

import se.chalmers.threebook.model.Author;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AuthorDataStream {

//	private SQLiteDatabase database;
//	private DBHelper dbHelper;
//	private String[] allColumns = {
//			AuthorTable.COLUMN_ID,
//			AuthorTable.COLUMN_FIRSTNAME,
//			AuthorTable.COLUMN_LASTNAME
//		};
//
//	public AuthorDataStream(Context context) {
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
//	public Author persistAuthor(Author author) {
//
//		Cursor matchingAuthors = database.query(AuthorTable.TABLE_AUTHORS,
//				allColumns, AuthorTable.COLUMN_FIRSTNAME + " = ? AND "
//						+ AuthorTable.COLUMN_LASTNAME + " = ?",
//				new String[] { author.getFirstName(), author.getLastName() },
//				null, null, null);
//
//		// Check that author is not already in database.
//		if (matchingAuthors.getCount() > 0) {
//			matchingAuthors.moveToFirst();
//			return cursorToAuthor(matchingAuthors); // return author in database
//													// if existing.
//		} else {
//			ContentValues values = new ContentValues();
//			values.put(AuthorTable.COLUMN_FIRSTNAME, author.getFirstName());
//			values.put(AuthorTable.COLUMN_LASTNAME, author.getLastName());
//			long insertId = database.insert(AuthorTable.TABLE_AUTHORS, null,
//					values);
//
//			// return inserted Author
//			String[] cursorArgs = {};
//			Cursor cursor = database.query(AuthorTable.TABLE_AUTHORS,
//					allColumns, AuthorTable.COLUMN_ID + " = ?",
//					new String[]{String.valueOf(insertId)}, null, null, null);
//			cursor.moveToFirst();
//			Author newAuthor = cursorToAuthor(cursor);
//			return newAuthor;
//		}
//	}
//
//	public void deleteAuthor(Author author) {
//		long id = author.getId();
//		Log.i("AuthorDataStream",
//				"Deleting the author \"" + author.getFirstName() + " "
//						+ author.getLastName() + "\"");
//		database.delete(AuthorTable.TABLE_AUTHORS, AuthorTable.COLUMN_ID
//				+ " = " + author.getId(), null);
//	}
//
//	public List<Author> getAllAuthors() {
//		List<Author> authors = new ArrayList<Author>();
//
//		Cursor cursor = database.query(AuthorTable.TABLE_AUTHORS,
//				allColumns, null, null, null, null, null);
//
//		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//			authors.add(cursorToAuthor(cursor));
//		}
//
//		cursor.close();
//		return authors;
//	}
//
//	public Author getAuthor(long id) {
//		Cursor cursor = database.query(AuthorTable.TABLE_AUTHORS,
//				allColumns, AuthorTable.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null,
//				null, null);
//		cursor.moveToFirst();
//		return (cursor.getCount() > 0) ? cursorToAuthor(cursor) : null;
//	}
//
//	private Author cursorToAuthor(Cursor cursor) {
//		return new Author().setId(cursor.getLong(0))
//				.setFirstName(cursor.getString(1))
//				.setLastName(cursor.getString(2));
//	}
}
