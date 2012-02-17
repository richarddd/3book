package se.chalmers.threeBook;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.chalmers.threeBook.adapters.FileBrowserAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserActivity extends Activity {

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

		txtCurrDir = (TextView) findViewById(R.id.txt_file_path);
		lstFiles = (ListView) findViewById(R.id.lst_book_browser);
		ImageButton btnDirUp = (ImageButton) findViewById(R.id.btn_dir_up);

		btnDirUp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!currentPath.equals("/")) {
					browseTo(new File(currentPath).getParentFile());
				}
			}
		});

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

		txtCurrDir.setText(directory.getAbsolutePath());
		currentPath = directory.getAbsolutePath();

		folders.clear();
		files.clear();

		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				folders.add(file);
			} else {
				files.add(file);
			}
		}

		Collections.sort(folders);
		Collections.sort(files);

		adapter.getItems().clear();
		adapter.getItems().addAll(folders);
		adapter.getItems().addAll(files);
		adapter.notifyDataSetChanged();

	}

	private void openFile(File file) {

		/*
		 * Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW,
		 * Uri.parse("file://" + aFile.getAbsolutePath()));
		 * startActivity(myIntent);
		 */
	}

}
