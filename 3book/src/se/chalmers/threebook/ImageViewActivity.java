package se.chalmers.threebook;

import java.io.File;

import se.chalmers.threebook.ui.TouchImageView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ImageViewActivity extends Activity {

	private static final float MAX_ZOOM_FACTOR = 4f;

	private TouchImageView touchImageView;
	private SeekBar barImageZoom;
	private LinearLayout layControls;

	// private TouchImageView view;

	private class FadeAnimationListener implements AnimationListener {

		private boolean visible;
		private View view;

		public FadeAnimationListener(View view, boolean visible) {
			this.view = view;
			this.visible = visible;
		}

		public void onAnimationEnd(Animation animation) {
			view.setVisibility((visible) ? View.VISIBLE : View.INVISIBLE);

		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	}

	private void toggleZoomControls(boolean show) {
		Animation a = new AlphaAnimation(layControls.getAlpha(), show ? 1 : 0);
		a.setAnimationListener(new FadeAnimationListener(layControls, show));
		a.setInterpolator(new AccelerateInterpolator());
		a.setStartOffset(show ? 0 : 2000);
		a.setDuration(show ? 500 : 1000);
		layControls.startAnimation(a);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_view);

		touchImageView = (TouchImageView) findViewById(R.id.img_image_view);
		barImageZoom = (SeekBar) findViewById(R.id.bar_image_zoom);
		layControls = (LinearLayout) findViewById(R.id.lay_zoom_controls);

		touchImageView.setSecondTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					toggleZoomControls(true);
					break;
				case MotionEvent.ACTION_UP:
					toggleZoomControls(false);
					break;
				}
				return false;
			}

		});

		toggleZoomControls(false);

		barImageZoom.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				toggleZoomControls(false);

			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				toggleZoomControls(true);
				// TODO Auto-generated method stub

			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				float value = 1 + ((MAX_ZOOM_FACTOR - 1) * (float) progress / 100f);

				touchImageView.zoom(value);

				// Log.d("asda", ""+value);
				// toughImageView.zoom(factor)

			}
		});

		// TouchImageView touch = new TouchImageView(this);
		String imagePath = (String) getIntent().getSerializableExtra(
				"imagePath");

		boolean success = false;
		if (imagePath != null && !imagePath.equals("")) {
			File imgFile = new File(imagePath);
			if (imgFile.exists()) {
				success = true;
				Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
				touchImageView.setImageBitmap(myBitmap);
			}
		}

		touchImageView.setMaxZoom(MAX_ZOOM_FACTOR);

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
