package se.chalmers.threebook.contentprovider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import se.chalmers.threebook.db.AuthorTable;
import se.chalmers.threebook.db.BookAuthorsTable;
import se.chalmers.threebook.db.BookTable;
import se.chalmers.threebook.db.DBHelper;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ThreeBookContentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "3book.db";
	private static final int DATABASE_VERSION = 1;

	private DBHelper database;

	private static final int BOOKS = 10;
	private static final int BOOK_ID = 20;
	private static final int AUTHORS = 30;
	private static final int AUTHORS_ID = 40;
	private static final int BOOK_AUTHORS = 50;

	private static final String AUTHORITY = "se.chalmers.threebook.contentprovider";

	private static final String BOOK_PATH = "books";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BOOK_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/threebook";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/threebook";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, BOOK_PATH, BOOKS);
		sURIMatcher.addURI(AUTHORITY, BOOK_PATH + "/#", BOOK_ID);
		sURIMatcher.addURI(AUTHORITY, BOOK_PATH, AUTHORS);
		sURIMatcher.addURI(AUTHORITY, BOOK_PATH + "/#", AUTHORS_ID);
		sURIMatcher.addURI(AUTHORITY, BOOK_PATH, BOOK_AUTHORS);
	}

	@Override
	public synchronized boolean onCreate() {
		database = new DBHelper(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return database != null; // true if successful
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		checkColumns(projection);

		queryBuilder.setTables(BookTable.TABLE_BOOKS);

		int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case BOOKS:
			break;
		case BOOK_ID:
			queryBuilder.appendWhere(BookTable.COLUMN_ID + " = "
					+ uri.getLastPathSegment());
			break;
		case AUTHORS:
			break;
		case AUTHORS_ID:
			queryBuilder.appendWhere(AuthorTable.COLUMN_ID + " = "
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		long id = 0;

		switch (uriType) {
		case BOOKS:
			id = db.insert(BookTable.TABLE_BOOKS, null, values);
			break;
		case AUTHORS:
			db.beginTransaction();

			// Check that author is not in database already.
			Cursor c = db.query(
					AuthorTable.TABLE_AUTHORS,
					new String[] { AuthorTable.COLUMN_ID },
					AuthorTable.COLUMN_FIRSTNAME + " = ? AND "
							+ AuthorTable.COLUMN_LASTNAME + " = ?",
					new String[] {
							values.getAsString(AuthorTable.COLUMN_FIRSTNAME),
							values.getAsString(AuthorTable.COLUMN_LASTNAME) },
					null, null, null);

			if (c.getCount() > 0) {
				c.moveToFirst();
				id = c.getLong(c.getColumnIndexOrThrow(AuthorTable.COLUMN_ID)); // found
																				// author
			} else {
				id = db.insert(AuthorTable.TABLE_AUTHORS, null, values); // Add
																			// new
																			// author
			}

			c.close();

			Long bookId = values.getAsLong(BookAuthorsTable.COLUMN_BOOK);

			// Link author to book
			ContentValues joinValues = new ContentValues();
			joinValues.put(BookAuthorsTable.COLUMN_BOOK, bookId);
			joinValues.put(BookAuthorsTable.COLUMN_AUTHOR, id);
			db.insert(BookAuthorsTable.TABLE_BOOK_AUTHORS, null, joinValues);

			db.endTransaction();

			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public synchronized int delete(Uri uri, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsDeleted = 0;

		switch (uriType) {
		case BOOKS:
			rowsDeleted = db.delete(BookTable.TABLE_BOOKS, selection,
					selectionArgs);
			break;
		case BOOK_ID:
			String id = uri.getLastPathSegment();

			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(BookTable.TABLE_BOOKS,
						BookTable.COLUMN_ID + "=?", new String[] { id });
			} else {
				// Append selectionArgs to id, since we want to use tokens in
				// the query.
				List<String> args = new LinkedList<String>();
				args.add(id);
				args.addAll(Arrays.asList(selectionArgs));
				String[] sArgs = (String[]) args.toArray();

				rowsDeleted = db.delete(BookTable.TABLE_BOOKS,
						BookTable.COLUMN_ID + "=? and " + selection, sArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values,
			String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsUpdated = 0;

		switch (uriType) {
		case BOOKS:
			rowsUpdated = db.update(BookTable.TABLE_BOOKS, values, selection,
					selectionArgs);
			break;
		case BOOK_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(BookTable.TABLE_BOOKS, values,
						BookTable.COLUMN_ID + "=?", new String[] { id });
			} else {
				// Append selectionArgs to id, since we want to use tokens in
				// the query.
				List<String> args = new LinkedList<String>();
				args.add(id);
				args.addAll(Arrays.asList(selectionArgs));
				String[] sArgs = (String[]) args.toArray();

				rowsUpdated = db.update(BookTable.TABLE_BOOKS, values,
						BookTable.COLUMN_ID + "=? and " + selection, sArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public synchronized String getType(Uri uri) {
		throw new UnsupportedOperationException();
	}

	private synchronized void checkColumns(String[] projection) {
		String[] available = { BookTable.COLUMN_ID, BookTable.COLUMN_TITLE };

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
