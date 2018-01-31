package com.artifex.mupdfdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MarkLineActivity extends ListActivity {
	private OutlineItem MarkList[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 this.getListView().setBackgroundColor(Color.parseColor("#FFFFFF"));
		Intent intent = getIntent();
		String mFileName = intent.getStringExtra("mFileName");
		String progress = intent.getStringExtra("progress");
		SQLiteDatabase db = openDBOrTable();
		String[] values = new String[1];
		values[0] = mFileName;
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		lists = DataBaseManager.getDB().querysql("select * from bookmark where bookName=? order by id desc", values, db);
		if (lists.size() > 0) {
			MarkList= new OutlineItem[lists.size()];
			for(int i=0;i<lists.size();i++)
			{
				Map<String, Object> map=lists.get(i);
				OutlineItem ol= new OutlineItem(Integer.parseInt(map.get("id")+""), map.get("bookName").toString(), Integer.parseInt(map.get("bookNum")+""));
				MarkList[i]=ol;
			}
			setListAdapter(new MarkLineAdapter(getLayoutInflater(),MarkList));
			setResult(-1);

		}
	}

	public SQLiteDatabase openDBOrTable() {

		SQLiteDatabase db = DataBaseManager.getDB().createOrOpenDB(getApplication().getApplicationContext(), null,
				"bookmark.db");
		DataBaseManager.getDB().Exesql(
				"create table if not exists bookmark(id integer primary key autoincrement,bookName text, bookNum text)",
				null, db);
		return db;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		setResult(MarkList[position].page);
		finish();

	}

}
