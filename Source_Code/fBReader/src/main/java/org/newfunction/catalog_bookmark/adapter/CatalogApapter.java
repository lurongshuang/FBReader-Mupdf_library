package org.newfunction.catalog_bookmark.adapter;

import org.geometerplus.android.fbreader.ZLTreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class CatalogApapter extends ZLTreeAdapter{

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;
	private ZLTree<?> mySelectedItem;
	private Context context;
	
	public CatalogApapter(ListView parent, ZLTree<?> root,ZLTree<?> mySelectedItem,Context context) {
		super(parent, root);
		this.mySelectedItem=mySelectedItem;
		this.context=context;
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final TOCTree tree = (TOCTree)getItem(position);
		if (tree.hasChildren()) {
			menu.setHeaderTitle(tree.getText());
			final ZLResource resource = ZLResource.resource("tocView");
			menu.add(0, PROCESS_TREE_ITEM_ID, 0, resource.getResource(isOpen(tree) ? "collapseTree" : "expandTree").getValue());
			menu.add(0, READ_BOOK_ITEM_ID, 0, resource.getResource("readText").getValue());
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
		final TOCTree tree = (TOCTree)getItem(position);
		view.setBackgroundColor(tree == mySelectedItem ? Color.parseColor("#d9d9d9") : 0);
		setIcon(ViewUtil.findImageView(view, R.id.toc_tree_item_icon), tree);
		ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
		return view;
	}

	void openBookText(TOCTree tree) {
		final TOCTree.Reference reference = tree.getReference();
		if (reference != null) {
			((Activity)context).finish();
			((Activity)context).overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			fbreader.addInvisibleBookmark();
			fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
			fbreader.showBookTextView();
			fbreader.storePosition();
		}
	}

	@Override
	protected boolean runTreeItem(ZLTree<?> tree) {
		if (super.runTreeItem(tree)) {
			return true;
		}
		openBookText((TOCTree)tree);
		return true;
	}
}
