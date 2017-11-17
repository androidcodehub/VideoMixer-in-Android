package com.androidcodehub.demo.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidcodehub.demo.R;
import com.androidcodehub.demo.common.CommonIntentExtra;
import com.androidcodehub.demo.po.POThemeSingle;
import com.androidcodehub.demo.service.AssertService;
import com.androidcodehub.demo.ui.BaseActivity;
import com.androidcodehub.demo.ui.record.helper.ThemeHelper;
import com.androidcodehub.demo.ui.record.views.ThemeGroupLayout;
import com.androidcodehub.demo.ui.record.views.ThemeSufaceView;
import com.androidcodehub.demo.ui.record.views.ThemeView;
import com.androidcodehub.demo.utils.ConvertToUtils;
import com.androidcodehub.demo.utils.IsUtils;
import com.androidcodehub.demo.utils.ToastUtils;

import com.yixia.camera.model.MediaObject;
import com.yixia.camera.model.MediaThemeObject;
import com.yixia.camera.util.DeviceUtils;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.io.File;
import java.util.ArrayList;


public class MediaPreviewActivity extends BaseActivity implements OnClickListener, UtilityAdapter.OnNativeListener {


	private static final int HANDLER_ENCODING_START = 100;

	private static final int HANDLER_ENCODING_PROGRESS = 101;

	private static final int HANDLER_ENCODING_END = 102;
	private final static int NO_THEME_INDEX = 0;

	private ImageView mPlayStatus;

	private TextView mTitleLeft, mTitleNext, mTitleText, mVideoPreviewMusic;

	private CheckBox mThemeVolumn, mVideoVolumn;

	private View mLoadingView;

	private View mThemeLayout, mFilterLayout;

	private ThemeGroupLayout mThemes, mFilters;

	private ThemeSufaceView mThemeSufaceView;

	private File mThemeCacheDir;

	private POThemeSingle mCurrentTheme;

	private ArrayList<POThemeSingle> mThemeList;

	private ArrayList<POThemeSingle> mFilterList;

	private String mAuthorBitmapPath;

	private String mVideoPath, mCoverPath;

	private String mVideoTempPath;

	private String mCurrentMusicPath;

	private String mCurrentMusicTitle;

	private String mCurrentMusicName;

	private boolean mNeedResume;

	private boolean mStopPlayer;

	private boolean mStartEncoding;

	private int mWindowWidth;

	private int mLeftMargin;

	private int mDuration;

	private MediaObject mMediaObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMediaObject = (MediaObject) getIntent().getSerializableExtra(CommonIntentExtra.EXTRA_MEDIA_OBJECT);
		if (mMediaObject == null) {
			Toast.makeText(this, R.string.record_read_object_faild, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 防止锁屏
		prepareActivity();
		prepareViews();
	}


	private boolean prepareActivity() {

		mWindowWidth = DeviceUtils.getScreenWidth(this);

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && !isExternalStorageRemovable())
			mThemeCacheDir = new File(getExternalCacheDir(), "Theme");
		else
			mThemeCacheDir = new File(getCacheDir(), "Theme");
		mLeftMargin = ConvertToUtils.dipToPX(this, 8);

		mVideoPath = mMediaObject.getOutputVideoPath();
		if (StringUtils.isNotEmpty(mVideoPath)) {
			mCoverPath = mVideoPath.replace(".mp4", ".jpg");
		}
		mVideoTempPath = getIntent().getStringExtra("output");

		return true;
	}


