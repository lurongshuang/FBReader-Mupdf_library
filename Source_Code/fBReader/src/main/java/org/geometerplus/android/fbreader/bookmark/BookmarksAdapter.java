package org.geometerplus.android.fbreader.bookmark;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import yuku.ambilwarna.widget.AmbilWarnaPrefWidgetView;

public class BookmarksAdapter extends BaseAdapter
		implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

	private Context context;
	private Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();
	private ZLResource myResource = ZLResource.resource("bookmarksView");
	private Map<Integer, HighlightingStyle> myStyles = Collections
			.synchronizedMap(new HashMap<Integer, HighlightingStyle>());
	private BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Bookmark myBookmark;

	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private final List<Bookmark> myBookmarksList = Collections.synchronizedList(new LinkedList<Bookmark>());
	private volatile boolean myShowAddBookmarkItem;

	public BookmarksAdapter(ListView listView, boolean showAddBookmarkItem, Context context, Comparator<Bookmark> myComparator,
			ZLResource myResource, Map<Integer, HighlightingStyle> myStyles, BookCollectionShadow myCollection,
			Bookmark myBookmark) {
		myShowAddBookmarkItem = showAddBookmarkItem;
		listView.setAdapter(this);
		listView.setOnItemClickListener(this);
//		listView.setOnCreateContextMenuListener(this);
		this.context = context;
		this.myComparator = myComparator;
		this.myResource=myResource;
		this.myStyles=myStyles;
		this.myCollection=myCollection;
		this.myBookmark=myBookmark;

	}

	public List<Bookmark> bookmarks() {
		return Collections.unmodifiableList(myBookmarksList);
	}

	public void addAll(final List<Bookmark> bookmarks) {
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				synchronized (myBookmarksList) {
					for (Bookmark b : bookmarks) {
						final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
						if (position < 0) {
							myBookmarksList.add(-position - 1, b);
						}
					}
				}
				notifyDataSetChanged();
			}
		});
	}

	private boolean areEqualsForView(Bookmark b0, Bookmark b1) {
		return b0.getStyleId() == b1.getStyleId() && b0.getText().equals(b1.getText())
				&& b0.getTimestamp(Bookmark.DateType.Latest).equals(b1.getTimestamp(Bookmark.DateType.Latest));
	}

	public void replace(final Bookmark old, final Bookmark b) {
		if (old != null && areEqualsForView(old, b)) {
			return;
		}
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				synchronized (myBookmarksList) {
					if (old != null) {
						myBookmarksList.remove(old);
					}
					final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
					if (position < 0) {
						myBookmarksList.add(-position - 1, b);
					}
				}
				notifyDataSetChanged();
			}
		});
	}

	public void removeAll(final Collection<Bookmark> bookmarks) {
		if (bookmarks.isEmpty()) {
			return;
		}
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				myBookmarksList.removeAll(bookmarks);
				notifyDataSetChanged();
			}
		});
	}

	public void clear() {
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				myBookmarksList.clear();
				notifyDataSetChanged();
			}
		});
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
		if (getItem(position) != null) {
			menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("openBook").getValue());
			menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("editBookmark").getValue());
			menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("deleteBookmark").getValue());
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
		final ImageView imageView = ViewUtil.findImageView(view, R.id.bookmark_item_icon);
		final View colorContainer = ViewUtil.findView(view, R.id.bookmark_item_color_container);
		final AmbilWarnaPrefWidgetView colorView = (AmbilWarnaPrefWidgetView) ViewUtil.findView(view,
				R.id.bookmark_item_color);
		final TextView textView = ViewUtil.findTextView(view, R.id.bookmark_item_text);
		final TextView bookTitleView = ViewUtil.findTextView(view, R.id.bookmark_item_booktitle);

		final Bookmark bookmark = getItem(position);
		if (bookmark == null) {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.ic_list_plus1);
			colorContainer.setVisibility(View.GONE);
			textView.setText(myResource.getResource("new").getValue());
			bookTitleView.setVisibility(View.GONE);
		} else {
			imageView.setVisibility(View.GONE);
			colorContainer.setVisibility(View.VISIBLE);
			BookmarksUtil.setupColorView(colorView, myStyles.get(bookmark.getStyleId()));
			textView.setText(bookmark.getText());
			if (myShowAddBookmarkItem) {
				bookTitleView.setVisibility(View.GONE);
			} else {
				bookTitleView.setVisibility(View.GONE);
				bookTitleView.setText(bookmark.BookTitle);
			}
		}
		return view;
	}

	@Override
	public final boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public final boolean isEnabled(int position) {
		return true;
	}

	@Override
	public final long getItemId(int position) {
		final Bookmark item = getItem(position);
		return item != null ? item.getId() : -1;
	}

	@Override
	public final Bookmark getItem(int position) {
		if (myShowAddBookmarkItem) {
			--position;
		}
		return position >= 0 ? myBookmarksList.get(position) : null;
	}

	@Override
	public final int getCount() {
		return myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Bookmark bookmark = getItem(position);
		if (bookmark != null) {
			gotoBookmark(bookmark);
		} else if (myShowAddBookmarkItem) {
			myShowAddBookmarkItem = false;
			myCollection.saveBookmark(myBookmark);
		}
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.BookId);
		if (book != null) {
			FBReader.openBookActivity(context, book, bookmark);
		} else {
			UIMessageUtil.showErrorMessage(((Activity) context), "cannotOpenBook");
		}
	}

}
