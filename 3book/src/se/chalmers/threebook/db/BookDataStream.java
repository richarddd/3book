package se.chalmers.threebook.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BookDataStream {
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	
	private BookDataStream() {
		dbHelper = DBHelper.getInstance();
	}
	
	private static class BookDataStreamHolder {
		public static final BookDataStream instance = new BookDataStream();
	}
	
	public static BookDataStream getInstance() {
		return BookDataStreamHolder.instance;
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
}