	private void prepareViews() {
		setContentView(R.layout.activity_media_preview);

		mPlayStatus = (ImageView) findViewById(R.id.play_status);
		mThemeSufaceView = (ThemeSufaceView) findViewById(R.id.preview_theme);
		mTitleLeft = (TextView) findViewById(R.id.titleLeft);
	//
		// 	mTitleNext = (TextView) findViewById(R.id.titleRight);
		mTitleText = (TextView) findViewById(R.id.titleText);
		mVideoPreviewMusic = (TextView) findViewById(R.id.video_preview_music);
		mThemes = (ThemeGroupLayout) findViewById(R.id.themes);
		mFilters = (ThemeGroupLayout) findViewById(R.id.filters);
		mThemeVolumn = (CheckBox) findViewById(R.id.video_preview_theme_volume);
		mVideoVolumn = (CheckBox) findViewById(R.id.video_preview_video_volume);
		mLoadingView = findViewById(R.id.loading);
		mThemeLayout = findViewById(R.id.theme_layout);
		mFilterLayout = findViewById(R.id.filter_layout);

		mTitleLeft.setOnClickListener(this);
	//
		//
	//	mTitleNext.setOnClickListener(this);
		mThemeSufaceView.setOnComplateListener(mOnComplateListener);
		mThemeSufaceView.setOnClickListener(this);
		findViewById(R.id.tab_theme).setOnClickListener(this);
		findViewById(R.id.tab_filter).setOnClickListener(this);
		mThemeVolumn.setOnClickListener(this);
		mVideoVolumn.setOnClickListener(this);

		mTitleText.setText(R.string.record_camera_preview_title);
	//mTitleNext.setText(R.string.record_camera_preview_next);


		mThemeSufaceView.setIntent(getIntent());
		mThemeSufaceView.setOutputPath(mVideoPath);
		mThemeSufaceView.setMediaObject(mMediaObject);
		if (FileUtils.checkFile(mThemeCacheDir)) {
			mThemeSufaceView.setFilterCommomPath(new File(mThemeCacheDir, ThemeHelper.THEME_VIDEO_COMMON).getAbsolutePath());
		}

		View preview_layout = findViewById(R.id.preview_layout);
		LinearLayout.LayoutParams mPreviewParams = (LinearLayout.LayoutParams) preview_layout.getLayoutParams();
		mPreviewParams.height = DeviceUtils.getScreenWidth(this);
		loadThemes();
	}

	@Override
	public void onResume() {
		super.onResume();
		UtilityAdapter.registerNativeListener(this);
		if (mThemeSufaceView != null && mNeedResume && mCurrentTheme != null) {
			restart();
		}
		mNeedResume = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		UtilityAdapter.registerNativeListener(null);
		if (mThemeSufaceView != null && mThemeSufaceView.isPlaying()) {
			mNeedResume = true;
			releaseVideo();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titleLeft:
			finish();
			break;

		case R.id.preview_theme:
			if (isPlaying())
				stopVideo();
			else
				startVideo();
			break;
		case R.id.video_preview_theme_volume:

			ToastUtils.showToastImage(this, mThemeVolumn.isChecked() ? R.drawable.priview_theme_volumn_close : R.drawable.priview_theme_volumn_open);
			mThemeSufaceView.setThemeMute(mThemeVolumn.isChecked());
			restart();
			break;
		case R.id.video_preview_video_volume:
			ToastUtils.showToastImage(this, mVideoVolumn.isChecked() ? R.drawable.priview_orig_volumn_close : R.drawable.priview_orig_volumn_open);
			mThemeSufaceView.setOrgiMute(mVideoVolumn.isChecked());
			restart();
			break;
		case R.id.tab_theme:
			mThemeLayout.setVisibility(View.VISIBLE);
			mFilterLayout.setVisibility(View.GONE);
			break;
		case R.id.tab_filter:
			mThemeLayout.setVisibility(View.GONE);
			mFilterLayout.setVisibility(View.VISIBLE);
			break;
		}
	}


	private void startEncoding() {

		stopVideo();

		if (mMediaObject != null && mMediaObject.mThemeObject != null) {
			mMediaObject.mThemeObject.mThemeMute = mThemeVolumn.isChecked();
			mMediaObject.mThemeObject.mOrgiMute = mVideoVolumn.isChecked();
		}

		mStartEncoding = true;
		mHandler.removeMessages(HANDLER_ENCODING_START);
		mHandler.removeMessages(HANDLER_ENCODING_PROGRESS);
		mHandler.removeMessages(HANDLER_ENCODING_END);
		mHandler.sendEmptyMessage(HANDLER_ENCODING_START);
	}


