package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

@SuppressLint("InflateParams")
public class MarkLineAdapter  extends BaseAdapter{
	private  OutlineItem markitems[];
	private  LayoutInflater mInflater;
	public MarkLineAdapter(LayoutInflater inflater, OutlineItem items[]) {
		mInflater = inflater;
		markitems    = items;
	}
	public MarkLineAdapter(Context context,OutlineItem items[])
	{
		mInflater = LayoutInflater.from(context) ;
		markitems    = items;
	}

	@Override
	public int getCount() {
		return markitems.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.pdf_outline_entry, null);
		} else {
			v = convertView;
		}
		
		((TextView)v.findViewById(R.id.title)).setText("第"+(markitems[position].page+1 + MuPDFActivity.mPageSliderRes / 2) / MuPDFActivity.mPageSliderRes+"页");
//		((TextView)v.findViewById(R.id.page)).setText(markitems[position].page);
		return v;
	}

}
