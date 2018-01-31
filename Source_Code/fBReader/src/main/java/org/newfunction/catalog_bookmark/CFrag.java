package org.newfunction.catalog_bookmark;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.bookmark.BookmarksAdapter;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.SearchManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CFrag extends Fragment implements IBookCollection.Listener<Book> {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	private volatile Bookmark myBookmark;
	private volatile BookmarksAdapter myThisBookAdapter;
	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private View View;
	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();
	private final Map<Integer, HighlightingStyle> myStyles = Collections
			.synchronizedMap(new HashMap<Integer, HighlightingStyle>());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View = inflater.inflate(R.layout.bookmarks, container, false);
		ListView listView=(ListView) View.findViewById(R.id.bookmarks_this_book);
		listView.setDividerHeight(1); 
		listView.setDivider(new ColorDrawable(Color.GREEN));  
		Thread.setDefaultUncaughtExceptionHandler(
				new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(getActivity()));
		getActivity().setDefaultKeyMode(getActivity().DEFAULT_KEYS_SEARCH_LOCAL);
		final SearchManager manager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
		manager.setOnCancelListener(null);
		myBook = FBReaderIntents.getBookExtra(getActivity().getIntent(), myCollection);
		if (myBook == null) {
//			getActivity().finish();
		}
		myBookmark = FBReaderIntents.getBookmarkExtra(getActivity().getIntent());
		return View;
	}

	@Override
	public void onStart() {
		super.onStart();
		myCollection.bindToService(getActivity(), new Runnable() {
			public void run() {
				myThisBookAdapter = new BookmarksAdapter((ListView) View.findViewById(R.id.bookmarks_this_book),
						myBookmark != null, getActivity(), myComparator, myResource, myStyles, myCollection,
						myBookmark);
				myCollection.addListener(CFrag.this);
				updateStyles();
				loadBookmarks();
			}
		});
		OrientationUtil.setOrientation(getActivity(), getActivity().getIntent());
	}

	private void updateStyles() {
		synchronized (myStyles) {
			myStyles.clear();
			for (HighlightingStyle style : myCollection.highlightingStyles()) {
				myStyles.put(style.Id, style);
			}
		}
	}

	private final Object myBookmarksLock = new Object();

	private void loadBookmarks() {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					for (BookmarkQuery query = new BookmarkQuery(myBook, 50);; query = query.next()) {
						final List<Bookmark> thisBookBookmarks = myCollection.bookmarks(query);
						if (thisBookBookmarks.isEmpty()) {
							break;
						}
						myThisBookAdapter.addAll(thisBookBookmarks);
					}
					for (BookmarkQuery query = new BookmarkQuery(50);; query = query.next()) {
						final List<Bookmark> allBookmarks = myCollection.bookmarks(query);
						if (allBookmarks.isEmpty()) {
							break;
						}
					}
				}
			}
		}).start();
	}

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
		default:
			break;
		case BookmarkStyleChanged:
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					updateStyles();
					myThisBookAdapter.notifyDataSetChanged();
				}
			});
			break;
		case BookmarksUpdated:
			updateBookmarks(book);
			break;
		}
	}

	private void updateBookmarks(final Book book) {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					final boolean flagThisBookTab = book.getId() == myBook.getId();

					final Map<String, Bookmark> oldBookmarks = new HashMap<String, Bookmark>();
					if (flagThisBookTab) {
						for (Bookmark b : myThisBookAdapter.bookmarks()) {
							oldBookmarks.put(b.Uid, b);
						}
					}

					for (BookmarkQuery query = new BookmarkQuery(book, 50);; query = query.next()) {
						final List<Bookmark> loaded = myCollection.bookmarks(query);
						if (loaded.isEmpty()) {
							break;
						}
						for (Bookmark b : loaded) {
							final Bookmark old = oldBookmarks.remove(b.Uid);
							if (flagThisBookTab) {
								myThisBookAdapter.replace(old, b);
							}
						}
					}
					if (flagThisBookTab) {
						myThisBookAdapter.removeAll(oldBookmarks.values());
					}
				}
			}
		}).start();
	}

	@Override
	public void onBuildEvent(Status status) {
		// TODO Auto-generated method stub

	}

}