	private void loadThemes() {
		if (isFinishing() || mStartEncoding)
			return;

		new android.os.AsyncTask<Void, Void, File>() {

			@Override
			protected File doInBackground(Void... params) {

				while (AssertService.isRunning()) {
					SystemClock.sleep(500);
				}


				File result = ThemeHelper.prepareTheme(MediaPreviewActivity.this, mThemeCacheDir);
				if (result != null) {

					mThemeList = ThemeHelper.parseTheme(MediaPreviewActivity.this, mThemeCacheDir, ThemeHelper.THEME_MUSIC_VIDEO_ASSETS, R.array.theme_order);


					POThemeSingle orgiTheme = ThemeHelper.loadThemeJson(mThemeCacheDir, new File(mThemeCacheDir, ThemeHelper.THEME_EMPTY));
					if (orgiTheme != null)
						mThemeList.add(NO_THEME_INDEX, orgiTheme);
				}


				mFilterList = ThemeHelper.parseTheme(MediaPreviewActivity.this, mThemeCacheDir, ThemeHelper.THEME_FILTER_ASSETS, R.array.theme_filter_order);

				mAuthorBitmapPath = ThemeHelper.updateVideoAuthorLogo(mThemeCacheDir, getString(R.string.record_camera_author, getString(R.string.app_name)), false);
				return result;
			}

			@Override
			protected void onPostExecute(File result) {
				super.onPostExecute(result);
				File themeDir = result;
				if (themeDir != null && !isFinishing() && mThemeList != null && mThemeList.size() > 1) {

					mThemes.removeAllViews();

					String themeName = getIntent().getStringExtra("theme");
					int defaultIndex = NO_THEME_INDEX, index = 0;
					if (mCurrentTheme != null) {
						themeName = mCurrentTheme.themeName;
					}
					for (POThemeSingle theme : mThemeList) {
						addThemeItem(theme, -1);
						if (StringUtils.isNotEmpty(themeName) && IsUtils.equals(theme.themeName, themeName)) {
							defaultIndex = index;
						}
						index++;
					}

					mFilters.removeAllViews();
					for (POThemeSingle theme : mFilterList) {
						addThemeItem(mFilters, theme, -1);
					}

					mCurrentTheme = null;
					mThemes.getChildAt(defaultIndex).performClick();
				}
			}

		}.execute();
	}

	private ThemeView addThemeItem(ThemeGroupLayout layout, POThemeSingle theme, int index) {
		ThemeView themeView = new ThemeView(MediaPreviewActivity.this, theme);
		if (theme.themeIconResource > 0) {
			themeView.getIcon().setImageResource(theme.themeIconResource);
		} else {
			if (StringUtils.isNotEmpty(theme.themeIcon)) {
				themeView.getIcon().setImagePath(theme.themeIcon);
			}
		}

		themeView.setOnClickListener(mThemeClickListener);
		themeView.setTag(theme);
		//		Logger.e("[MediaPreviewActivity]addThemeItem..." + theme.themeDisplayName + " themeFolder:" + theme.themeFolder);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);//mThemeItemWH, mThemeItemWH
		lp.leftMargin = mLeftMargin;
		if (index == -1)
			layout.addView(themeView, lp);
		else
			layout.addView(themeView, index, lp);
		return themeView;
	}



	private ThemeView addThemeItem(POThemeSingle theme, int index) {
		return addThemeItem(mThemes, theme, index);
	}


	private synchronized void restart() {
		mStopPlayer = false;
		mHandler.removeMessages(UtilityAdapter.NOTIFYVALUE_PLAYFINISH);
		mHandler.sendEmptyMessageDelayed(UtilityAdapter.NOTIFYVALUE_PLAYFINISH, 100);
	}

	private void releaseVideo() {
		mThemeSufaceView.pauseClearDelayed();
		mThemeSufaceView.release();
		mPlayStatus.setVisibility(View.GONE);
	}


	private void startVideo() {
		mStopPlayer = false;
		mThemeSufaceView.start();
		mPlayStatus.setVisibility(View.GONE);
	}


