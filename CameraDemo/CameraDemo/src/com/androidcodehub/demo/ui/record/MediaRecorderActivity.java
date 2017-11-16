package com.androidcodehub.demo.ui.record;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidcodehub.demo.R;
import com.androidcodehub.demo.common.CommonIntentExtra;
import com.androidcodehub.demo.log.Logger;
import com.androidcodehub.demo.ui.BaseActivity;
import com.androidcodehub.demo.ui.record.views.ProgressView;
import com.androidcodehub.demo.utils.ConvertToUtils;
import com.yixia.camera.MediaRecorderBase;
import com.yixia.camera.MediaRecorderBase.OnErrorListener;
import com.yixia.camera.MediaRecorderBase.OnPreparedListener;
import com.yixia.camera.MediaRecorderNative;
import com.yixia.camera.MediaRecorderSystem;
import com.yixia.camera.VCamera;

import com.yixia.camera.model.MediaObject;
import com.yixia.camera.util.DeviceUtils;
import com.yixia.camera.util.FileUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.io.File;
import java.util.ArrayList;


public class MediaRecorderActivity extends BaseActivity implements OnErrorListener, OnClickListener, OnPreparedListener, MediaRecorderBase.OnEncodeListener {


	public final static int RECORD_TIME_MAX = 10 * 1000;

	public final static int RECORD_TIME_MIN = 3 * 1000;

	private static final int HANDLE_INVALIDATE_PROGRESS = 0;

	private static final int HANDLE_STOP_RECORD = 1;

	private static final int HANDLE_HIDE_RECORD_FOCUS = 2;

	private ImageView mTitleNext;

	private ImageView mFocusImage;

	private CheckBox mCameraSwitch;

	private CheckedTextView mRecordDelete;

	private CheckBox mRecordLed;

	private ImageView mRecordController;

	private RelativeLayout mBottomLayout;

	private SurfaceView mSurfaceView;

	private ProgressView mProgressView;

	private Animation mFocusAnimation;

	private MediaRecorderBase mMediaRecorder;

	private MediaObject mMediaObject;

	private boolean mRebuild;

	private boolean mCreated;

	private volatile boolean mPressedStatus;

	private volatile boolean mReleased;

	private int mFocusWidth;

	private int mBackgroundColorNormal, mBackgroundColorPress;

