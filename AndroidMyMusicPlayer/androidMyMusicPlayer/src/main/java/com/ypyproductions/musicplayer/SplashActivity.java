package com.ypyproductions.musicplayer;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.ypyproductions.musicplayer.dataMng.TotalDataManager;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.musicplayer.view.CircularProgressBar;
import com.ypyproductions.task.DBTask;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.task.IDBTaskListener;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBLog;


public class SplashActivity extends DBFragmentActivity {

	public static final String TAG = SplashActivity.class.getSimpleName();

	private CircularProgressBar mProgressBar;
	private boolean isPressBack;

	private Handler mHandler = new Handler();
	private TextView mTvCopyright;

	private TextView mTvVersion;
	private boolean isLoading;
	private TextView mTvAppName;
	private boolean isStartAnimation;
	private ImageView mImgLogo;
	protected boolean isShowingDialog;

	private DBTask mDBTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_splash);
		this.mProgressBar = (CircularProgressBar) findViewById(R.id.progressBar1);
		this.mTvCopyright = (TextView) findViewById(R.id.tv_copyright);
		this.mTvVersion = (TextView) findViewById(R.id.tv_version);
		this.mTvAppName = (TextView) findViewById(R.id.tv_app_name);

		mImgLogo = (ImageView) findViewById(R.id.img_logo);

		this.mTvCopyright.setTypeface(mTypefaceNormal);
		this.mTvVersion.setTypeface(mTypefaceNormal);
		this.mTvAppName.setTypeface(mTypefaceNormal);

		mProgressBar.setVisibility(View.INVISIBLE);
		mTvAppName.setVisibility(View.INVISIBLE);

		mTvVersion.setText(String.format(getString(R.string.info_version_format), ApplicationUtils.getVersionName(this)));
		DBLog.setDebug(DEBUG);
		SettingManager.setOnline(this, true);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if (!isLoading) {
			isLoading = true;
			startAnimationLogo(new IDBCallback() {
				@Override
				public void onAction() {
					mProgressBar.setVisibility(View.VISIBLE);
					mTvAppName.setVisibility(View.VISIBLE);
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							startLoadFavorite();
						}
					}, 2000);
				}
			});
		}
	}

	public void startLoadFavorite() {
		mDBTask = new DBTask(new IDBTaskListener() {

			@Override
			public void onPreExcute() {
				
			}

			@Override
			public void onDoInBackground() {
				TotalDataManager.getInstance().readLibraryTrack(SplashActivity.this);
			}

			@Override
			public void onPostExcute() {
				mProgressBar.setVisibility(View.INVISIBLE);
				Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(mIntent);
				finish();
			}

		});
		mDBTask.execute();
	}

	private void startAnimationLogo(final IDBCallback mCallback) {
		if (!isStartAnimation) {
			isStartAnimation = true;
			mProgressBar.setVisibility(View.INVISIBLE);
			mImgLogo.setRotationY(-180);

			AccelerateDecelerateInterpolator mInterpolator = new AccelerateDecelerateInterpolator();
			final ViewPropertyAnimator localViewPropertyAnimator = mImgLogo.animate().rotationY(0).setDuration(1000).setInterpolator(mInterpolator);

			localViewPropertyAnimator.setListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					if (mCallback != null) {
						mCallback.onAction();
					}
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					if (mCallback != null) {
						mCallback.onAction();
					}
				}
			});
			localViewPropertyAnimator.start();
		}
		else {
			if (mCallback != null) {
				mCallback.onAction();
			}
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onDestroyData() {
		super.onDestroyData();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isPressBack) {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
