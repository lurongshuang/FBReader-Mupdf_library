package org.newfunction.catalog_bookmark;

import org.geometerplus.zlibrary.ui.android.R;
import org.newfunction.catalog_bookmark.widget.PagerSlidingTabStrip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

public class Ctatalog_Bookmark extends FragmentActivity {
	PagerSlidingTabStrip tabs;
	ViewPager pager;
	DisplayMetrics dm;

	BFrag bfrag;
	CFrag cfrag;
	String[] titles = { "目录", "书签" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}

	private void initView() {
		setContentView(R.layout.ctatalog_bookmark);
		dm = getResources().getDisplayMetrics();
		pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyAdapter(getSupportFragmentManager(), titles));
		tabs.setViewPager(pager);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

	public class MyAdapter extends FragmentPagerAdapter {
		String[] _titles;

		public MyAdapter(FragmentManager fm, String[] titles) {
			super(fm);
			_titles = titles;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return _titles[position];
		}

		@Override
		public int getCount() {
			return _titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				if (bfrag == null) {
					bfrag = new BFrag();
				}
				return bfrag;
			case 1:
				if (cfrag == null) {
					cfrag = new CFrag();
				}
				return cfrag;
			default:
				return null;
			}
		}
	}
}
