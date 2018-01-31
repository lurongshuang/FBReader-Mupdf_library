package org.newfunction.catalog_bookmark;
 

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;
import org.newfunction.catalog_bookmark.adapter.CatalogApapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class BFrag extends Fragment {
	private ListView lv_calist;
	private CatalogApapter adapter;
	private TextView tv_ishave;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View View = inflater.inflate(R.layout.cataloglist, container, false);
		lv_calist=(ListView) View.findViewById(R.id.lv_calist);
		tv_ishave=(TextView)View.findViewById(R.id.tv_ishave);
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();
		final TOCTree root = fbreader.Model.TOCTree;
		if(root.getSize()>0)
		{
			tv_ishave.setVisibility(android.view.View.GONE);
			lv_calist.setVisibility(android.view.View.VISIBLE);
		}
		else
		{
			tv_ishave.setVisibility(android.view.View.VISIBLE);
			lv_calist.setVisibility(android.view.View.GONE);
		}
		adapter = new CatalogApapter(lv_calist,root,treeToSelect,getActivity());
		adapter.selectItem(treeToSelect);
		lv_calist.setAdapter(adapter);
		return View;
	}
	
	
	
	
 
}
