package man;

import java.io.File;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.ui.android.R;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.artifex.mupdfdemo.MuPDFActivity;

public class Main extends Activity {
	public Button btpdf;
	public Button bteup;
	public Button bttxt;
	private Context context;
	private BookCollectionShadow bs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		btpdf = (Button) findViewById(R.id.btpdf);
		bteup = (Button) findViewById(R.id.bteup);
		bttxt = (Button) findViewById(R.id.bttxt);
		context = Main.this;
		bs =new BookCollectionShadow();
		bs.bindToService(context, null);
		btpdf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(Environment.getExternalStorageDirectory().getPath() + "/JavaScript.pdf");
				if (file.exists()) {
					openFile("pdf", file + "");
				}

			}
		});

		bteup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(Environment.getExternalStorageDirectory().getPath() + "/追风筝的人.epub");
				if (file.exists()) {
					openFile("epub", file + "");
				}
			}
		});

		bttxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(Environment.getExternalStorageDirectory().getPath() + "/33.txt");
				if (file.exists()) {
					openFile("txt", file+"");
				}

			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	public void openFile(String type, String filePath) {
		Book book = bs.getBookByFile(filePath);
		if (type.equalsIgnoreCase("txt")||type.equalsIgnoreCase("epub")) {
			FBReader.openBookActivity(context, book, null);
		}
		else if (type.equalsIgnoreCase("pdf")) {
			// ��������ͼ����PDF��ʽ��ִ�и÷���
			Uri uri = Uri.parse(filePath);
			Intent intent = new Intent(this,MuPDFActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);

		}
	}
}
