package com.artifex.mupdfdemo.bookmark;

import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.OutlineAdapter;
import com.artifex.mupdfdemo.OutlineItem;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

public class BFrag extends Fragment {
	private ListView lv_calist;
	private TextView te_isshow;
	OutlineItem mItems[];
	private OutlineAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View View = inflater.inflate(R.layout.pdf_cataloglist, container, false);
		lv_calist = (ListView) View.findViewById(R.id.lv_calist);
		te_isshow = (TextView) View.findViewById(R.id.te_isshow);
		mItems = OutlineActivityData.get().items;
		if (mItems==null||mItems.length == 0) {
			te_isshow.setVisibility(android.view.View.VISIBLE);
			lv_calist.setVisibility(android.view.View.GONE);
			te_isshow.setText("暂无目录");
		} else {
			te_isshow.setVisibility(android.view.View.GONE);
			lv_calist.setVisibility(android.view.View.VISIBLE);
			adapter = new OutlineAdapter(getActivity(), mItems);
			lv_calist.setAdapter(adapter);
			lv_calist.setDividerHeight(0);
			lv_calist.requestFocusFromTouch();
			lv_calist.setSelection((lv_calist.getHeaderViewsCount() + OutlineActivityData.get().index - 13));
			lv_calist.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
					OutlineActivityData.get().position = lv_calist.getFirstVisiblePosition();
					getActivity().setResult(mItems[position].page);
					getActivity().finish();
					getActivity().overridePendingTransition(R.anim.pdf_zoomin, R.anim.pdf_zoomout);
				}
			});
			getActivity().setResult(-1);
		}
		return View;
	}
}
