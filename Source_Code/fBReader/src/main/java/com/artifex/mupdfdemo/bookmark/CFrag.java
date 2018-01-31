package com.artifex.mupdfdemo.bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.artifex.mupdfdemo.DataBaseManager;
import com.artifex.mupdfdemo.MarkLineAdapter;
import com.artifex.mupdfdemo.OutlineItem;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

public class CFrag extends Fragment {
	private ListView lv_calist;
	private TextView te_isshow;
	private OutlineItem MarkList[];
	private MarkLineAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View View = inflater.inflate(R.layout.pdf_cataloglist, container, false);
		lv_calist = (ListView) View.findViewById(R.id.lv_calist);
		te_isshow = (TextView) View.findViewById(R.id.te_isshow);
		Intent intent = getActivity().getIntent();
		String mFileName = intent.getStringExtra("mFileName");
		SQLiteDatabase db = openDBOrTable();
		String[] values = new String[1];
		values[0] = mFileName;
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		lists = DataBaseManager.getDB().querysql("select * from bookmark where bookName=? order by id desc", values,
				db);
		if (lists.size() > 0) {
			te_isshow.setVisibility(android.view.View.GONE);
			lv_calist.setVisibility(android.view.View.VISIBLE);
			MarkList = new OutlineItem[lists.size()];
			for (int i = 0; i < lists.size(); i++) {
				Map<String, Object> map = lists.get(i);
				OutlineItem ol = new OutlineItem(Integer.parseInt(map.get("id") + ""), map.get("bookName").toString(),
						Integer.parseInt(map.get("bookNum") + ""));
				MarkList[i] = ol;
			}
			adapter = new MarkLineAdapter(getActivity(), MarkList);
			lv_calist.setAdapter(adapter);
			getActivity().setResult(-1);
		} else {
			te_isshow.setVisibility(android.view.View.VISIBLE);
			lv_calist.setVisibility(android.view.View.GONE);
			te_isshow.setText("书签 ");
		}
		lv_calist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
				Intent intnet = new Intent();
				intnet.putExtra("code", 110);
				getActivity().setResult(MarkList[position].page, intnet);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.pdf_zoomin, R.anim.pdf_zoomout);
			}
		});

		return View;
	}

	public SQLiteDatabase openDBOrTable() {

		SQLiteDatabase db = DataBaseManager.getDB()
				.createOrOpenDB(getActivity().getApplication().getApplicationContext(), null, "bookmark.db");
		DataBaseManager.getDB().Exesql(
				"create table if not exists bookmark(id integer primary key autoincrement,bookName text, bookNum text)",
				null, db);
		return db;
	}
}
