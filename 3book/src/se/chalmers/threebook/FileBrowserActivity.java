package se.chalmers.threebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.chalmers.threebook.adapters.FileBrowserAdapter;
import se.chalmers.threebook.contentprovider.ThreeBookContentProvider;
import se.chalmers.threebook.db.AuthorTable;
import se.chalmers.threebook.db.BookDataHelper;
import se.chalmers.threebook.db.BookTable;
import se.chalmers.threebook.db.EpubImporter;
import se.chalmers.threebook.db.Importer;
import se.chalmers.threebook.model.Author;
import se.chalmers.threebook.model.Book;
import se.chalmers.threebook.model.Position;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.util.Helper;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowserActivity extends ActionBarActivity {

    private TextView txtCurrDir;
    private ListView lstFiles;
    private FileBrowserAdapter adapter;
    private String currentPath;

    private List<File> folders = new ArrayList<File>();
    private List<File> files = new ArrayList<File>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_file_browser);

	/* getActionBarHelper().set */

	if (Helper.SupportsNewApi()) {
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	}

	txtCurrDir = (TextView) findViewById(R.id.txt_file_path);
	lstFiles = (ListView) findViewById(R.id.lst_book_browser);

	// lstFiles.setDrawingCacheBackgroundColor(getResources().getColor(R.color.default_background));
	lstFiles.setCacheColorHint(getResources().getColor(
		R.color.default_background));

	lstFiles.setOnItemClickListener(new OnItemClickListener() {

	    private String[] openChoiseItems;

		public void onItemClick(AdapterView<?> parent, View view,
		     final int position, long id) {
		if (adapter.getItem(position).isDirectory()) {
		    try {
			browseTo(adapter.getItem(position));
		    } catch (Exception e) {

			e.printStackTrace();

			new AlertDialog.Builder(FileBrowserActivity.this)
				.setMessage(R.string.err_image)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.err_general))
				.setPositiveButton(getString(R.string.ok), null)
				.create().show();
		    }

		} else {
		    openChoiseItems = new String[] {
		    		getString(R.string.open_book),
		    		getString(R.string.import_book)
		    };
		    
		    new AlertDialog.Builder(FileBrowserActivity.this)
			.setIcon(android.R.drawable.ic_menu_help)
			.setTitle(getString(R.string.open_import))
			.setItems(openChoiseItems, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					
					if(openChoiseItems[which].equals(getString(R.string.open_book))){
						BookDataHelper.openBook(FileBrowserActivity.this, adapter.getItem(position));
					} else {
						importFileTest(adapter.getItem(position));
					}
				}
			})
			.create().show();
		}
	    }
	});

	adapter = new FileBrowserAdapter(this);

	lstFiles.setAdapter(adapter);

	browseTo(new File("/"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (keyCode == 4 && !currentPath.equals("/")) {
	    browseTo(new File(currentPath).getParentFile());
	    return true;
	} else {
	    return super.onKeyDown(keyCode, event);
	}
    };

    private void browseTo(final File directory) {

	folders.clear();
	files.clear();

	for (File file : directory.listFiles()) {
	    if (file.isDirectory()) {
		folders.add(file);
	    } else {
		files.add(file);
	    }
	}

	txtCurrDir.setText(directory.getAbsolutePath());
	currentPath = directory.getAbsolutePath();

	Collections.sort(folders);
	Collections.sort(files);

	adapter.getItems().clear();
	adapter.getItems().addAll(folders);
	adapter.getItems().addAll(files);
	adapter.notifyDataSetChanged();

    }

    private void importFileTest(File file) {

		if(file.getName().endsWith(".epub")) {
		
			Importer importer = new EpubImporter();
			try {
				importer.focusOn(file);
				Book book = importer.createBook();
				
				//Insert book 
				ContentValues values = new ContentValues();
				values.put(BookTable.COLUMN_TITLE, book.getTitle());
				values.put(BookTable.COLUMN_SOURCE, book.getSource());
				
				Position p = new Position();
				p.setCurrentNode(1337).setResourcePath("Leet");
				values.put(BookTable.COLUMN_POSITION, p.getBlob());
				
				Uri bookUid = getContentResolver().insert(ThreeBookContentProvider.BOOK_URI, values);
				Long bookId = Long.parseLong(bookUid.getLastPathSegment());
				
				//Store book authors
				for(Author a : book.getAuthors()) {
        				ContentValues authorValues = new ContentValues();
        				authorValues.put(AuthorTable.COLUMN_FIRSTNAME, a.getFirstName());
        				authorValues.put(AuthorTable.COLUMN_LASTNAME, a.getLastName());
        				getContentResolver().insert(Uri.withAppendedPath(ThreeBookContentProvider.BOOK_AUTHORS_URI, String.valueOf(bookId)), authorValues);
				}
				
				//Display all books and authors
				Cursor cursor = getContentResolver().query(ThreeBookContentProvider.BOOK_URI, null, null, null, null);
				
				if(cursor != null) {
					for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
						long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookTable.COLUMN_ID));
						String title = cursor.getString(cursor.getColumnIndexOrThrow(BookTable.COLUMN_TITLE));
						String path = cursor.getString(cursor.getColumnIndex(BookTable.COLUMN_SOURCE));
						Position pos = Position.fromBlob(cursor.getBlob(cursor.getColumnIndex(BookTable.COLUMN_POSITION)));
						
						Cursor authorsCursor = getContentResolver().query(Uri.withAppendedPath(ThreeBookContentProvider.BOOK_AUTHORS_URI, String.valueOf(id)), null, null, null, null);
						
						StringBuilder toast = new StringBuilder();
						toast.append("Id: ")
							.append(id)
							.append(", Title: ")
							.append(title)
							.append(", Path: ")
							.append(path)
							.append(", Position: ")
							.append(pos);
						
						for(authorsCursor.moveToFirst(); !authorsCursor.isAfterLast(); authorsCursor.moveToNext()) {
						    toast.append(", Author: ")
						    	.append(authorsCursor.getString(authorsCursor.getColumnIndex(AuthorTable.COLUMN_FIRSTNAME)))
						    	.append(" ")
						    	.append(authorsCursor.getString(authorsCursor.getColumnIndex(AuthorTable.COLUMN_LASTNAME)));
						}
						
						Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
					}
				}
				
				cursor.close();
//				Toast.makeText(this, "Title: " + b.getTitle() + ", Author: " + b.getAuthors().get(0), Toast.LENGTH_SHORT).show();
			} catch (FileNotFoundException e) {
				Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show();
				Log.e("FileBrowserActivity", e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				//TODO: do something more constructive with this exception.
				Toast.makeText(this, "IO Exception", Toast.LENGTH_LONG).show();
				Log.e("FileBrowserActivity", (e.getMessage() != null) ? e.getMessage() : "IOException: No message");
				e.printStackTrace();
			}
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater menuInflater = getMenuInflater();
	menuInflater.inflate(R.menu.file_browser, menu);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    finish();
	    break;
	case R.id.menu_parent_directory:
	    if (!currentPath.equals("/")) {
		browseTo(new File(currentPath).getParentFile());
	    }
	    break;
	case R.id.menu_scan:
	    Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
	    break;
	}
	return super.onOptionsItemSelected(item);
    }

}
