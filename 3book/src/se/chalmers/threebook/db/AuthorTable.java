package se.chalmers.threebook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AuthorTable {
	
	//"It is best practice to create a separate class per table." - Vogella
	public static final String TABLE_AUTHORS = "authors";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FIRSTNAME = "first_name";
	public static final String COLUMN_LASTNAME = "last_name";
	
	
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_AUTHORS
			+ "( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_FIRSTNAME + " text not null, "
			+ COLUMN_LASTNAME + " text not null);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTHORS);
		onCreate(db);
	}
}
