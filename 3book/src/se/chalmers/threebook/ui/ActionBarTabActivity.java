package se.chalmers.threebook.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import se.chalmers.threeBook.R;
import se.chalmers.threebook.adapters.PagerAdapter;
import se.chalmers.threebook.ui.actionbarcompat.ActionBarHelper;
import se.chalmers.threebook.ui.fragments.AuthorsFragment;
import se.chalmers.threebook.ui.fragments.BooksFragment;
import se.chalmers.threebook.ui.fragments.TagsFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;

public class ActionBarTabActivity extends FragmentActivity implements
		TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

	final ActionBarHelper mActionBarHelper = ActionBarHelper
			.createInstance(this);

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private List<TabInfo> tabInfoList = new ArrayList<TabInfo>();
	//private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, ActionBarTabActivity.TabInfo>();
	private PagerAdapter mPagerAdapter;

	private class TabInfo {
		private String tag;
		private Class<?> className;
		private Bundle args;
		private Fragment fragment;

		TabInfo(String tag, Class<?> className, Bundle args) {
			this.tag = tag;
			this.className = className;
			this.args = args;
		}
		
		private Class getClassName(){
			return className;		
		}
		
		private String getTag(){
			return tag;		
		}
	}

	private class TabFactory implements TabContentFactory {

		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);
	}

	protected void buildTabs(Bundle savedInstanceState) {

		if (tabInfoList.size() > 0) {

			init(savedInstanceState);
			if (savedInstanceState != null) {
				mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
			}
		} else {
			Log.e("ActionBarTabAcitivy",
					"You have not added any fragments. Add them via addFragment method!");
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelper.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		/*outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);*/
	}

	private void init(Bundle args) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		List<Fragment> fragments = new Vector<Fragment>();

		for (TabInfo tabInfo : tabInfoList) {
			View tabview = createTabView(mTabHost.getContext(), tabInfo.getTag());
			ActionBarTabActivity
					.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(tabInfo.getTag())
							.setIndicator(tabview), tabInfo);
			fragments.add(Fragment.instantiate(this, tabInfo.getClassName().getName()));
		}

		this.mPagerAdapter = new PagerAdapter(
				super.getSupportFragmentManager(), fragments);
		//
		this.mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(this);


		mTabHost.setOnTabChangedListener(this);
		initTabsAppearance(mTabHost.getTabWidget());
	}

	private View createTabView(final Context context, final String tag) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_tab,
				null);
		TextView tv = (TextView) view.findViewById(R.id.txt_tab);
		tv.setText(tag.toUpperCase());
		return view;
	}

	protected ActionBarTabActivity addFragment(Class className, String title,
			Bundle args) {
		tabInfoList.add(new TabInfo(title, className, args));
		return this;
	}

	private void initTabsAppearance(TabWidget tabWidget) {
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			/*
			 * tabWidget.getChildAt(i).setBackgroundResource(
			 * R.drawable.tab_background);
			 */
		}
	}

	private static void AddTab(ActionBarTabActivity activity, TabHost tabHost,
			TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		tabSpec.setContent(activity.new TabFactory(activity));
		tabHost.addTab(tabSpec);
	}

	public void onTabChanged(String tag) {
		int pos = this.mTabHost.getCurrentTab();
		mViewPager.setCurrentItem(pos);
	}

	public void onPageSelected(int page) {
		mTabHost.setCurrentTab(page);
	}

	public void onPageScrollStateChanged(int arg0) {

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

	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
