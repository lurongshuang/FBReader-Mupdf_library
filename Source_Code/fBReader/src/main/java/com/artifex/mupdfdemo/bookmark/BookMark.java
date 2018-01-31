package com.artifex.mupdfdemo.bookmark;

import com.artifex.mupdfdemo.bookmark.widget.PagerSlidingTabStrip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import org.geometerplus.zlibrary.ui.android.R;

public class BookMark extends FragmentActivity {
	PagerSlidingTabStrip tabs;
	ViewPager pager;
	DisplayMetrics dm;
	BFrag bfrag;
	CFrag cfrag;
	String[] titles = {"目录", "书签" };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.pdf_book_mark);
		dm = getResources().getDisplayMetrics();
		pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyAdapter(getSupportFragmentManager(),titles));
		tabs.setViewPager(pager);
	}
	

	public class MyAdapter extends FragmentPagerAdapter{
		String[] _titles;
		public MyAdapter(FragmentManager fm,String[] titles) {
			super(fm);
			_titles=titles;
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
