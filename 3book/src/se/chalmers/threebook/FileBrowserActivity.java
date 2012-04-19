package se.chalmers.threebook;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.chalmers.threebook.adapters.FileBrowserAdapter;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import se.chalmers.threebook.util.Helper;
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
	
	public class FileSelect{
		private File file;
		private boolean selected;
		
		public FileSelect(File file, boolean selected) {
			this.file = file;
			this.selected = selected;
		}
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}	
	}

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
				File file = adapter.getItem(position).getFile();
				if (file.isDirectory()) {
					try {
						browseTo(file);
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
					openFile(file);
				}
			}
		});

		adapter = new FileBrowserAdapter(this);

		lstFiles.setAdapter(adapter);

		//XXX debuging only
		browseTo(new File("/"));
		//openFile(new File("/sdcard/austen-pride-and-prejudice-illustrations.epub"));
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
		for(File f : folders){
			adapter.getItems().add(new FileSelect(f, false));
		}
		for(File f : files){
			adapter.getItems().add(new FileSelect(f, false));
		}
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
