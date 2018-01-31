package com.artifex.mupdfdemo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DataBaseManager {
	// 数据库对象
	public static DataBaseManager dbManager;
	public static SQLiteDatabase db;

	public static DataBaseManager getDB() {
		if (dbManager == null) {
			dbManager = new DataBaseManager();
		}
		return dbManager;
	}

	public static SQLiteDatabase getInstance() {
		return db;
	}

	/**
	 * 创建数据库文件
	 * 
	 * @param path
	 *            数据库文件地址
	 */
	public SQLiteDatabase createOrOpenDB(Context context,String path, String dbname) {
		if(path==null)
		{
			path=context.getFilesDir().getAbsolutePath()+"/databases/";
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		db = SQLiteDatabase.openOrCreateDatabase(path + dbname, null);
		return db;

	}

	/**
	 * 进行对数据库的 增删改 或者创建表
	 * 
	 * @param sql
	 *            执行的sql语句 支持占位符
	 * @param values
	 *            数据数组
	 * @return 执行结果
	 */
	public boolean Exesql(String sql, Object[] values, SQLiteDatabase db) {
		if (db == null) {
			return false;
		}
		try {
			if (values != null && values.length > 0) {
				db.execSQL(sql, values);
			} else {
				db.execSQL(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param values
	 *            数据值
	 * @return 查询数据
	 */
	public List<Map<String, Object>> querysql(String sql,
			String[] values, SQLiteDatabase db) {
		if (db == null) {
			return null;
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (values != null && values.length > 0) {
			Cursor cursor = db.rawQuery(sql, values);
			while (cursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					map.put(cursor.getColumnName(i), cursor.getString(i));
				}
				list.add(map);
			}
		} else {
			Cursor cursor = db.rawQuery(sql, new String[] {});
			while (cursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					map.put(cursor.getColumnName(i), cursor.getString(i));
				}
				list.add(map);
			}
		}
		return list;

	}

	// 关闭数据库
	public boolean closeDB(SQLiteDatabase db) {
		if (db != null) {
			db.close();
		}
		return true;
	}
}