	private void stopVideo() {
		mStopPlayer = true;
		mThemeSufaceView.pause();
		mPlayStatus.setVisibility(View.VISIBLE);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_ENCODING_START:
				if (!isFinishing()) {
					showProgress("", getString(R.string.record_preview_encoding));
					//					WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
					//					lp.y = -ConvertToUtils.dipToPX(MediaPreviewActivity.this, 49 + 30);
					//					dialog.getWindow().setAttributes(lp);
					//					showProgressLayout(false, false, getString(R.string.progressbar_message_preview_making));
					releaseVideo();
					mThemeSufaceView.startEncoding();
					sendEmptyMessage(HANDLER_ENCODING_PROGRESS);
				}
				break;
			case HANDLER_ENCODING_PROGRESS:
				int progress = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_PROGRESS);
				if (mProgressDialog != null) {
					mProgressDialog.setMessage(getString(R.string.record_preview_encoding_format, progress));
				}
				if (progress < 100)
					sendEmptyMessageDelayed(HANDLER_ENCODING_PROGRESS, 200);
				else {
					sendEmptyMessage(HANDLER_ENCODING_END);
				}
				break;
			case HANDLER_ENCODING_END:
				mDuration = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_TOTALMS);
				mThemeSufaceView.release();
				onEncodingEnd();
				break;
			case UtilityAdapter.NOTIFYVALUE_BUFFEREMPTY:
				showLoading();
				break;
			case UtilityAdapter.NOTIFYVALUE_BUFFERFULL:
				hideLoading();
				break;
			case UtilityAdapter.NOTIFYVALUE_PLAYFINISH:

				if (!isFinishing() && !mStopPlayer) {
					showLoading();
					mThemeSufaceView.release();
					mThemeSufaceView.initFilter();
					mPlayStatus.setVisibility(View.GONE);
				}
				break;
			case UtilityAdapter.NOTIFYVALUE_HAVEERROR:

				if (!isFinishing()) {
					Toast.makeText(MediaPreviewActivity.this, R.string.record_preview_theme_load_faild, Toast.LENGTH_SHORT).show();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};


	private void onEncodingEnd() {
		hideProgress();
		mStartEncoding = false;
///		startActivity(new Intent(this, VideoPlayerActivity.class).putExtra("path", mVideoPath));
	}


	private void showLoading() {
		//showProgress("", getString(R.string.record_preview_building));
		if (mLoadingView != null)
			mLoadingView.setVisibility(View.VISIBLE);
	}


	private void hideLoading() {
		if (mLoadingView != null)
			mLoadingView.setVisibility(View.GONE);
	}


	private boolean isPlaying() {
		return mThemeSufaceView.isPlaying();
	}


	private OnClickListener mThemeClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			POThemeSingle theme = (POThemeSingle) v.getTag();
			if (theme == null || mMediaObject == null)
				return;

			if (StringUtils.isNotEmpty(mAuthorBitmapPath) && (mCurrentTheme == null || !IsUtils.equals(mCurrentTheme.themeName, theme.themeName))) {
				String key = theme.themeName;
				((ThemeGroupLayout) v.getParent()).mObservable.notifyObservers(key);

				mCurrentTheme = theme;

				if (mMediaObject.mThemeObject == null)
					mMediaObject.mThemeObject = new MediaThemeObject();

				if (theme.isMV()) {
					mMediaObject.mThemeObject.mMVThemeName = theme.themeName;
					mMediaObject.mThemeObject.mMusicThemeName = theme.musicName;
					mThemeSufaceView.reset();
					mThemeSufaceView.setMVPath(theme.themeFolder);
					mThemeSufaceView.setTheme(theme);
					mThemeSufaceView.setVideoEndPath(mAuthorBitmapPath);
					mThemeSufaceView.setInputPath(mVideoTempPath);

					mCurrentMusicPath = mCurrentTheme.musicPath;
					mCurrentMusicTitle = mCurrentTheme.musicTitle;
					mCurrentMusicName = mCurrentTheme.musicName;
					mThemeSufaceView.setMusicPath(mCurrentMusicPath);

					updateMusicTextView();


					mThemeVolumn.setChecked(false);

					if (mFilters != null) {
						mFilters.mObservable.notifyObservers(POThemeSingle.THEME_EMPTY);
					}
				}


				if (theme.isFilter()) {
					mMediaObject.mThemeObject.mFilterThemeName = theme.themeName;
					mThemeSufaceView.setFilterPath(theme.getFilterPath());
				}

				restart();
			}
		}
	};

	private void updateMusicTextView() {
		if (StringUtils.isEmpty(mCurrentMusicTitle)) {
			mVideoPreviewMusic.setText(R.string.record_preview_music_nothing);
			mThemeVolumn.setVisibility(View.GONE);
		} else {
			mVideoPreviewMusic.setText(mCurrentMusicTitle);
			mThemeVolumn.setVisibility(View.VISIBLE);
		}
	}


	private ThemeSufaceView.OnComplateListener mOnComplateListener = new ThemeSufaceView.OnComplateListener() {

		@Override
		public void onComplate() {
			if (!isFinishing()) {
				mThemeSufaceView.release();
			}
		}

	};

	public static boolean isExternalStorageRemovable() {
		if (DeviceUtils.hasGingerbread())
			return Environment.isExternalStorageRemovable();
		else
			return Environment.MEDIA_REMOVED.equals(Environment.getExternalStorageState());
	}

	@Override
	public void ndkNotify(int key, int value) {
		if (!isFinishing())
			mHandler.sendEmptyMessage(value);
	}
}