	private int mWindowWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mCreated = false;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
		loadIntent();
		loadViews();
		mCreated = true;
	}


	private void loadIntent() {
		mWindowWidth = DeviceUtils.getScreenWidth(this);

		mFocusWidth = ConvertToUtils.dipToPX(this, 64);
		mBackgroundColorNormal = getResources().getColor(R.color.black);//camera_bottom_bg
		mBackgroundColorPress = getResources().getColor(R.color.camera_bottom_press_bg);
	}


	private void loadViews() {
		setContentView(R.layout.activity_media_recorder);

		mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
		mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
		mTitleNext = (ImageView) findViewById(R.id.title_next);
		mFocusImage = (ImageView) findViewById(R.id.record_focusing);
		mProgressView = (ProgressView) findViewById(R.id.record_progress);
		mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
		mRecordController = (ImageView) findViewById(R.id.record_controller);
		mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
		mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);

		if (DeviceUtils.hasICS())
			mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);

		mTitleNext.setOnClickListener(this);
		findViewById(R.id.title_back).setOnClickListener(this);
		mRecordDelete.setOnClickListener(this);
		mBottomLayout.setOnTouchListener(mOnVideoControllerTouchListener);

		if (MediaRecorderBase.isSupportFrontCamera()) {
			mCameraSwitch.setOnClickListener(this);
		} else {
			mCameraSwitch.setVisibility(View.GONE);
		}

		if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
			mRecordLed.setOnClickListener(this);
		} else {
			mRecordLed.setVisibility(View.GONE);
		}

		try {
			mFocusImage.setImageResource(R.drawable.video_focus);
		} catch (OutOfMemoryError e) {
			Logger.e(e);
		}

		mProgressView.setMaxDuration(RECORD_TIME_MAX);
		initSurfaceView();
	}

	private void initSurfaceView() {
		final int w = DeviceUtils.getScreenWidth(this);
		((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w;
		int width = w;
		int height = w * 4 / 3;
		//
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
		lp.width = width;
		lp.height = height;
		mSurfaceView.setLayoutParams(lp);
	}


	private void initMediaRecorder() {
		mMediaRecorder = new MediaRecorderNative();
		mRebuild = true;

		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setOnEncodeListener(this);
		File f = new File(VCamera.getVideoCachePath());
		if (!FileUtils.checkFile(f)) {
			f.mkdirs();
		}
		String key = String.valueOf(System.currentTimeMillis());
		mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath() + key);
		mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
		mMediaRecorder.prepare();
	}


	private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null || !mCreated) {
				return false;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//检测是否手动对焦
				if (checkCameraFocus(event))
					return true;
				break;
			}
			return true;
		}

	};

	private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null) {
				return false;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
					return true;
				}
				if (cancelDelete())
					return true;

				startRecord();

				break;

			case MotionEvent.ACTION_UP:

				if (mPressedStatus) {
					stopRecord();

					if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
						mTitleNext.performClick();
					}
				}
				break;
			}
			return true;
		}

	};

	@Override
	public void onResume() {
		super.onResume();
		UtilityAdapter.freeFilterParser();
		UtilityAdapter.initFilterParser();

		if (mMediaRecorder == null) {
			initMediaRecorder();
		} else {
			mRecordLed.setChecked(false);
			mMediaRecorder.prepare();
			mProgressView.setData(mMediaObject);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopRecord();
		UtilityAdapter.freeFilterParser();
		if (!mReleased) {
			if (mMediaRecorder != null)
				mMediaRecorder.release();
		}
		mReleased = false;
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private boolean checkCameraFocus(MotionEvent event) {
		mFocusImage.setVisibility(View.GONE);
		float x = event.getX();
		float y = event.getY();
		float touchMajor = event.getTouchMajor();
		float touchMinor = event.getTouchMinor();

		Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2), (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));
		//The direction is relative to the sensor orientation, that is, what the sensor sees. The direction is not affected by the rotation or mirroring of setDisplayOrientation(int). Coordinates of the rectangle range from -1000 to 1000. (-1000, -1000) is the upper left point. (1000, 1000) is the lower right point. The width and height of focus areas cannot be 0 or negative.
		//No matter what the zoom level is, (-1000,-1000) represents the top of the currently visible camera frame
		if (touchRect.right > 1000)
			touchRect.right = 1000;
		if (touchRect.bottom > 1000)
			touchRect.bottom = 1000;
		if (touchRect.left < 0)
			touchRect.left = 0;
		if (touchRect.right < 0)
			touchRect.right = 0;

		if (touchRect.left >= touchRect.right || touchRect.top >= touchRect.bottom)
			return false;

		ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
		focusAreas.add(new Camera.Area(touchRect, 1000));
		if (!mMediaRecorder.manualFocus(new Camera.AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				//				if (success) {
				mFocusImage.setVisibility(View.GONE);
				//				}
			}
		}, focusAreas)) {
			mFocusImage.setVisibility(View.GONE);
		}

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage.getLayoutParams();
		int left = touchRect.left - (mFocusWidth / 2);//(int) x - (focusingImage.getWidth() / 2);
		int top = touchRect.top - (mFocusWidth / 2);//(int) y - (focusingImage.getHeight() / 2);
		if (left < 0)
			left = 0;
		else if (left + mFocusWidth > mWindowWidth)
			left = mWindowWidth - mFocusWidth;
		if (top + mFocusWidth > mWindowWidth)
			top = mWindowWidth - mFocusWidth;

		lp.leftMargin = left;
		lp.topMargin = top;
		mFocusImage.setLayoutParams(lp);
		mFocusImage.setVisibility(View.VISIBLE);

		if (mFocusAnimation == null)
			mFocusAnimation = AnimationUtils.loadAnimation(this, R.anim.record_focus);

		mFocusImage.startAnimation(mFocusAnimation);

		mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);//最多3.5秒也要消失
		return true;
	}

	private void startRecord() {
		if (mMediaRecorder != null) {
			MediaObject.MediaPart part = mMediaRecorder.startRecord();
			if (part == null) {
				return;
			}

			if (mMediaRecorder instanceof MediaRecorderSystem) {
				mCameraSwitch.setVisibility(View.GONE);
			}
			mProgressView.setData(mMediaObject);
		}

		mRebuild = true;
		mPressedStatus = true;
		mRecordController.setImageResource(R.drawable.record_controller_press);
		mBottomLayout.setBackgroundColor(mBackgroundColorPress);

		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
			mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

			mHandler.removeMessages(HANDLE_STOP_RECORD);
			mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD, RECORD_TIME_MAX - mMediaObject.getDuration());
		}
		mRecordDelete.setVisibility(View.GONE);
		mCameraSwitch.setEnabled(false);
		mRecordLed.setEnabled(false);
	}

	@Override
	public void onBackPressed() {
		if (mRecordDelete != null && mRecordDelete.isChecked()) {
			cancelDelete();
			return;
		}

		if (mMediaObject != null && mMediaObject.getDuration() > 1) {

			new AlertDialog.Builder(this).setTitle("Close").setMessage(R.string.record_camera_exit_dialog_message).setNegativeButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mMediaObject.delete();
					finish();
				}

			}).setPositiveButton(R.string.record_camera_cancel_dialog_no, null).setCancelable(false).show();
			return;
		}

		if (mMediaObject != null)
			mMediaObject.delete();
		finish();
	}

	private void stopRecord() {
		mPressedStatus = false;
		mRecordController.setImageResource(R.drawable.record_controller_normal);
		mBottomLayout.setBackgroundColor(mBackgroundColorNormal);

		if (mMediaRecorder != null) {
			mMediaRecorder.stopRecord();
		}

		mRecordDelete.setVisibility(View.VISIBLE);
		mCameraSwitch.setEnabled(true);
		mRecordLed.setEnabled(true);

		mHandler.removeMessages(HANDLE_STOP_RECORD);
		checkStatus();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
			mHandler.removeMessages(HANDLE_STOP_RECORD);
		}

		if (id != R.id.record_delete) {
			if (mMediaObject != null) {
				MediaObject.MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						part.remove = false;
						mRecordDelete.setChecked(false);
						if (mProgressView != null)
							mProgressView.invalidate();
					}
				}
			}
		}

		switch (id) {
		case R.id.title_back:
			onBackPressed();
			break;
		case R.id.record_camera_switcher:
			if (mRecordLed.isChecked()) {
				if (mMediaRecorder != null) {
					mMediaRecorder.toggleFlashMode();
				}
				mRecordLed.setChecked(false);
			}

			if (mMediaRecorder != null) {
				mMediaRecorder.switchCamera();
			}

			if (mMediaRecorder.isFrontCamera()) {
				mRecordLed.setEnabled(false);
			} else {
				mRecordLed.setEnabled(true);
			}
			break;
		case R.id.record_camera_led:

			if (mMediaRecorder != null) {
				if (mMediaRecorder.isFrontCamera()) {
					return;
				}
			}

			if (mMediaRecorder != null) {
				mMediaRecorder.toggleFlashMode();
			}
			break;
		case R.id.title_next://
			mMediaRecorder.startEncoding();
			break;
		case R.id.record_delete:

			if (mMediaObject != null) {
				MediaObject.MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						mRebuild = true;
						part.remove = false;
						mMediaObject.removePart(part, true);
						mRecordDelete.setChecked(false);
					} else {
						part.remove = true;
						mRecordDelete.setChecked(true);
					}
				}
				if (mProgressView != null)
					mProgressView.invalidate();

					checkStatus();
			}
			break;
		}
	}
	private boolean cancelDelete() {
		if (mMediaObject != null) {
			MediaObject.MediaPart part = mMediaObject.getCurrentPart();
			if (part != null && part.remove) {
				part.remove = false;
				mRecordDelete.setChecked(false);

				if (mProgressView != null)
					mProgressView.invalidate();

				return true;
			}
		}
		return false;
	}

	private int checkStatus() {
		int duration = 0;
		if (!isFinishing() && mMediaObject != null) {
			duration = mMediaObject.getDuration();
			if (duration < RECORD_TIME_MIN) {
				if (duration == 0) {
					mCameraSwitch.setVisibility(View.VISIBLE);
					mRecordDelete.setVisibility(View.GONE);
				}

				if (mTitleNext.getVisibility() != View.INVISIBLE)
					mTitleNext.setVisibility(View.INVISIBLE);
			} else {

				if (mTitleNext.getVisibility() != View.VISIBLE) {
					mTitleNext.setVisibility(View.VISIBLE);
				}
			}
		}
		return duration;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_INVALIDATE_PROGRESS:
				if (mMediaRecorder != null && !isFinishing()) {
					if (mProgressView != null)
						mProgressView.invalidate();
					//					if (mPressedStatus)
					//						titleText.setText(String.format("%.1f", mMediaRecorder.getDuration() / 1000F));
					if (mPressedStatus)
						sendEmptyMessageDelayed(0, 30);
				}
				break;
			}
		}
	};

	@Override
	public void onEncodeStart() {
		showProgress("", getString(R.string.record_camera_progress_message));
	}

	@Override
	public void onEncodeProgress(int progress) {
		Logger.e("[MediaRecorderActivity]onEncodeProgress..." + progress);
	}


	@Override
	public void onEncodeComplete() {
		hideProgress();
		Intent intent = new Intent(this, MediaPreviewActivity.class);
		Bundle bundle = getIntent().getExtras();
		if (bundle == null)
			bundle = new Bundle();
		bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
		bundle.putString("output", mMediaObject.getOutputTempVideoPath());
		bundle.putBoolean("Rebuild", mRebuild);
		intent.putExtras(bundle);
		startActivity(intent);
		mRebuild = false;
	}


	@Override
	public void onEncodeError() {
		hideProgress();
		Toast.makeText(this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onVideoError(int what, int extra) {

	}

	@Override
	public void onAudioError(int what, String message) {

	}

	@Override
	public void onPrepared() {

	}
}
