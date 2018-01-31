package org.newfunction;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.FBReaderMainActivity;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.PackageUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewEnums.Animation;
import org.geometerplus.zlibrary.ui.android.R;
import org.newfunction.catalog_bookmark.Ctatalog_Bookmark;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SwitchFragment extends Fragment {

	private LinearLayout groupViewLl;
	private ViewPager viewPager;
	private ImageView[] imageViews;
	private ImageView imageView;
	private FBReaderApp myReader;
	private List<View> viewList = new ArrayList<View>();
	private LayoutInflater mInflater;
	private FBReaderMainActivity activity;
	private ZLIntegerRangeOption option;
	private Button bt_bagcf;
	private Button bt_bagc9;
	private Button bt_bagbf;
	private Button bt_bag8a;
	private Button bt_bag29;
	private Button bt_bag59;

	private Button bt_fangz;
	private Button bt_yid;
	private Button bt_huad;
	private Button bt_none;

	private SharedPreferences sp;
	private ImageView im_eyes;
	private TextView eyes_text;

	private ImageView im_page_num;
	private TextView te_page_num;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View switchView = inflater.inflate(R.layout.fragment_switch, container, false);
		mInflater = inflater;
		viewPager = (ViewPager) switchView.findViewById(R.id.viewPager);
		groupViewLl = (LinearLayout) switchView.findViewById(R.id.viewGroup);

		return switchView;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (myReader == null) {
			myReader = (FBReaderApp) FBReaderApp.Instance();
		}
		if (activity == null) {
			activity = (FBReaderMainActivity) getActivity();
		}
		/**
		 * 将需要滑动的View加入到viewList
		 */
		View oneView = mInflater.inflate(R.layout.view_one, null);
		View twoView = mInflater.inflate(R.layout.view_two, null);
		oneView.getBackground().setAlpha(249);
		twoView.getBackground().setAlpha(249);
		viewList.add(oneView);
		viewList.add(twoView);
		/**
		 * 定义个圆点滑动导航ImageView，根据View的个数而定
		 */
		imageViews = new ImageView[viewList.size()];
		// ------------------------------------------------------------------------------------------------------
		// 初始化亮度样式
		SeekBar sk_seekbar = (SeekBar) oneView.findViewById(R.id.sk_seekbar);
		sk_seekbar.setMax(100);
		if (activity.getZLibrary().ScreenBrightnessLevelOption.getValue() == 0) {
			sk_seekbar.setProgress(myReader.getViewWidget().getScreenBrightness());
		} else {
			sk_seekbar.setProgress(activity.getZLibrary().ScreenBrightnessLevelOption.getValue());
		}
		// 改变亮度
		sk_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				myReader.getViewWidget().setScreenBrightness(progress);
			}
		});
		// 初始化字体大小样式
		Button bt_fontsizes = (Button) oneView.findViewById(R.id.bt_fontsizes);
		Button bt_fontsizeb = (Button) oneView.findViewById(R.id.bt_fontsizeb);
		final TextView te_font_size = (TextView) oneView.findViewById(R.id.te_font_size);
		if (option == null) {
			option = myReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
		}
		te_font_size.setText(option.getValue() + "");
		// 字体缩小
		bt_fontsizes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				option.setValue(option.getValue() - 2);
				te_font_size.setText(option.getValue() + "");
				myReader.clearTextCaches();
				myReader.getViewWidget().repaint();
			}
		});
		// 字体放大
		bt_fontsizeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				option.setValue(option.getValue() + 2);
				te_font_size.setText(option.getValue() + "");
				myReader.clearTextCaches();
				myReader.getViewWidget().repaint();
			}
		});

		// 改变阅读背景

		// 切换背景颜色
		bt_bagcf = (Button) oneView.findViewById(R.id.bt_bagcf);
		bt_bagc9 = (Button) oneView.findViewById(R.id.bt_bagc9);
		bt_bagbf = (Button) oneView.findViewById(R.id.bt_bagbf);
		bt_bag8a = (Button) oneView.findViewById(R.id.bt_bag8a);
		bt_bag29 = (Button) oneView.findViewById(R.id.bt_bag29);
		bt_bag59 = (Button) oneView.findViewById(R.id.bt_bag59);

		// 初始化背景颜色
		String StylecolorName = myReader.ViewOptions.ColorProfileName.getValue();
		// 当前主题
		if (StylecolorName.equals("colorcfcf")) {
			bt_bagcf.setBackgroundResource(R.drawable.buttoncfcfcf_2);
		} else if (StylecolorName.equals("colorc9")) {
			bt_bagc9.setBackgroundResource(R.drawable.buttonc9bfaf2_2);
		} else if (StylecolorName.equals("colorbf")) {
			bt_bagbf.setBackgroundResource(R.drawable.buttonbfa876_2);
		} else if (StylecolorName.equals("color8a")) {
			bt_bag8a.setBackgroundResource(R.drawable.button8ab991_2);
		} else if (StylecolorName.equals("color29")) {
			bt_bag29.setBackgroundResource(R.drawable.button294868_2);
		} else if (StylecolorName.equals("color59")) {
			bt_bag59.setBackgroundResource(R.drawable.button59473f2_2);
		} else {
			setReadBg("defaultLight");
			bt_bagc9.setBackgroundResource(R.drawable.buttonc9bfaf2_2);
		}

		// 颜色为 #cfcfcf
		bt_bagcf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("colorcfcf");
				bt_bagcf.setBackgroundResource(R.drawable.buttoncfcfcf_2);
			}
		});

		// 颜色为 #c9bfaf
		bt_bagc9.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("defaultLight");
				bt_bagc9.setBackgroundResource(R.drawable.buttonc9bfaf2_2);
			}
		});

		// 颜色为 bfa875
		bt_bagbf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("colorbf");
				bt_bagbf.setBackgroundResource(R.drawable.buttonbfa876_2);
			}
		});
		// 颜色为 8ab990
		bt_bag8a.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("color8a");
				bt_bag8a.setBackgroundResource(R.drawable.button8ab991_2);
			}
		});
		// 颜色为 294867
		bt_bag29.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("color29");
				bt_bag29.setBackgroundResource(R.drawable.button294868_2);
			}
		});
		// 颜色为 59473f
		bt_bag59.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setReadBg("color59");
				bt_bag59.setBackgroundResource(R.drawable.button59473f2_2);
			}
		});

		// 跳转目录书签
		ImageView im_cnlist = (ImageView) oneView.findViewById(R.id.im_cnlist);
		im_cnlist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent externalIntent = new Intent(FBReaderIntents.Action.EXTERNAL_BOOKMARKS);
				final Intent internalIntent = new Intent(getActivity(), Ctatalog_Bookmark.class);
				if (PackageUtil.canBeStarted(getActivity(), externalIntent, true)) {
					try {
						startBookmarksActivity(externalIntent);
					} catch (ActivityNotFoundException e) {
						startBookmarksActivity(internalIntent);
					}
				} else {
					startBookmarksActivity(internalIntent);
				}
			}
		});
		// -------------------------------------------------------------------------------------------
		bt_fangz = (Button) twoView.findViewById(R.id.bt_fangz);
		bt_yid = (Button) twoView.findViewById(R.id.bt_yid);
		bt_huad = (Button) twoView.findViewById(R.id.bt_huad);
		bt_none = (Button) twoView.findViewById(R.id.bt_none);
		// 获取当前翻页效果
		Animation animation = myReader.getCurrentView().getAnimationType();
		if (animation.toString().equals("curl")) {
			// 仿真
			initstylebg(bt_fangz);
		} else if (animation.toString().equals("shift")) {
			// 移动
			initstylebg(bt_yid);
		} else if (animation.toString().equals("slide")) {
			// 滑动
			initstylebg(bt_huad);
		} else if (animation.toString().equals("none")) {
			// 无
			initstylebg(bt_none);

		} else {
			// 选择默认
			initstylebg(bt_none);
			myReader.PageTurningOptions.Animation.setValue(ZLView.Animation.none);
		}

		// 仿真
		bt_fangz.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initstylebg(bt_fangz);
				myReader.PageTurningOptions.Animation.setValue(ZLView.Animation.curl);
			}
		});

		// 移动
		bt_yid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initstylebg(bt_yid);
				myReader.PageTurningOptions.Animation.setValue(ZLView.Animation.shift);
			}
		});

		// 滑动
		bt_huad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initstylebg(bt_huad);
				myReader.PageTurningOptions.Animation.setValue(ZLView.Animation.slide);
			}
		});

		// 无
		bt_none.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initstylebg(bt_none);
				myReader.PageTurningOptions.Animation.setValue(ZLView.Animation.none);
			}
		});

		// 护眼模式
		im_eyes = (ImageView) twoView.findViewById(R.id.im_eyes);
		eyes_text = (TextView) twoView.findViewById(R.id.eyes_text);

		sp = getActivity().getSharedPreferences("is_eyes", Context.MODE_PRIVATE);
		if (sp.getBoolean("is_eyes", false)) {
			im_eyes.setImageResource(R.drawable.eyes_select);
		} else {
			im_eyes.setImageResource(R.drawable.eyes);
		}
		// 设置护眼模式
		im_eyes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sp.getBoolean("is_eyes", false)) {
					im_eyes.setImageResource(R.drawable.eyes);
				} else {
					im_eyes.setImageResource(R.drawable.eyes_select);
				}
				FBReader.iseys();
			}
		});
		// 设置护眼模式
		eyes_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sp.getBoolean("is_eyes", false)) {
					im_eyes.setImageResource(R.drawable.eyes);
				} else {
					im_eyes.setImageResource(R.drawable.eyes_select);
				}
				FBReader.iseys();
			}
		});

		// 页码进度
		im_page_num = (ImageView) twoView.findViewById(R.id.im_page_num);
		te_page_num = (TextView) twoView.findViewById(R.id.te_page_num);
		int height = new ViewOptions().FooterHeight.getValue();
		if (height < 2) {
			im_page_num.setImageResource(R.drawable.page_num);
		} else {
			im_page_num.setImageResource(R.drawable.page_num_select);
		}

		// 页码进度
		im_page_num.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int height = new ViewOptions().FooterHeight.getValue();
				if (height < 2) {
					ZLIntegerRangeOption viewOptions = new ViewOptions().FooterHeight;
					viewOptions.setValue(30);
					im_page_num.setImageResource(R.drawable.page_num_select);
				} else {
					ZLIntegerRangeOption viewOptions = new ViewOptions().FooterHeight;
					viewOptions.setValue(0);
					im_page_num.setImageResource(R.drawable.page_num);
				}

			}
		});
		// 页面进度
		te_page_num.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int height = new ViewOptions().FooterHeight.getValue();
				if (height < 2) {
					ZLIntegerRangeOption viewOptions = new ViewOptions().FooterHeight;
					viewOptions.setValue(30);
					im_page_num.setImageResource(R.drawable.page_num_select);
				} else {
					ZLIntegerRangeOption viewOptions = new ViewOptions().FooterHeight;
					viewOptions.setValue(0);
					im_page_num.setImageResource(R.drawable.page_num);
				}

			}
		});

		for (int i = 0; i < viewList.size(); i++) {
			imageView = new ImageView(this.getActivity());
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(20, 0, 20, 0);
			imageViews[i] = imageView;

			if (i == 0) {
				// 默认选中第一张图片
				imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.page_indicator);
			}

			groupViewLl.addView(imageViews[i]);
		}

		viewPager.setAdapter(new meunPagerAdapter(viewList));
		viewPager.setOnPageChangeListener(new SwitchPageChangeListener());
	}

	private void startBookmarksActivity(Intent intent) {
		FBReaderIntents.putBookExtra(intent, myReader.getCurrentBook());
		FBReaderIntents.putBookmarkExtra(intent, myReader.createBookmark(80, true));
		OrientationUtil.startActivity(getActivity(), intent);
		getActivity().overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
	}

	/**
	 * 设置阅读界面颜色
	 * 
	 * @param color
	 */
	public void setReadBg(String colorName) {
		initbtbag();
		myReader.ViewOptions.ColorProfileName.setValue(colorName);
		myReader.getViewWidget().reset();
		myReader.getViewWidget().repaint();
	}

	public void initbtbag() {
		bt_bagcf.setBackgroundResource(R.drawable.buttoncfcfcf);
		bt_bagc9.setBackgroundResource(R.drawable.buttonc9bfaf);
		bt_bagbf.setBackgroundResource(R.drawable.buttonbfa875);
		bt_bag8a.setBackgroundResource(R.drawable.button8ab990);
		bt_bag29.setBackgroundResource(R.drawable.button294867);
		bt_bag59.setBackgroundResource(R.drawable.button59473f);
	}

	public void initstylebg(Button bt) {
		bt_fangz.setBackgroundResource(R.drawable.buttonflipw);
		bt_fangz.setTextColor(Color.parseColor("#ffffff"));
		bt_yid.setBackgroundResource(R.drawable.buttonflipw);
		bt_yid.setTextColor(Color.parseColor("#ffffff"));
		bt_huad.setBackgroundResource(R.drawable.buttonflipw);
		bt_huad.setTextColor(Color.parseColor("#ffffff"));
		bt_none.setBackgroundResource(R.drawable.buttonflipw);
		bt_none.setTextColor(Color.parseColor("#ffffff"));
		bt.setBackgroundResource(R.drawable.buttonflip);
		bt.setTextColor(Color.parseColor("#12bfc2"));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// 指引页面更改事件监听器
	class SwitchPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0].setBackgroundResource(R.drawable.page_indicator_focused);

				if (arg0 != i) {
					imageViews[i].setBackgroundResource(R.drawable.page_indicator);
				}
			}
		}
	}

}
