package se.chalmers.threebook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BookAuthorsTable {
	
	public static final String TABLE_BOOK_AUTHORS = "book_authors";
	public static final String COLUMN_BOOK = "book";
	public static final String COLUMN_AUTHOR = "author";
	public static final String COLUMN_ORDER = "_order";
	
	
//	"CREATE TABLE book_authors(
//		book integer,
//		author integer,
//		_order integer,
//		PRIMARY KEY (book, author),
//		FOREIGN KEY (book) REFERENCES books(_id),
//		FOREIGN KEY (author) REFERENCES authors(_id)
//	);
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_BOOK_AUTHORS
			+ "( "
			+ COLUMN_BOOK + " integer, "
			+ COLUMN_AUTHOR + " integer, "
			+ COLUMN_ORDER + " integer, "
			+ "PRIMARY KEY (" + COLUMN_BOOK + ", " + COLUMN_AUTHOR + "), "
			+ "FOREIGN KEY (" + COLUMN_BOOK + ") REFERENCES " + BookTable.TABLE_BOOKS + "(" + BookTable.COLUMN_ID + "), "
			+ "FOREIGN KEY (" + COLUMN_AUTHOR + ") REFERENCES " + AuthorTable.TABLE_AUTHORS + "(" + AuthorTable.COLUMN_ID + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(BookAuthorsTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK_AUTHORS);
		onCreate(db);
	}
	
}
