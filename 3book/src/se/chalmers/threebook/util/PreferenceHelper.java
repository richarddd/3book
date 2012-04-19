package se.chalmers.threebook.util;

import se.chalmers.threebook.R;
import se.chalmers.threebook.ui.ColorPickerDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PreferenceHelper {

	private AlertDialog marginDialog;
	private AlertDialog fontSizeDialog;
	private SeekBar seekBarMarginOne;
	private SeekBar seekBarMarginTwo;
	private SeekBar seekBarFontSize;
	private SharedPreferences settings;
	private Dialog colorDialog;
	private Context context;

	private class SeekValue {
		int min;
		int max;

		public SeekValue(int min, int max) {
			super();
			this.min = min;
			this.max = max;
		}
	}

	private interface SeekBarCallBack {
		public void onProgressChanged(int value);
	}

	public class SeekBarListener implements OnSeekBarChangeListener {
		private TextView textView;
		private int min, max;
		private SeekBarCallBack callback;

		public SeekBarListener(SeekBar seekBar, TextView textView, int min,
				int max, SeekBarCallBack callback) {
			init(seekBar, textView, min, max);
			this.callback = callback;
		}

		public SeekBarListener(SeekBar seekBar, TextView textView, int min,
				int max) {
			init(seekBar, textView, min, max);
		}

		private void init(SeekBar seekBar, TextView textView, int min, int max) {
			this.textView = textView;
			this.min = min;
			this.max = max;
			seekBar.setTag(new SeekValue(min, max));

			if (max < min) {
				throw new IllegalArgumentException(
						"max can't be less then min!");
			}
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			int val = valueFromProgress(min, max, progress);

			textView.setText(String.valueOf(val));
			if (callback != null) {
				callback.onProgressChanged(val);
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	public int valueFromProgress(int min, int max, int progress) {
		float perc = progress / 100f;
		return (int) (min + ((max - min) * perc));
	}

	public int progressFromValue(int min, int max, int value) {
		
		return (int)(((value - min*1f) / (max - min*1f))*100f);
	}

	public PreferenceHelper(Context context) {

		this.context = context;
		settings = PreferenceManager.getDefaultSharedPreferences(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View marginView = inflater.inflate(R.layout.dialog_margin_picker, null);
		View fontSizeView = inflater.inflate(R.layout.dialog_font_size, null);

		seekBarMarginOne = (SeekBar) marginView.findViewById(R.id.bar_one);
		seekBarMarginTwo = (SeekBar) marginView.findViewById(R.id.bar_two);
		seekBarFontSize = (SeekBar) fontSizeView.findViewById(R.id.bar_one);
		
		colorDialog = new ColorPickerDialog(context, new ColorPickerDialog.OnColorChangedListener() {
			public void colorChanged(int color) {
				
			}
		}, settings.getInt(Constants.SETTINGS_BACKGROUND_COLOR.value(), 0xFFFFFF));

		TextView txtMarginBarOne = (TextView) marginView
				.findViewById(R.id.txt_bar_one);
		TextView txtMarginBarTwo = (TextView) marginView
				.findViewById(R.id.txt_bar_two);
		TextView txtFontSizeBarOne = (TextView) fontSizeView
				.findViewById(R.id.txt_bar_one);
		final TextView txtFontSizePreview = (TextView) fontSizeView
				.findViewById(R.id.txt_font_preview);

		seekBarMarginOne.setOnSeekBarChangeListener(new SeekBarListener(
				seekBarMarginOne, txtMarginBarOne, 0, 50));
		seekBarMarginTwo.setOnSeekBarChangeListener(new SeekBarListener(
				seekBarMarginTwo, txtMarginBarTwo, 0, 50));
		seekBarFontSize.setOnSeekBarChangeListener(new SeekBarListener(
				seekBarFontSize, txtFontSizeBarOne, 5, 40,
				new SeekBarCallBack() {
					public void onProgressChanged(int value) {
						txtFontSizePreview.setTextSize(Helper.dpToPx(
								PreferenceHelper.this.context, value));
					}
				}));
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		// create margin dialog
		builder.setTitle(R.string.dialog_margin_title);
		builder.setView(marginView);
		builder.setCancelable(true);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor editor = settings.edit();
				
				SeekValue sv1 = (SeekValue) seekBarMarginOne.getTag();
				SeekValue sv2 = (SeekValue) seekBarMarginTwo.getTag();
				
				editor.putInt(Constants.SETTINGS_MARGIN_TOP_BOTTOM_KEY.value(),
						valueFromProgress(sv1.min, sv1.max,
								seekBarMarginOne.getProgress()));
				editor.putInt(Constants.SETTINGS_MARGIN_RIGHT_LEFT_KEY.value(),
						valueFromProgress(sv2.min, sv2.max,
								seekBarMarginTwo.getProgress()));
				editor.commit();
				dialog.dismiss();
			}
		});
		marginDialog = builder.create();

		// create margin dialog
		builder.setTitle(R.string.dialog_font_size_title);
		builder.setView(fontSizeView);
		builder.setCancelable(true);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor editor = settings.edit();
				SeekValue sv = (SeekValue) seekBarFontSize.getTag();
				editor.putInt(
						Constants.SETTINGS_FONT_SIZE.value(),
						valueFromProgress(sv.min, sv.max,
								seekBarFontSize.getProgress()));
				editor.commit();

				dialog.dismiss();
			}
		});
		fontSizeDialog = builder.create();

	}

	public SharedPreferences getSharedPreferences() {
		return settings;
	}

	public void showMarginDialog() {

		int val1 = settings.getInt(
				Constants.SETTINGS_MARGIN_TOP_BOTTOM_KEY.value(), 10);
		SeekValue sv1 = (SeekValue) seekBarMarginOne.getTag();
		int val2 = settings.getInt(
				Constants.SETTINGS_MARGIN_RIGHT_LEFT_KEY.value(), 10);
		SeekValue sv2 = (SeekValue) seekBarMarginTwo.getTag();
		seekBarMarginOne.setProgress(progressFromValue(sv1.min, sv1.max, val1));
		seekBarMarginTwo.setProgress(progressFromValue(sv2.min, sv2.max, val2));
		marginDialog.show();
	}

	public void showFontSizeDialog() {
		int val = settings.getInt(Constants.SETTINGS_FONT_SIZE.value(), 12);
		SeekValue sv = (SeekValue) seekBarFontSize.getTag();
		seekBarFontSize.setProgress(progressFromValue(sv.min, sv.max, val));
		fontSizeDialog.show();
	}
	
	public void showBackgroundColorDialog(){
		colorDialog.show();
	}
}
