package se.chalmers.threebook.ui.actionbarcompat;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;

public class ActionBarFragmentActivity extends FragmentActivity {

	final ActionBarHelper mActionBarHelper = ActionBarHelper
			.createInstance(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelper.onPostCreate(savedInstanceState);
	}
	
	protected ActionBarHelper getActionBarHelper() {
		return mActionBarHelper;
	}

	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarHelper.getMenuInflater(super.getMenuInflater());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retValue = false;
		retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
		retValue |= super.onCreateOptionsMenu(menu);
		return retValue;
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		mActionBarHelper.onTitleChanged(title, color);
		super.onTitleChanged(title, color);
	}
}
