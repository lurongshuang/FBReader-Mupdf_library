package com.artifex.mupdfdemo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.artifex.mupdfdemo.bookmark.BookMark;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.geometerplus.zlibrary.ui.android.R;

class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity {
	/* The core rendering instance */
	enum TopBarMode {
		Main, Search, Annot, Delete, More, Accept
	};

	enum AcceptMode {
		Highlight, Underline, StrikeOut, Ink, CopyText
	};

	private final int OUTLINE_REQUEST = 0;
	private final int PRINT_REQUEST = 1;
	private MuPDFCore core;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;
	private EditText mPasswordView;
	private TextView mFilenameView;
	private SeekBar mPageSlider;
	public static int mPageSliderRes;
	private TextView mPageNumberView;
	private TextView mInfoView;
	// private ImageButton mOutlineButton;
	private TextView mAnnotTypeText;
	private ImageButton mAnnotButton;
	private ViewAnimator mTopBarSwitcher;
	private TopBarMode mTopBarMode = TopBarMode.Main;
	private AcceptMode mAcceptMode;
	private ImageButton mSearchBack;
	private ImageButton mSearchFwd;
	private EditText mSearchText;
	private SearchTask mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private boolean mLinkHighlight = false;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive = false;
	private boolean mReflow = false;
	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;

	private ImageButton mLinkButton;
	// private ImageButton mReflowButton;

