package com.ypyproductions.musicplayer;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ypyproductions.abtractclass.fragment.IDBFragmentConstants;
import com.ypyproductions.materialdialogs.MaterialDialog;
import com.ypyproductions.materialdialogs.MaterialDialog.ButtonCallback;
import com.ypyproductions.materialdialogs.MaterialDialog.ListCallback;
import com.ypyproductions.musicplayer.adapter.DBSlidingTripAdapter;
import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.dataMng.TotalDataManager;
import com.ypyproductions.musicplayer.fragment.FragmentHome;
import com.ypyproductions.musicplayer.fragment.FragmentPlaylist;
import com.ypyproductions.musicplayer.object.DBImageLoader;
import com.ypyproductions.musicplayer.object.PlaylistObject;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.musicplayer.playerservice.IMusicConstant;
import com.ypyproductions.musicplayer.setting.ISettingConstants;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.musicplayer.view.PagerSlidingTabStrip;
import com.ypyproductions.musicplayer.view.SliderView;
import com.ypyproductions.musicplayer.view.SliderView.OnValueChangedListener;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.DirectionUtils;
import com.ypyproductions.utils.ResolutionUtils;
import com.ypyproductions.utils.ShareActionUtils;
import com.ypyproductions.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends DBFragmentActivity implements IDBFragmentConstants, ISettingConstants, IMusicConstant {
	public static final String TAG = MainActivity.class.getSimpleName();

	private SearchView searchView;
	TrackObject mTrackObject;
	public boolean isFirstTime;

	public DisplayImageOptions mImgTrackOptions;

	private AdView adView;

	public DisplayImageOptions mAvatarOptions;

	private ViewPager mViewPager;

	private PagerSlidingTabStrip mPagerTabStrip;

	private ArrayList<Fragment> mListFragments = new ArrayList<Fragment>();
	private ArrayList<String> mListTitle = new ArrayList<String>();

	private DBSlidingTripAdapter mTabAdapters;

	private RelativeLayout mLayoutPlayMusic;

	private SliderView mSeekbar;

	private TextView mTvCurrentTime;

	private TextView mTvDuration;

	private TextView mTvTitleSongs;

	private Button mBtnPlay;

	private Button mBtnClose;

	private Button mBtnPrev;

	private Button mBtnNext;

	protected ProgressDialog progressDialog;

	private TrackObject mCurrentTrack;

	private TextView mTvUserName;

	private TextView mTvTime;

	private Date mDate;

	private ImageView mImgTrack;

	private RelativeLayout mLayoutControl;

	private LayoutParams mTopLayoutParams;
	private LayoutParams mBottomLayoutParams;

	private RelativeLayout mLayoutSmallMusic;

	private Button mBtnSmallPlay;

	private Button mBtnSmallNext;

	private TextView mTvSmallSong;

	private ImageView mImgSmallSong;

	private LayoutParams mBottomSmallLayoutParams;

	private LayoutParams mTopSmallLayoutParams;

	private MusicPlayerBroadcast mPlayerBroadcast;

	private String[] mListStr;

	private CheckBox mCbShuffe;

	private CheckBox mCbRepeat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(toolbar);

		SettingManager.setFirstTime(this, true);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).memoryCacheExtraOptions(400, 400).diskCacheExtraOptions(400, 400, null)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024).imageDownloader(new DBImageLoader(this)).tasksProcessingOrder(QueueProcessingType.FIFO).writeDebugLogs().build();
		ImageLoader.getInstance().init(config);

		this.mImgTrackOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.music_note).resetViewBeforeLoading(false).cacheInMemory(true).cacheOnDisk(true)
				.considerExifParams(true).build();

		this.mAvatarOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_account_circle_grey).resetViewBeforeLoading(false).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).build();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mPagerTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pagertabstrip);
		mPagerTabStrip.setVisibility(View.GONE);
		mPagerTabStrip.setIndicatorHeight(getResources().getDimensionPixelOffset(R.dimen.height_indicator_tabs));
		mPagerTabStrip.setIndicatorColor(getResources().getColor(R.color.tab_indicator_color));
		mPagerTabStrip.setBackgroundColor(getResources().getColor(R.color.main_color_tab));
		mPagerTabStrip.setTextColor(getResources().getColor(R.color.white));

		setUpPlayMusicLayout();
		setUpSmallMusicLayout();

		setUpLayoutAdmob();

		mDate = new Date();

		registerPlayerBroadCastReceiver();
		createTab();
		handleIntent(getIntent());

	}

	private void setUpSmallMusicLayout() {
		mLayoutSmallMusic = (RelativeLayout) findViewById(R.id.layout_child_listen);
		mBtnSmallPlay = (Button) findViewById(R.id.btn_small_play);
		mBtnSmallNext = (Button) findViewById(R.id.btn_small_next);
		Button mBtnSmallClose = (Button) findViewById(R.id.btn_small_close);
		mBtnSmallClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHiddenPlay(true);
			}
		});
		mLayoutSmallMusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLayoutPlayMusic.getVisibility() != View.VISIBLE) {
					mLayoutPlayMusic.setVisibility(View.VISIBLE);
				}
			}
		});
		mBtnSmallPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onProcessPausePlayAction();
			}
		});
		mBtnSmallNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextTrack();
			}
		});
		Button mBtnEqua = (Button) findViewById(R.id.btn_equalizer);
		mBtnEqua.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(MainActivity.this, EqualizerActivity.class);
				DirectionUtils.changeActivity(MainActivity.this, R.anim.slide_in_from_right, R.anim.slide_out_to_left, false, mIntent);
			}
		});
		mTvSmallSong = (TextView) findViewById(R.id.tv_small_song);
		mImgSmallSong = (ImageView) findViewById(R.id.img_small_track);

		mTopSmallLayoutParams = (LayoutParams) mLayoutSmallMusic.getLayoutParams();

		mBottomSmallLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) ResolutionUtils.convertDpToPixel(this, 70));
		mBottomSmallLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	}

	private void setUpPlayMusicLayout() {
		mLayoutPlayMusic = (RelativeLayout) findViewById(R.id.layout_listen_music);
		mSeekbar = (SliderView) findViewById(R.id.seekBar1);
		this.mSeekbar.setOnValueChangedListener(new OnValueChangedListener() {
			
			@Override
			public void onValueChanged(int value) {
				int currentPos = (int) (value * mCurrentTrack.getDuration() / 100f);
				DBLog.d(TAG, "=================>currentPos=" + currentPos);
				seekAudio(currentPos);
			}
		});

		mLayoutPlayMusic.findViewById(R.id.img_bg).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		

		mLayoutControl = (RelativeLayout) findViewById(R.id.layout_control);
		mTopLayoutParams = (LayoutParams) mLayoutControl.getLayoutParams();
		mBottomLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) ResolutionUtils.convertDpToPixel(this, 60));
		mBottomLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		mTvUserName = (TextView) findViewById(R.id.tv_username);
		mTvUserName.setTypeface(mTypefaceBold);

		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvTime.setTypeface(mTypefaceLight);

		mImgTrack = (ImageView) findViewById(R.id.img_track);

		mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
		mTvCurrentTime.setTypeface(mTypefaceLight);

		mTvDuration = (TextView) findViewById(R.id.tv_duration);
		mTvDuration.setTypeface(mTypefaceLight);

		mTvTitleSongs = (TextView) findViewById(R.id.tv_song);
		mTvTitleSongs.setTypeface(mTypefaceBold);

		mBtnPlay = (Button) findViewById(R.id.btn_play);
		mBtnClose = (Button) findViewById(R.id.btn_close);
		mBtnPrev = (Button) findViewById(R.id.btn_prev);
		mBtnNext = (Button) findViewById(R.id.btn_next);

		mCbShuffe = (CheckBox) findViewById(R.id.cb_shuffle);
		mCbShuffe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingManager.setShuffle(MainActivity.this, mCbShuffe.isChecked());
			}
		});
		mCbShuffe.setChecked(SettingManager.getShuffle(this));

		mCbRepeat = (CheckBox) findViewById(R.id.cb_repeat);
		mCbRepeat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingManager.setRepeat(MainActivity.this, mCbRepeat.isChecked());
			}
		});
		mCbRepeat.setChecked(SettingManager.getRepeat(this));

		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextTrack();
			}
		});

		mBtnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prevTrack();
			}
		});

		mBtnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onProcessPausePlayAction();
			}
		});
		mBtnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHiddenPlay(false);
			}
		});
		findViewById(R.id.img_add_playlist).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrentTrack != null) {
					showDialogPlaylist(mCurrentTrack);
				}
			}
		});

	}

	protected void prevTrack() {
		Intent i = new Intent(getPackageName() + ACTION_PREVIOUS);
		startService(i);
	}

	protected void nextTrack() {
		Intent i = new Intent(getPackageName() + ACTION_NEXT);
		startService(i);
	}

	private void seekAudio(int currentPos) {
		Intent mIntent = new Intent(getPackageName() + ACTION_SEEK);
		mIntent.putExtra(KEY_POSITION, currentPos);
		startService(mIntent);
	}

	private void onProcessPausePlayAction() {
		boolean b = SoundCloundDataMng.getInstance().setCurrentIndex(mCurrentTrack);
		if (b) {
			Intent i = new Intent(getPackageName() + ACTION_TOGGLE_PLAYBACK);
			startService(i);
		}
	}

	public void setInfoForPlayingTrack(final TrackObject mTrackObject, boolean isNeedShowFul, boolean isAutoPlay) {
		mCurrentTrack = mTrackObject;
		mLayoutPlayMusic.setVisibility(isNeedShowFul ? View.VISIBLE : View.GONE);
		if (mLayoutSmallMusic.getVisibility() != View.VISIBLE) {
			mLayoutSmallMusic.setVisibility(View.VISIBLE);
		}
		mLayoutSmallMusic.setBackgroundColor(getResources().getColor(R.color.main_color));

		mTvUserName.setText(mTrackObject.getUsername());
		Date mTrackDate = mTrackObject.getCreatedDate();
		if (mTrackDate != null) {
			String mTime = getStringTimeAgo(this, (mDate.getTime() - mTrackDate.getTime()) / 1000);
			mTvTime.setText(mTime);
		}

		Uri mUri = mTrackObject.getURI();
		if (mUri != null) {
			String uri = mUri.toString();
			ImageLoader.getInstance().displayImage(uri, mImgTrack, mImgTrackOptions);
			ImageLoader.getInstance().displayImage(uri, mImgSmallSong, mImgTrackOptions);
		}
		else {
			mImgTrack.setImageResource(R.drawable.music_note);
			mImgSmallSong.setImageResource(R.drawable.ic_music_default);
		}

		mTvTitleSongs.setText(mTrackObject.getTitle());
		mTvSmallSong.setText(mTrackObject.getTitle());

		mTvCurrentTime.setText("00:00");
		this.mSeekbar.setValue(0);

		long duration = mTrackObject.getDuration() / 1000;
		String minute = String.valueOf((int) (duration / 60));
		String seconds = String.valueOf((int) (duration % 60));
		if (minute.length() < 2) {
			minute = "0" + minute;
		}
		if (seconds.length() < 2) {
			seconds = "0" + seconds;
		}
		mTvDuration.setText(minute + ":" + seconds);
		if (isAutoPlay) {
			TrackObject mCurrentTrack = SoundCloundDataMng.getInstance().getCurrentTrackObject();
			boolean isPlayingTrack = (mCurrentTrack != null && mCurrentTrack.getId() == mTrackObject.getId());
			DBLog.d(TAG, "=======================>isPlayingTrack=" + isPlayingTrack);
			if (!isPlayingTrack) {
				boolean b = SoundCloundDataMng.getInstance().setCurrentIndex(mTrackObject);
				if (b) {
					Intent i = new Intent(getPackageName() + ACTION_PLAY);
					startService(i);
				}
			}
			else {
				MediaPlayer mMediaPlayer = SoundCloundDataMng.getInstance().getPlayer();
				if (mMediaPlayer != null) {
					onUpdateStatePausePlay(mMediaPlayer.isPlaying());
				}
				else {
					onUpdateStatePausePlay(false);
					boolean b = SoundCloundDataMng.getInstance().setCurrentIndex(mTrackObject);
					if (b) {
						Intent i = new Intent(getPackageName() + ACTION_PLAY);
						startService(i);
					}
				}
			}
		}
	}

	private void onHiddenPlay(boolean isStop) {
		if (isStop) {
			mBtnPlay.setVisibility(View.VISIBLE);
			mBtnPlay.setBackgroundResource(R.drawable.ic_pause);

			mBtnSmallPlay.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp);
			mLayoutSmallMusic.setVisibility(View.GONE);

			Intent i = new Intent(getPackageName() + ACTION_STOP);
			startService(i);
		}
		mLayoutPlayMusic.setVisibility(View.GONE);
	}

	private void setUpLayoutAdmob() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_ad);
		if (ApplicationUtils.isOnline(this)) {
			boolean b=SHOW_ADVERTISEMENT;
			if (b) {
				adView = new AdView(this);
				adView.setAdUnitId(ADMOB_ID_BANNER);
				adView.setAdSize(AdSize.SMART_BANNER);

				layout.addView(adView);
				AdRequest mAdRequest = new AdRequest.Builder().build();
				adView.loadAd(mAdRequest);

				mLayoutSmallMusic.setLayoutParams(mTopSmallLayoutParams);
			}
			else {
				layout.setVisibility(View.GONE);
				mLayoutSmallMusic.setLayoutParams(mBottomSmallLayoutParams);
			}
		}
		else {
			layout.setVisibility(View.GONE);
			mLayoutSmallMusic.setLayoutParams(mBottomSmallLayoutParams);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			processSearchData(query);
		}
	}

	@Override
	public void onDestroyData() {
		super.onDestroyData();
		showIntertestialAds();

		startService(new Intent(getPackageName() + ACTION_STOP));

		SoundCloundDataMng.getInstance().onDestroy();
		TotalDataManager.getInstance().onDestroy();

		SettingManager.setOnline(this, false);
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearDiskCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPlayerBroadcast != null) {
			unregisterReceiver(mPlayerBroadcast);
			mPlayerBroadcast = null;
		}
		if (mListFragments != null) {
			mListFragments.clear();
			mListFragments = null;
		}
		ImageLoader.getInstance().stop();
	}

	public void processSearchData(final String query) {
		if (!StringUtils.isEmptyString(query)) {
			onHiddenPlay(false);
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					FragmentHome mFragmentSearch = getFragmentHome();
					if (mFragmentSearch != null) {
						mViewPager.setCurrentItem(LIBRARY_INDEX, true);
						mFragmentSearch.startGetData(query);
					}
				}
			});
		}
	}

	private FragmentPlaylist getFragmentPlaylist() {
		if (mListFragments != null && mListFragments.size() > 0) {
			Fragment mFragment = mListFragments.get(PLAYLIST_INDEX);
			if (mFragment instanceof FragmentPlaylist) {
				return (FragmentPlaylist) mFragment;
			}
		}
		return null;
	}

	private FragmentHome getFragmentHome() {
		if (mListFragments != null && mListFragments.size() > 0) {
			Fragment mFragment = mListFragments.get(LIBRARY_INDEX);
			if (mFragment instanceof FragmentHome) {
				return (FragmentHome) mFragment;
			}
		}
		return null;
	}

	private void createTab() {
		mPagerTabStrip.setVisibility(View.VISIBLE);

		Fragment mFragmentLibrary = Fragment.instantiate(this, FragmentHome.class.getName(), null);
		mListFragments.add(mFragmentLibrary);
		mListTitle.add(getString(R.string.title_home).toUpperCase(Locale.US));

		Fragment mFragmentPlaylist = Fragment.instantiate(this, FragmentPlaylist.class.getName(), null);
		mListFragments.add(mFragmentPlaylist);
		mListTitle.add(getString(R.string.title_playlist).toUpperCase(Locale.US));

		mTabAdapters = new DBSlidingTripAdapter(getSupportFragmentManager(), mListFragments, mListTitle);
		mViewPager.setAdapter(mTabAdapters);
		mPagerTabStrip.setViewPager(mViewPager);
		mPagerTabStrip.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.setCurrentItem(LIBRARY_INDEX, false);
		mViewPager.setOffscreenPageLimit(2);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (searchView != null && !searchView.isIconified()) {
				searchView.setIconified(true);
				return true;
			}
			boolean b = hideLayoutPlay();
			if (b) {
				return b;
			}
			if (mViewPager.getCurrentItem() != 0) {
				mViewPager.setCurrentItem(0, true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}


	private void showDiaglogAboutUs() {
		MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
		mBuilder.backgroundColor(getResources().getColor(R.color.white));
		mBuilder.title(R.string.title_about_us);
		mBuilder.titleColor(getResources().getColor(R.color.black_text));
		mBuilder.items(R.array.list_share);
		mBuilder.contentColor(getResources().getColor(R.color.black_text));
		mBuilder.positiveColor(getResources().getColor(R.color.main_color));
		mBuilder.negativeColor(getResources().getColor(R.color.black_secondary_text));
		mBuilder.positiveText(R.string.title_cancel);
		mBuilder.autoDismiss(true);
		mBuilder.typeface(mTypefaceBold, mTypefaceLight);
		mBuilder.itemsCallback(new ListCallback() {
			
			@Override
			public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
				if (which == 0) {
					ShareActionUtils.shareViaEmail(MainActivity.this, YOUR_EMAIL_CONTACT, "", "");
				}
				else if (which == 1) {
					Intent mIntent = new Intent(MainActivity.this, ShowUrlActivity.class);
					mIntent.putExtra(KEY_URL, URL_YOUR_WEBSITE);
					mIntent.putExtra(KEY_HEADER, getString(R.string.title_website));
					startActivity(mIntent);
				}
				else if (which == 2) {
					Intent mIntent = new Intent(MainActivity.this, ShowUrlActivity.class);
					mIntent.putExtra(KEY_URL, URL_YOUR_FACE_BOOK);
					mIntent.putExtra(KEY_HEADER, getString(R.string.title_facebook));
					startActivity(mIntent);
				}
				else if (which == 3) {
					String url = String.format(URL_FORMAT_LINK_APP, getPackageName());
					ShareActionUtils.goToUrl(MainActivity.this, url);
				}
			}
		});
		//mBuilder.build().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String keyword) {
				hiddenVirtualKeyBoard();
				processSearchData(keyword);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String keyword) {
				startSuggestion(keyword);
				return true;
			}
		});
		searchView.setOnSearchClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(LIBRARY_INDEX, true);
				FragmentHome mFragmentSearch = getFragmentHome();
				if (mFragmentSearch != null) {
					mFragmentSearch.setUpFullData();
				}
			}
		});
		searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				FragmentHome mFragmentSearch = getFragmentHome();
				if (mFragmentSearch != null) {
					mFragmentSearch.setUpFullData();
				}
				return false;
			}
		});
		searchView.setQueryHint(getString(R.string.title_search));
		searchView.setSubmitButtonEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_info:
			showDiaglogAboutUs();
			break;
		case R.id.action_search:
			mViewPager.setCurrentItem(LIBRARY_INDEX, true);
			searchView.setIconified(false);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void hiddenVirtualKeyBoard() {
		if (searchView != null && !searchView.isIconified()) {
			searchView.setQuery("", false);
			searchView.clearFocus();
			searchView.setIconified(true);
			ApplicationUtils.hiddenVirtualKeyboard(this, searchView);
		}
	}

	private void startSuggestion(final String search) {
		if (!StringUtils.isEmptyString(search)) {
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					FragmentHome mFragmentSearch = getFragmentHome();
					if (mFragmentSearch != null) {
						mViewPager.setCurrentItem(LIBRARY_INDEX, true);
						mFragmentSearch.startGetData(search);
					}
				}
			});
		}
	}

	public boolean hideLayoutPlay() {
		if (mLayoutPlayMusic.getVisibility() == View.VISIBLE) {
			onHiddenPlay(false);
			return true;
		}
		return false;
	}

	public void registerPlayerBroadCastReceiver() {
		if (mPlayerBroadcast != null) {
			return;
		}
		mPlayerBroadcast = new MusicPlayerBroadcast();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(getPackageName() + ACTION_BROADCAST_PLAYER);
		registerReceiver(mPlayerBroadcast, mIntentFilter);
	}

	private void onUpdateStatePausePlay(boolean isPlay) {
		mBtnPlay.setBackgroundResource(!isPlay ? R.drawable.ic_play : R.drawable.ic_pause);
		mBtnSmallPlay.setBackgroundResource(!isPlay ? R.drawable.ic_play_arrow_white_36dp : R.drawable.ic_pause_white_36dp);
	}

	private class MusicPlayerBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent != null) {
					String action = intent.getAction();
					if (!StringUtils.isEmptyString(action)) {
						String packageName = getPackageName();
						if (action.equals(packageName + ACTION_BROADCAST_PLAYER)) {
							String actionPlay = intent.getStringExtra(KEY_ACTION);
							if (!StringUtils.isEmptyString(actionPlay)) {
								if (actionPlay.equals(packageName + ACTION_NEXT)) {
									onUpdateStatePausePlay(false);
								}
								else if (actionPlay.equals(packageName + ACTION_LOADING)) {
									showProgressDialog();
								}
								else if (actionPlay.equals(packageName + ACTION_DIMISS_LOADING)) {
									dimissProgressDialog();
								}
								else if (actionPlay.equals(packageName + ACTION_PAUSE) || actionPlay.equals(packageName + ACTION_STOP)) {
									onUpdateStatePausePlay(false);
									if (actionPlay.equals(packageName + ACTION_STOP)) {
										onHiddenPlay(true);
									}
								}
								else if (actionPlay.equals(packageName + ACTION_PLAY)) {
									onUpdateStatePausePlay(true);
									TrackObject mTrackObject = SoundCloundDataMng.getInstance().getCurrentTrackObject();
									if (mTrackObject != null) {
										setInfoForPlayingTrack(mTrackObject, mLayoutPlayMusic.getVisibility() == View.VISIBLE ? true : false, false);
									}
								}
								else if (actionPlay.equals(packageName + ACTION_UPDATE_POS)) {
									int currentPos = intent.getIntExtra(KEY_POSITION, -1);
									if (currentPos > 0 && mCurrentTrack != null) {
										long duration = currentPos / 1000;
										String minute = String.valueOf((int) (duration / 60));
										String seconds = String.valueOf((int) (duration % 60));
										if (minute.length() < 2) {
											minute = "0" + minute;
										}
										if (seconds.length() < 2) {
											seconds = "0" + seconds;
										}
										mTvCurrentTime.setText(minute + ":" + seconds);
										int percent = (int) (((float) currentPos / (float) mCurrentTrack.getDuration()) * 100f);
										mSeekbar.setValue(percent);
									}
								}
							}
						}

					}
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createDialogPlaylist(final boolean isEdit, final PlaylistObject mPlaylistObject, final IDBCallback mCallback) {
		final EditText mEdPlaylistName = new EditText(this);
		if (isEdit) {
			mEdPlaylistName.setText(mPlaylistObject.getName());
		}
		MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
		mBuilder.backgroundColor(getResources().getColor(R.color.white));
		mBuilder.customView(mEdPlaylistName,false);
		mBuilder.title(R.string.title_playlist_name);
		mBuilder.titleColor(getResources().getColor(R.color.black_text));
		mBuilder.items(mListStr);
		mBuilder.contentColor(getResources().getColor(R.color.black_text));
		mBuilder.positiveColor(getResources().getColor(R.color.main_color));
		mBuilder.negativeColor(getResources().getColor(R.color.black_secondary_text));
		mBuilder.positiveText(R.string.title_save);
		mBuilder.negativeText(R.string.title_cancel);
		mBuilder.autoDismiss(true);
		mBuilder.typeface(mTypefaceBold, mTypefaceLight);
		mBuilder.callback(new ButtonCallback() {
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);
				
			}
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				String mPlaylistName = mEdPlaylistName.getText().toString();
				if (StringUtils.isEmptyString(mPlaylistName)) {
					showToast(R.string.info_playlistname_error);
					return;
				}
				if (!isEdit) {
					PlaylistObject mPlaylistObject = new PlaylistObject(System.currentTimeMillis(), mPlaylistName);
					mPlaylistObject.setListTrackIds(new ArrayList<Long>());
					TotalDataManager.getInstance().addPlaylistObject(MainActivity.this, mPlaylistObject);
				}
				else {
					TotalDataManager.getInstance().editPlaylistObject(MainActivity.this, mPlaylistObject, mPlaylistName);
				}
				if (mCallback != null) {
					mCallback.onAction();
				}
			}
		});
		mBuilder.build().show();
	}

	public void showDialogPlaylist(final TrackObject mTrackObject) {
		final ArrayList<PlaylistObject> mListPlaylist = TotalDataManager.getInstance().getListPlaylistObjects();
		if (mListPlaylist != null && mListPlaylist.size() > 0) {
			int size = mListPlaylist.size();
			mListStr = new String[size];
			for (int i = 0; i < size; i++) {
				PlaylistObject mPlaylistObject = mListPlaylist.get(i);
				mListStr[i] = mPlaylistObject.getName();
			}
			MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
			mBuilder.backgroundColor(getResources().getColor(R.color.white));
			mBuilder.title(R.string.title_select_playlist);
			mBuilder.titleColor(getResources().getColor(R.color.black_text));
			mBuilder.items(mListStr);
			mBuilder.contentColor(getResources().getColor(R.color.black_text));
			mBuilder.positiveColor(getResources().getColor(R.color.main_color));
			mBuilder.negativeColor(getResources().getColor(R.color.black_secondary_text));
			mBuilder.positiveText(R.string.title_cancel);
			mBuilder.autoDismiss(true);
			mBuilder.typeface(mTypefaceBold, mTypefaceLight);
			mBuilder.itemsCallback(new ListCallback() {
				
				@Override
				public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
					TotalDataManager.getInstance().addTrackToPlaylist(MainActivity.this, mTrackObject, mListPlaylist.get(which), true, new IDBCallback() {
						@Override
						public void onAction() {
							updateDataOfPlaylist();
						}
					});
					mListStr = null;
				}
			});
			mBuilder.callback(new ButtonCallback() {
				@Override
				public void onNegative(MaterialDialog dialog) {
					super.onNegative(dialog);
					mListStr = null;
				}
			});
			mBuilder.build().show();
		}
		else {
			mListStr = getResources().getStringArray(R.array.list_create_playlist);
			MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
			mBuilder.backgroundColor(getResources().getColor(R.color.white));
			mBuilder.title(R.string.title_select_playlist);
			mBuilder.titleColor(getResources().getColor(R.color.black_text));
			mBuilder.items(mListStr);
			mBuilder.contentColor(getResources().getColor(R.color.black_text));
			mBuilder.positiveColor(getResources().getColor(R.color.main_color));
			mBuilder.negativeColor(getResources().getColor(R.color.black_secondary_text));
			mBuilder.positiveText(R.string.title_cancel);
			mBuilder.autoDismiss(true);
			mBuilder.typeface(mTypefaceBold, mTypefaceLight);
			mBuilder.itemsCallback(new ListCallback() {
				
				@Override
				public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
					createDialogPlaylist(false, null, new IDBCallback() {
						@Override
						public void onAction() {
							updateDataOfPlaylist();
							final ArrayList<PlaylistObject> mListPlaylist = TotalDataManager.getInstance().getListPlaylistObjects();
							TotalDataManager.getInstance().addTrackToPlaylist(MainActivity.this, mTrackObject, mListPlaylist.get(0), true, new IDBCallback() {
								@Override
								public void onAction() {
									updateDataOfPlaylist();
								}
							});
						}
					});
					mListStr = null;
				}
			});
			mBuilder.callback(new ButtonCallback() {
				@Override
				public void onNegative(MaterialDialog dialog) {
					super.onNegative(dialog);
					mListStr = null;
				}
			});
			mBuilder.build().show();
		}
	}

	public void updateDataOfPlaylist() {
		FragmentPlaylist mFragment = getFragmentPlaylist();
		if (mFragment != null) {
			mFragment.notifyData();
		}
	}

	public void updateTracks() {
		FragmentHome mFragment = getFragmentHome();
		if (mFragment != null) {
			mFragment.notifyData();
		}
	}

	public static String getStringTimeAgo(Context mContext, long second) {
		double minutes = second / 60f;
		if (second < 5) {
			return mContext.getString(R.string.title_just_now);
		}
		else if (second < 60) {
			return String.valueOf(second) + " " + mContext.getString(R.string.title_second_ago);
		}
		else if (second < 120) {
			return mContext.getString(R.string.title_a_minute_ago);
		}
		else if (minutes < 60) {
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_minute_ago);
		}
		else if (minutes < 120) {
			return mContext.getString(R.string.title_a_hour_ago);
		}
		else if (minutes < 24 * 60) {
			minutes = Math.floor(minutes / 60);
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_hour_ago);
		}
		else if (minutes < 24 * 60 * 2) {
			return mContext.getString(R.string.title_yester_day);
		}
		else if (minutes < 24 * 60 * 7) {
			minutes = Math.floor(minutes / (60 * 24));
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_day_ago);
		}
		else if (minutes < 24 * 60 * 14) {
			return mContext.getString(R.string.title_last_week);
		}
		else if (minutes < 24 * 60 * 31) {
			minutes = Math.floor(minutes / (60 * 24 * 7));
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_weeks_ago);
		}
		else if (minutes < 24 * 60 * 61) {
			return mContext.getString(R.string.title_last_month);
		}
		else if (minutes < 24 * 60 * 365.25) {
			minutes = Math.floor(minutes / (60 * 24 * 30));
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_month_ago);
		}
		else if (minutes < 24 * 60 * 731) {
			return mContext.getString(R.string.title_last_year);
		}
		else if (minutes > 24 * 60 * 731) {
			minutes = Math.floor(minutes / (60 * 24 * 365));
			return String.valueOf((int) minutes) + " " + mContext.getString(R.string.title_year_ago);
		}
		return mContext.getString(R.string.title_unknown);
	}

	public void showDialogTrack(TrackObject _mTrackObject) {

		final EditText mEdTrackName = new EditText(this);
		mEdTrackName.setText(_mTrackObject.getTitle());

		mTrackObject = _mTrackObject;

		MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
		mBuilder.backgroundColor(getResources().getColor(R.color.white));
		mBuilder.customView(mEdTrackName,false);
		mBuilder.title(R.string.title_track_name);
		mBuilder.titleColor(getResources().getColor(R.color.black_text));
		mBuilder.contentColor(getResources().getColor(R.color.black_text));
		mBuilder.positiveColor(getResources().getColor(R.color.main_color));
		mBuilder.negativeColor(getResources().getColor(R.color.black_secondary_text));
		mBuilder.positiveText(R.string.title_save);
		mBuilder.negativeText(R.string.title_cancel);
		mBuilder.autoDismiss(true);
		mBuilder.typeface(mTypefaceBold, mTypefaceLight);

		mBuilder.callback(new ButtonCallback() {
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);

			}
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				String sTracklistName = mEdTrackName.getText().toString();

				if (StringUtils.isEmptyString(sTracklistName)) {
					showToast(R.string.info_trackname_error);
					return;
				}

				mTrackObject.setTitle(sTracklistName);
				renameSongFromMediaStore(mTrackObject.getId(), sTracklistName);
				updateTracks();
			}
		});
		mBuilder.build().show();
	}
}
