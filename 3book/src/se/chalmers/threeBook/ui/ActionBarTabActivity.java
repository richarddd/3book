package se.chalmers.threeBook.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import se.chalmers.threeBook.R;
import se.chalmers.threeBook.adapters.PagerAdapter;
import se.chalmers.threeBook.ui.actionbarcompat.ActionBarHelper;
import se.chalmers.threeBook.ui.fragments.AuthorsFragment;
import se.chalmers.threeBook.ui.fragments.BooksFragment;
import se.chalmers.threeBook.ui.fragments.TagsFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

public class ActionBarTabActivity extends FragmentActivity implements
TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
	
	final ActionBarHelper mActionBarHelper = ActionBarHelper
			.createInstance(this);

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, ActionBarTabActivity.TabInfo>();
	private PagerAdapter mPagerAdapter;

	private class TabInfo {
		private String tag;
		private Class<?> clss;
		private Bundle args;
		private Fragment fragment;

		TabInfo(String tag, Class<?> clazz, Bundle args) {
			this.tag = tag;
			this.clss = clazz;
			this.args = args;
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
		super.onCreate(savedInstanceState); // Inflate the layout'
		mActionBarHelper.onCreate(savedInstanceState);
		
		
	}
	
	protected void build(Bundle savedInstanceState){
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		intialiseViewPager();	
	}
	
	protected ActionBarTabActivity addFragment(Class className, String title){
		
		return this;
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelper.onPostCreate(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}
	
	private void intialiseViewPager() {

		List<Fragment> fragments = new Vector<Fragment>();
		fragments
				.add(Fragment.instantiate(this, BooksFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,
				AuthorsFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, TagsFragment.class.getName()));
		this.mPagerAdapter = new PagerAdapter(
				super.getSupportFragmentManager(), fragments);
		//
		this.mViewPager = (ViewPager) super
				.findViewById(R.id.view_collection_pager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(this);
	}
	
	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabInfo tabInfo = null;

		ActionBarTabActivity.AddTab(this, this.mTabHost, this.mTabHost
				.newTabSpec("Tab1").setIndicator("Tab 1"),
				(tabInfo = new TabInfo("Tab1", BooksFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		ActionBarTabActivity.AddTab(this, this.mTabHost, this.mTabHost
				.newTabSpec("Tab2").setIndicator("Tab 2"),
				(tabInfo = new TabInfo("Tab2", AuthorsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		ActionBarTabActivity.AddTab(this, this.mTabHost, this.mTabHost
				.newTabSpec("Tab3").setIndicator("Tab 3"),
				(tabInfo = new TabInfo("Tab3", TagsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		// Default to first tab
		// this.onTabChanged("Tab1");
		//
		mTabHost.setOnTabChangedListener(this);
		initTabsAppearance(mTabHost.getTabWidget());
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
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
		tabHost.addTab(tabSpec);
	}

	public void onTabChanged(String tag) {
		// TabInfo newTab = this.mapTabInfo.get(tag);
		int pos = this.mTabHost.getCurrentTab();
		this.mViewPager.setCurrentItem(pos);
	}
	

	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
