/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.bookmark;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

public class BookmarksActivity extends Activity implements IBookCollection.Listener<Book> {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;
	private final Map<Integer, HighlightingStyle> myStyles = Collections
			.synchronizedMap(new HashMap<Integer, HighlightingStyle>());

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	private volatile Bookmark myBookmark;

	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();

	private volatile BookmarksAdapter myThisBookAdapter;
	private final ZLResource myResource = ZLResource.resource("bookmarksView");

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(
				new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.bookmarks);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);
		myBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);
		if (myBook == null) {
			finish();
		}
		myBookmark = FBReaderIntents.getBookmarkExtra(getIntent());
	}

	@Override
	protected void onStart() {
		super.onStart();
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myThisBookAdapter = new BookmarksAdapter((ListView) findViewById(R.id.bookmarks_this_book),
						myBookmark != null, BookmarksActivity.this, myComparator, myResource, myStyles, myCollection,
						myBookmark);
				myCollection.addListener(BookmarksActivity.this);
				updateStyles();
				loadBookmarks();
			}
		});
		OrientationUtil.setOrientation(this, getIntent());
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
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);

		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
		final BookmarksAdapter adapter;
		adapter = myThisBookAdapter;
		final Bookmark bookmark = adapter.getItem(position);
		switch (item.getItemId()) {
		case OPEN_ITEM_ID:
			gotoBookmark(bookmark);
			return true;
		case EDIT_ITEM_ID:
			final Intent intent = new Intent(this, EditBookmarkActivity.class);
			FBReaderIntents.putBookmarkExtra(intent, bookmark);
			OrientationUtil.startActivity(this, intent);
			return true;
		case DELETE_ITEM_ID:
			myCollection.deleteBookmark(bookmark);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.BookId);
		if (book != null) {
			FBReader.openBookActivity(this, book, bookmark);
		} else {
			UIMessageUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
		default:
			break;
		case BookmarkStyleChanged:
			runOnUiThread(new Runnable() {
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

	@Override
	public void onBuildEvent(Status status) {
		// TODO Auto-generated method stub

	}
}