	private LinearLayout ln_Bottom;
	private Button bt_look_Sty;
	private SeekBar sb_nowLig;
	private SharedPreferences sp;
	private Button bt_bookmark;

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid
		// stalling
		// the UI. Some calls can lead to javascript-invoked requests to display
		// an
		// alert dialog and collect a reply from the user. The task has to be
		// blocked
		// until the user's reply is received. This method creates an
		// asynchronous task,
		// the purpose of which is to wait of these requests and produce the
		// dialog
		// in response, while leaving the core blocked. When the dialog receives
		// the
		// user's response, it is sent to the core via replyToAlert, unblocking
		// it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1:
								index = 0;
								break;
							case AlertDialog.BUTTON2:
								index = 1;
								break;
							case AlertDialog.BUTTON3:
								index = 2;
								break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it
							// can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next
							// alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType) {
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[]) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(this, buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();

			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file
					// manager
					// Hopefully other file managers will use this same scheme,
					// if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri, new String[] { "_data" }, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String reason = null;
						if (str == null) {
							try {
								InputStream is = getContentResolver().openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							} catch (java.lang.OutOfMemoryError e) {
								System.out.println("Out of memory during buffer reading");
								reason = e.toString();
							} catch (Exception e) {
								reason = e.toString();
							}
							if (reason != null) {
								buffer = null;
								Resources res = getResources();
								AlertDialog alert = mAlertBuilder.create();
								setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
								alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										});
								alert.show();
								return;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				requestPassword(savedInstanceState);
				return;
			}
			if (core != null && core.countPages() == 0) {
				core = null;
			}
		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}

		createUI(savedInstanceState);
	}

	public void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(this);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (core.authenticatePassword(mPasswordView.getText().toString())) {
					createUI(savedInstanceState);
				} else {
					requestPassword(savedInstanceState);
				}
			}
		});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.show();
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d / %d", i + 1, core.countPages()));
				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}

			@Override
			protected void onHit(Hit item) {
				switch (mTopBarMode) {
				case Annot:
					if (item == Hit.Annotation) {
						showButtons();
						mTopBarMode = TopBarMode.Delete;
						mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					}
					break;
				case Delete:
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, core));

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Set the file-name text
		mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
				// 数据库对比 是否为书签
				SQLiteDatabase db = openDBOrTable();
				String[] values = new String[2];
				values[0] = mFileName;
				values[1] = progress + "";
				List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
				lists = DataBaseManager.getDB().querysql("select * from bookmark where bookName=? and bookNum=?",
						values, db);
				if (lists.size() > 0) {
					mLinkButton.setImageResource(R.drawable.book_mark_select);
				} else {
					mLinkButton.setImageResource(R.drawable.book_mark);
				}
			}
		});

		// Activate the search-preparing button
		// mSearchButton.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// searchModeOn();
		// }
		// });

		if (core.fileFormat().startsWith("PDF")) {
			mAnnotButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
				}
			});
		} else {
			mAnnotButton.setVisibility(View.GONE);
		}

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				setButtonEnabled(mSearchBack, haveText);
				setButtonEnabled(mSearchFwd, haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null
						&& !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
					search(1);
				return false;
			}
		});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		// 点击书签
		mLinkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SQLiteDatabase db = openDBOrTable();
				String[] values = new String[2];
				values[0] = mFileName;
				values[1] = mPageSlider.getProgress() + "";
				List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
				lists = DataBaseManager.getDB().querysql("select * from bookmark where bookName=? and bookNum=?",
						values, db);
				if (lists.size() > 0) {
					DataBaseManager.getDB().Exesql("delete from bookmark where bookName=? and bookNum=?", values, db);
					mLinkButton.setImageResource(R.drawable.book_mark);
				} else {
					DataBaseManager.getDB().Exesql("insert into bookmark(bookName,bookNum) values(?,?)", values, db);
					mLinkButton.setImageResource(R.drawable.book_mark_select);
				}

			}
		});

		// 横屏or竖屏观看
		bt_look_Sty.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int mCurrentOrientation = getResources().getConfiguration().orientation;
				if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
					// 竖屏
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

				} else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
					// 横屏
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		});

		// 更改阅读亮度
		sb_nowLig.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setBrightness(MuPDFActivity.this, progress);
			}
		});

		// 点击跳转
		bt_bookmark.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				OutlineItem outline[] = core.getOutline();
				if (outline != null) {
					OutlineActivityData.get().items = outline;
					OutlineActivityData.get().index = mDocView.getDisplayedViewIndex();

				}
				Intent intent = new Intent(MuPDFActivity.this, BookMark.class);
				intent.putExtra("mFileName", mFileName.toString());
				intent.putExtra("progress", mPageSlider.getProgress() + "");
				startActivityForResult(intent, OUTLINE_REQUEST);
				 overridePendingTransition(R.anim.pdf_zoomin, R.anim.pdf_zoomout);
			}
		});

		// 跳转书签列表
		// mReflowButton.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// SQLiteDatabase db = openDBOrTable();
		// String[] values = new String[1];
		// values[0] = mFileName;
		// List<Map<String, Object>> lists = new ArrayList<Map<String,
		// Object>>();
		// lists = DataBaseManager.getDB().querysql("select * from bookmark
		// where bookName=?", values, db);
		// if (lists.size() > 0) {
		// Intent intent =new Intent(MuPDFActivity.this,MarkLineActivity.class);
		// intent.putExtra("mFileName", mFileName.toString());
		// intent.putExtra("progress", mPageSlider.getProgress()+"");
		// startActivityForResult(intent, 110);
		// }
		// else
		// {
		// Toast.makeText(getApplicationContext(), "暂无收藏书签",
		// Toast.LENGTH_SHORT).show();
		// }
		// }
		// });

		// if (core.hasOutline()) {
		// mOutlineButton.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// OutlineItem outline[] = core.getOutline();
		// if (outline != null) {
		// OutlineActivityData.get().items = outline;
		// Intent intent = new Intent(MuPDFActivity.this,
		// OutlineActivity.class);
		// startActivityForResult(intent, OUTLINE_REQUEST);
		// }
		// }
		// });
		// } else {
		// mOutlineButton.setVisibility(View.GONE);
		// }

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		// if (savedInstanceState == null ||
		// !savedInstanceState.getBoolean("ButtonsHidden", false))
		// showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
			reflowModeSet(true);

		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		setContentView(layout);
	}

	/**
	 * 获取屏幕的亮度
	 * 
	 * @param activity
	 * @return
	 */
	public static int getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * 设置亮度
	 * 
	 * @param activity
	 * @param brightness
	 */
	public static void setBrightness(Activity activity, int brightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		activity.getWindow().setAttributes(lp);
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// String message = newConfig.orientation ==
		// Configuration.ORIENTATION_LANDSCAPE ? "屏幕设置为：横屏" : "屏幕设置为：竖屏";

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		hideButtons();
		if (data != null && data.getIntExtra("code", -1) == 110) {
			if (resultCode > 0) {
				mDocView.setDisplayedViewIndex((resultCode + mPageSliderRes / 2) / mPageSliderRes);
				updatePageNumView((resultCode + mPageSliderRes / 2) / mPageSliderRes);
			}
		} else {
			switch (requestCode) {
			case OUTLINE_REQUEST:
				if (resultCode >= 0)
					mDocView.setDisplayedViewIndex(resultCode);
				break;
			case PRINT_REQUEST:
				if (resultCode == RESULT_CANCELED)
					showInfo(getString(R.string.print_failed));
				break;
			}
		}
	}

	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow) {
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, core));
		setButtonEnabled(mAnnotButton, !reflow);
		if (reflow)
			setLinkHighlight(false);
		mDocView.refresh(mReflow);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search)
			outState.putBoolean("SearchMode", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchTask != null)
			mSearchTask.stop();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}

	public void onDestroy() {
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		super.onDestroy();
	}

	private void setButtonEnabled(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color.argb(255, 128, 128, 128));
	}

	private void setLinkHighlight(boolean highlight) {
		mLinkHighlight = highlight;
		// LINK_COLOR tint
		mDocView.setLinksEnabled(highlight);
	}

	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			mPageSlider.setProgress(index * mPageSliderRes);
			if (mTopBarMode == TopBarMode.Search) {
				mSearchText.requestFocus();
				showKeyboard();
			}
			fullscreen(false);
			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					ln_Bottom.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					ln_Bottom.setVisibility(View.VISIBLE);
				}
			});
			ln_Bottom.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);
			fullscreen(true);
			anim = new TranslateAnimation(0, 0, 0, ln_Bottom.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {

				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					ln_Bottom.setVisibility(View.INVISIBLE);
				}
			});
			ln_Bottom.startAnimation(anim);
		}
	}

	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			// Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	private void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d / %d", index + 1, core.countPages()));
	}

	private void printDoc() {
		if (!core.fileFormat().startsWith("PDF")) {
			showInfo(getString(R.string.format_currently_not_supported));
			return;
		}

		Intent myIntent = getIntent();
		Uri docUri = myIntent != null ? myIntent.getData() : null;

		if (docUri == null) {
			showInfo(getString(R.string.print_failed));
		}

		if (docUri.getScheme() == null)
			docUri = Uri.parse("file://" + docUri.toString());

		Intent printIntent = new Intent(this, PrintDialogActivity.class);
		printIntent.setDataAndType(docUri, "aplication/pdf");
		printIntent.putExtra("title", mFileName);
		startActivityForResult(printIntent, PRINT_REQUEST);
	}

	private void showInfo(String message) {
		mInfoView.setText(message);

		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			SafeAnimatorInflater safe = new SafeAnimatorInflater((Activity) this, R.animator.info, (View) mInfoView);
		} else {
			mInfoView.setVisibility(View.VISIBLE);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mInfoView.setVisibility(View.INVISIBLE);
				}
			}, 500);
		}
	}

	private void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.pdf_buttons, null);
		mFilenameView = (TextView) mButtonsView.findViewById(R.id.docNameText);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
		mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);
		mInfoView = (TextView) mButtonsView.findViewById(R.id.info);
		// mOutlineButton = (ImageButton)
		// mButtonsView.findViewById(R.id.outlineButton);
		mAnnotButton = (ImageButton) mButtonsView.findViewById(R.id.editAnnotButton);
		mAnnotTypeText = (TextView) mButtonsView.findViewById(R.id.annotType);
		mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
		mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.searchBack);
		mSearchFwd = (ImageButton) mButtonsView.findViewById(R.id.searchForward);
		mSearchText = (EditText) mButtonsView.findViewById(R.id.searchText);

		mLinkButton = (ImageButton) mButtonsView.findViewById(R.id.linkButton);
		// mReflowButton=(ImageButton)
		// mButtonsView.findViewById(R.id.reflowButton);
		ln_Bottom = (LinearLayout) mButtonsView.findViewById(R.id.ln_Bottom);
		bt_look_Sty = (Button) mButtonsView.findViewById(R.id.bt_look_Sty);
		bt_bookmark = (Button) mButtonsView.findViewById(R.id.bt_bookmark);

		mInfoView.setVisibility(View.INVISIBLE);
		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		// mPageSlider.setVisibility(View.INVISIBLE);
		ln_Bottom.setVisibility(View.INVISIBLE);
		
		mButtonsView.findViewById(R.id.topBar0Main).getBackground().setAlpha(250);
		ln_Bottom.getBackground().setAlpha(250);

		sb_nowLig = (SeekBar) mButtonsView.findViewById(R.id.sb_nowLig);
		sb_nowLig.setMax(255);

		// 初始化当前亮度
		sp = getSharedPreferences("is_eyes", Context.MODE_PRIVATE);
		int now_li = sp.getInt("now_li", -1);
		if (now_li >= 0) {
			sb_nowLig.setProgress(now_li);
			setBrightness(MuPDFActivity.this, now_li);
		} else {
			int size = getScreenBrightness(MuPDFActivity.this);
			sb_nowLig.setProgress(size);
		}
	}

	public void OnMoreButtonClick(View v) {
		mTopBarMode = TopBarMode.More;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelMoreButtonClick(View v) {
		mTopBarMode = TopBarMode.Main;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnPrintButtonClick(View v) {
		printDoc();
	}

	public void OnCopyTextButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.CopyText;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(getString(R.string.copy_text));
		showInfo(getString(R.string.select_text));
	}

	public void OnEditAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.More;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnHighlightButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Highlight;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.highlight);
		showInfo(getString(R.string.select_text));
	}

	public void OnUnderlineButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Underline;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.underline);
		showInfo(getString(R.string.select_text));
	}

	public void OnStrikeOutButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.StrikeOut;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.strike_out);
		showInfo(getString(R.string.select_text));
	}

	public void OnInkButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Ink;
		mDocView.setMode(MuPDFReaderView.Mode.Drawing);
		mAnnotTypeText.setText(R.string.ink);
		showInfo(getString(R.string.draw_annotation));
	}

	public void OnCancelAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectText();
			pageView.cancelDraw();
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		switch (mAcceptMode) {
		case CopyText:
			mTopBarMode = TopBarMode.More;
			break;
		default:
			mTopBarMode = TopBarMode.Annot;
			break;
		}
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		boolean success = false;
		switch (mAcceptMode) {
		case CopyText:
			if (pageView != null)
				success = pageView.copySelection();
			mTopBarMode = TopBarMode.More;
			showInfo(success ? getString(R.string.copied_to_clipboard) : getString(R.string.no_text_selected));
			break;

		case Highlight:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case Underline:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.UNDERLINE);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case StrikeOut:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case Ink:
			if (pageView != null)
				success = pageView.saveDraw();
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.nothing_to_save));
			break;
		}
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
	}

	public void OnCancelSearchButtonClick(View v) {
		searchModeOff();
	}

	public void OnDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null)
			pageView.deleteSelectedAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null)
			pageView.deselectAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	private void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		try {
			if(sp!=null)
			{
				sp.edit().putInt("now_li", sb_nowLig.getProgress()).commit();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (core!=null&&core.hasChanges()) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == AlertDialog.BUTTON_POSITIVE)
						core.save();
					finish();
				}
			};
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle("MuPDF");
			alert.setMessage(getString(R.string.document_has_changes_save_them_));
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
			alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
			alert.show();
		} else {
			super.onBackPressed();
		}
	}

	private void fullscreen(boolean enable) {
		if (getWindow() == null) {
			return;
		}
		if (enable) { // 隐藏状态栏
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(lp);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else { // 显示状态栏
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(lp);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}
}
