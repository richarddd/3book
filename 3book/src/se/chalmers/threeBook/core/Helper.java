package se.chalmers.threeBook.core;

import android.os.Build;

public class Helper {
	public static boolean SupportsNewApi(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
}
