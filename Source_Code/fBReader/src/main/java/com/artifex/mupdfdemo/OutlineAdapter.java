package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

public class OutlineAdapter extends BaseAdapter {
	private final OutlineItem mItems[];
	private final LayoutInflater mInflater;

	public OutlineAdapter(LayoutInflater inflater, OutlineItem items[]) {
		mInflater = inflater;
		mItems = items;
	}

	public OutlineAdapter(Context context, OutlineItem items[]) {
		mInflater = LayoutInflater.from(context);
		mItems = items;
	}

	public int getCount() {
		return mItems.length;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.pdf_outline_entry, null);
		} else {
			v = convertView;
		}
		int level = mItems[position].level;
		if (level > 8)
			level = 8;
		String space = "";
		for (int i = 0; i < level; i++)
			space += "   ";
		if (mItems[position].page == OutlineActivityData.get().index) {
			v.findViewById(R.id.rlselect).setBackgroundColor(Color.parseColor("#CCCCCC"));
		}
		else
		{
			v.findViewById(R.id.rlselect).setBackgroundColor(Color.parseColor("#FFFFFF"));
		}
		((TextView) v.findViewById(R.id.title)).setText(space + mItems[position].title);
		((TextView) v.findViewById(R.id.page)).setText(String.valueOf(mItems[position].page + 1));
		return v;
	}

}
