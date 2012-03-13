package se.chalmers.threebook;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.chalmers.threebook.adapters.FileBrowserAdapter;
import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
		
		/*getActionBarHelper().set*/

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

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (adapter.getItem(position).isDirectory()) {
					try {
						browseTo(adapter.getItem(position));
					} catch (Exception e) {

						e.printStackTrace();

						new AlertDialog.Builder(FileBrowserActivity.this)
								.setMessage(R.string.err_file_browser_general)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setTitle(getString(R.string.err_general))
								.setPositiveButton(getString(R.string.ok), null)
								.create().show();
					}

				} else {
					openFile(adapter.getItem(position));
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

	private void openFile(File file) {	
		if(file.getName().endsWith(".epub")){		
			Intent displayBook= new Intent(this, ReadActivity.class);
			displayBook.putExtra(ReadActivity.IntentKey.FILE_PATH.toString(), file.getAbsolutePath());
			displayBook.putExtra(ReadActivity.IntentKey.INTENT_TYPE.toString(), ReadActivity.IntentType.READ_BOOK_FROM_LIBRARY);
			startActivity(displayBook);
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
