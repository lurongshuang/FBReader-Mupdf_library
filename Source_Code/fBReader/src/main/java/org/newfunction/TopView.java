package org.newfunction;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class TopView extends Fragment {
	// private LayoutInflater mInflater;
	private RelativeLayout maintop;
	private ImageView bt_back;
	private ImageView bt_mark;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View TopView = inflater.inflate(R.layout.topview, container, false);
		maintop = (RelativeLayout) TopView.findViewById(R.id.maintop);
		bt_back = (ImageView) TopView.findViewById(R.id.bt_back);
		bt_mark = (ImageView) TopView.findViewById(R.id.bt_mark);
		// 返回点击
		bt_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 getActivity().finish();
			}
		});

		// 书签点击
		bt_mark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				FBReaderApp myReader = (FBReaderApp) FBReaderApp.Instance();
//				Book book = myReader.getCurrentBook();
//
//				ZLTextView textView = myReader.getTextView();
//				ZLTextView.PagePosition pagePosition = textView.pagePosition();
//				Log.e("11111", (pagePosition.Total)+"---------"+(pagePosition.Current));
			}
		});

		return TopView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public static void changePage(String page) {
		FBReaderApp myReader = (FBReaderApp) FBReaderApp.Instance();
		Book book = myReader.getCurrentBook();
		ZLTextView textView = myReader.getTextView();
		ZLTextView.PagePosition pagePosition = textView.pagePosition();
		Log.e("11111", (pagePosition.Total - 1)+"---------"+(pagePosition.Current - 1));
	}

}
