package se.chalmers.threebook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// http://www.vogella.de/articles/AndroidSQLite/article.html#databasetutorial_database
public class DBHelper extends SQLiteOpenHelper {

	private static Context context;
	private static DBHelper self;

	private static final String DATABASE_NAME = "3book";
	private static final int DATABASE_VERSION = 0;

	private static final String TABLE_BOOKS = "books";
	private static final String COLUMN_TITLE = "title";

	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_BOOKS
			+ "( " + COLUMN_TITLE + "text primary key);"; // TODO: This needs to
															// be updated when
															// we know more
															// about the
															// database
															// structure.

	private DBHelper() {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void init(Context context) {
		DBHelper.context = context;
		DBHelper.self = new DBHelper();
	}

	public static DBHelper getInstance() {
		// TODO: Possibly, we should throw an exception here if self == null
		return self;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS" + TABLE_BOOKS);
		onCreate(db);
	}
}