package se.chalmers.threebook;

import java.io.File;

import se.chalmers.threebook.ui.TouchImageView;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class ImageViewActivity extends ActionBarActivity {

	// private TouchImageView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_image_view);

		TouchImageView touch = new TouchImageView(this);
		String path = (String) getIntent().getSerializableExtra("imagePath");
		boolean success = false;
		if (path != null && !path.equals("")) {
			File imgFile = new File(path);
			if (imgFile.exists()) {
				success = true;
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
						.getAbsolutePath());
				touch.setImageBitmap(myBitmap);
			}
		}

		touch.setMaxZoom(4f);
		setContentView(touch);

		if (!success) {
			new AlertDialog.Builder(this)
					.setMessage(R.string.err_file_browser_general)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(getString(R.string.err_general))
					.setPositiveButton(getString(R.string.ok),
							new OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).create().show();
		}
	}

}
