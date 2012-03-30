package se.chalmers.threebook.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BookTable {

	public static final String TABLE_BOOKS = "books";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_FORMAT = "format";
	public static final String COLUMN_POSITION = "_position";
	public static final String COLUMN_AUTHOR = "author";
	public static final String COLUMN_SOURCE = "_source";
	public static final String COLUMN_LASTREAD = "lastRead";
	public static final String COLUMN_RATING = "rating";
	public static final String COLUMN_COVER = "cover";

	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_BOOKS
			+ "( " + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_TITLE + " text not null);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
		onCreate(db);
	}
}
