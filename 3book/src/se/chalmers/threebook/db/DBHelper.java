package se.chalmers.threebook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// http://www.vogella.de/articles/AndroidSQLite/article.html#databasetutorial_database
public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		BookTable.onCreate(db);
		AuthorTable.onCreate(db);
		BookAuthorsTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		BookTable.onUpgrade(db, oldVersion, newVersion);
		AuthorTable.onUpgrade(db, oldVersion, newVersion);
		BookAuthorsTable.onUpgrade(db, oldVersion, newVersion);
	}
}