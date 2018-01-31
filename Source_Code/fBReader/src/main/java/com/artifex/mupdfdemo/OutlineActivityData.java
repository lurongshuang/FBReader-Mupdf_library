package com.artifex.mupdfdemo;

public class OutlineActivityData {
	public OutlineItem items[];
	public int         position;
	public int         index;
	static private OutlineActivityData singleton;

	static public void set(OutlineActivityData d) {
		singleton = d;
	}

	static public OutlineActivityData get() {
		if (singleton == null)
			singleton = new OutlineActivityData();
		return singleton;
	}
}
