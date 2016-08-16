package com.ypyproductions.musicplayer.playerservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.audiofx.Equalizer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import com.ypyproductions.musicplayer.MainActivity;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.dataMng.TotalDataManager;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.musicplayer.setting.ISettingConstants;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.task.DBTask;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.task.IDBTaskListener;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.ImageProcessingUtils;
import com.ypyproductions.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener, IMusicFocusableListener, IMusicConstant,
ISettingConstants, IMyMusicPlayerConstants {

	public final static String TAG = MusicService.class.getSimpleName();

	private MediaPlayer mPlayer = null;

	private AudioFocusHelper mAudioFocusHelper = null;

	private enum State {
		STOPPED, PREPARING, PLAYING, PAUSED
	};

	private State mState = State.STOPPED;

	private enum AudioFocus {
		NO_FOCUS_NO_DUCK, // we don't have audio focus, and can't duck
		NO_FOCUS_CAN_DUCK, // we don't have focus, but can play at a low volume
		FOCUSED // we have full audio focus
	}

	private AudioFocus mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;

	private String mSongTitle = "";

	private WifiLock mWifiLock;

	private RemoteControlClientCompat mRemoteControlClientCompat;

	private ComponentName mMediaButtonReceiverComponent;

	private AudioManager mAudioManager;
	private NotificationManager mNotificationManager;

	private Notification mNotification = null;

	private TrackObject mCurrentTrack;
	private Handler mHandler = new Handler();

	private Equalizer mEqualizer;

	private RemoteViews notificationView;

	private Bitmap mBitmapTrack;

	private void createMediaPlayerIfNeeded() {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);

			SoundCloundDataMng.getInstance().setPlayer(mPlayer);
		}
		else {
			mPlayer.reset();
			if (mEqualizer != null) {
				mEqualizer.release();
				mEqualizer = null;
			}
		}
	}

	private void initEqualizer() {
		mEqualizer = new Equalizer(0, mPlayer.getAudioSessionId());
		mEqualizer.setEnabled(SettingManager.getEqualizer(this));
		setUpParams();
		SoundCloundDataMng.getInstance().setEqualizer(mEqualizer);
	}

	private void setUpParams() {
		if (mEqualizer != null) {
			String presetStr = SettingManager.getEqualizerPreset(this);
			if (!StringUtils.isEmptyString(presetStr)) {
				if (StringUtils.isNumber(presetStr)) {
					short preset = Short.parseShort(presetStr);
					short numberPreset = mEqualizer.getNumberOfPresets();
					if (numberPreset > 0) {
						if (preset < numberPreset - 1 && preset >= 0) {
							try {
								mEqualizer.usePreset(preset);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							return;
						}
					}
				}
			}
			setUpEqualizerCustom();
		}
	}

	private void setUpEqualizerCustom() {
		if (mEqualizer != null) {
			String params = SettingManager.getEqualizerParams(this);
			if (!StringUtils.isEmptyString(params)) {
				String[] mEqualizerParams = params.split(":");
				if (mEqualizerParams != null && mEqualizerParams.length > 0) {
					int size = mEqualizerParams.length;
					for (int i = 0; i < size; i++) {
						mEqualizer.setBandLevel((short) i, Short.parseShort(mEqualizerParams[i]));
					}
					short numberPreset = mEqualizer.getNumberOfPresets();
					SettingManager.setEqualizerPreset(this, String.valueOf(numberPreset));
				}
			}
		}
	}

	@Override
	public void onCreate() {
		mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

		mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		String packageName = getPackageName();
		if (StringUtils.isEmptyString(action)) {
			return START_NOT_STICKY;
		}
		if (action.equals(packageName + ACTION_TOGGLE_PLAYBACK)) {
			processTogglePlaybackRequest();
		}
		else if (action.equals(packageName + ACTION_PLAY)) {
			processPlayRequest(true);
		}
		else if (action.equals(packageName + ACTION_PAUSE)) {
			processPauseRequest();
		}
		else if (action.equals(packageName + ACTION_NEXT)) {
			processNextRequest();
		}
		else if (action.equals(packageName + ACTION_STOP)) {
			processStopRequest();
		}
		else if (action.equals(packageName + ACTION_PREVIOUS)) {
			processPreviousRequest();
		}
		else if (action.equals(packageName + ACTION_SEEK)) {
			int mCurrentPos = intent.getIntExtra(KEY_POSITION, -1);
			processSeekBar(mCurrentPos);
		}
		return START_NOT_STICKY;
	}

	private void processTogglePlaybackRequest() {
		if (mState == State.PAUSED || mState == State.STOPPED) {
			processPlayRequest(false);
		}
		else {
			processPauseRequest();
		}
	}

	private void processSeekBar(int currentPos) {
		if ((mState == State.PLAYING || mState == State.PAUSED) && currentPos > 0) {
			if (mPlayer != null) {
				DBLog.d(TAG, "================>currentPos=" + currentPos);
				mPlayer.seekTo(currentPos);
			}
		}
	}

	private void processPlayRequest(boolean isForces) {
		ArrayList<TrackObject> mListTrack = SoundCloundDataMng.getInstance().getListPlayingTrackObjects();
		if (mListTrack == null) {
			startGetListData(new IDBCallback() {
				@Override
				public void onAction() {
					processPlayRequest(false);
				}
			});
			return;
		}
		mCurrentTrack = SoundCloundDataMng.getInstance().getCurrentTrackObject();
		if (mCurrentTrack == null) {
			mState = State.PAUSED;
			SoundCloundDataMng.getInstance().onDestroy();
			processStopRequest(true);
			onDestroyBitmap();
			return;
		}
		tryToGetAudioFocus();
		if (mState == State.STOPPED || mState == State.PLAYING || isForces) {
			playNextSong();
			broadcastAction(getPackageName() + ACTION_NEXT);
		}
		else if (mState == State.PAUSED) {
			mState = State.PLAYING;
			configAndStartMediaPlayer();
			updateStatusPlayPause();
		}
		if (mRemoteControlClientCompat != null) {
			mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}
	}

	private void startGetListData(final IDBCallback mCallback) {
		final Context mContext = getApplicationContext();
		DBTask mDBTask = new DBTask(new IDBTaskListener() {

			private ArrayList<TrackObject> mListNewTrackObjects;

			@Override
			public void onPreExcute() {
				
			}

			@Override
			public void onDoInBackground() {
				TotalDataManager.getInstance().readLibraryTrack(mContext);
				mListNewTrackObjects = TotalDataManager.getInstance().getListLibraryTrackObjects();
				if (mListNewTrackObjects != null && mListNewTrackObjects.size() > 0) {
					SoundCloundDataMng.getInstance().setListPlayingTrackObjects(mListNewTrackObjects);
					SoundCloundDataMng.getInstance().setCurrentIndex(0);
				}

			}

			@Override
			public void onPostExcute() {
				if (mCallback != null) {
					mCallback.onAction();
				}
			}

		});
		mDBTask.execute();
	}

	private void processPauseRequest() {
		if (mCurrentTrack == null) {
			mState = State.PAUSED;
			processStopRequest(true);
			return;
		}
		if (mState == State.PLAYING) {
			mState = State.PAUSED;
			mPlayer.pause();
			relaxResources(false);
			updateStatusPlayPause();
			SettingManager.setPlayingState(this, false);
			broadcastAction(getPackageName() + ACTION_PAUSE);
		}
		if (mRemoteControlClientCompat != null) {
			mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		}
	}

	private void processPreviousRequest() {
		if (mState == State.PLAYING || mState == State.PAUSED || mState == State.STOPPED) {
			mCurrentTrack = SoundCloundDataMng.getInstance().getPrevTrackObject(this);
			if (mCurrentTrack != null) {
				tryToGetAudioFocus();
				playNextSong();
			}
			else {
				mState = State.PAUSED;
				processStopRequest(true);
			}
		}
	}

	private void onDestroyBitmap() {
		if (mBitmapTrack != null) {
			mBitmapTrack.recycle();
			mBitmapTrack = null;
		}
	}


	private void processNextRequest() {
		if (mState == State.PLAYING || mState == State.PAUSED || mState == State.STOPPED) {
			mCurrentTrack = SoundCloundDataMng.getInstance().getNextTrackObject(this);
			DBLog.d(TAG, "==========>mCurrentTrack=" + mCurrentTrack);
			if (mCurrentTrack != null) {
				tryToGetAudioFocus();
				playNextSong();
			}
			else {
				mState = State.PAUSED;
				processStopRequest(true);
			}
		}
	}

	private void processStopRequest() {
		processStopRequest(false);
	}

	private void processStopRequest(boolean force) {
		if (mState == State.PLAYING || mState == State.PAUSED || force) {
			SettingManager.setPlayingState(this, false);
			mHandler.removeCallbacksAndMessages(null);
			mState = State.STOPPED;
			relaxResources(true);
			giveUpAudioFocus();
			if (mRemoteControlClientCompat != null) {
				mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
			}
			broadcastAction(getPackageName() + ACTION_STOP);
			stopSelf();
		}
	}

	private void relaxResources(boolean releaseMediaPlayer) {
		if (releaseMediaPlayer && mPlayer != null) {
			stopForeground(true);
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
			if (mEqualizer != null) {
				mEqualizer.release();
				mEqualizer = null;
			}
			SoundCloundDataMng.getInstance().setEqualizer(null);
			SoundCloundDataMng.getInstance().setPlayer(null);
		}
		if (mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	private void giveUpAudioFocus() {
		if (mAudioFocus != null && mAudioFocus == AudioFocus.FOCUSED && mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) {
			mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
		}
	}

	/**
	 * Reconfigures MediaPlayer according to audio focus settings and
	 * starts/restarts it. This method starts/restarts the MediaPlayer
	 * respecting the current audio focus state. So if we have focus, it will
	 * play normally; if we don't have focus, it will either leave the
	 * MediaPlayer paused or set it to a low volume, depending on what is
	 * allowed by the current focus settings. This method assumes mPlayer !=
	 * null, so if you are calling it, you have to do so from a context where
	 * you are sure this is the case.
	 */
	private void configAndStartMediaPlayer() {
		if (mPlayer != null) {
			if (mAudioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					SettingManager.setPlayingState(this, false);
					mHandler.removeCallbacksAndMessages(null);
					broadcastAction(getPackageName() + ACTION_PAUSE);
				}
				return;
			}
			else if (mAudioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
				mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
			}
			else {
				mPlayer.setVolume(1.0f, 1.0f);
			}
			if (!mPlayer.isPlaying()) {
				mPlayer.start();
				SettingManager.setPlayingState(this, true);
				startUpdatePosition();
				broadcastAction(getPackageName() + ACTION_PLAY);
			}
		}
	}

	private void broadcastAction(String action) {
		Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
		mIntent.putExtra(KEY_ACTION, action);
		sendBroadcast(mIntent);
	}

	private void tryToGetAudioFocus() {
		if (mAudioFocus != null && mAudioFocus != AudioFocus.FOCUSED && mAudioFocusHelper != null && mAudioFocusHelper.requestFocus())
			mAudioFocus = AudioFocus.FOCUSED;
	}

	/**
	 * Starts playing the next song. If manualUrl is null, the next song will be
	 * randomly selected from our Media Retriever (that is, it will be a random
	 * song in the user's device). If manualUrl is non-null, then it specifies
	 * the URL or path to the song that will be played next.
	 */
	private void playNextSong() {
		mState = State.STOPPED;
		relaxResources(false);
		mHandler.removeCallbacksAndMessages(null);
		if (mCurrentTrack == null) {
			mState = State.PAUSED;
			processStopRequest(true);
			return;
		}
		Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_COUNT_PLAY);
		sendBroadcast(mIntent);

		startStream();
	}

	private void startStream() {
		if (mCurrentTrack != null) {
			final String packageName = getPackageName();
			onDestroyBitmap();
			String path = mCurrentTrack.getPath();
			if (!StringUtils.isEmptyString(path)) {
				broadcastAction(packageName + ACTION_LOADING);
				processBitmapWithLocal();
				startStreamWithUrl(null);
				return;
			}
		}
	}

	private void processBitmapWithLocal(){
		if(mCurrentTrack==null){
			return;
		}
		try {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(this, mCurrentTrack.getURI());
			byte[] rawArt = mmr.getEmbeddedPicture();
			ByteArrayInputStream mInputStream=null;
			if(rawArt!=null && rawArt.length>0){
				mInputStream = new ByteArrayInputStream(rawArt);
				mmr.release();
			}
			else{
				mmr.release();
			}
			if(mInputStream!=null){
				mBitmapTrack = ImageProcessingUtils.decodePortraitBitmap(mInputStream, 100, 100);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void startStreamWithUrl(String manualUrl) {
		try {
			boolean isCanPlay = false;
			boolean isNeedAllowPreSyn = true;
			mSongTitle = mCurrentTrack.getTitle();
			String path = mCurrentTrack.getPath();
			if (!StringUtils.isEmptyString(path)) {
				createMediaPlayerIfNeeded();
				mPlayer.setDataSource(this, mCurrentTrack.getURI());
				mPlayer.prepare();
				isCanPlay = true;
				isNeedAllowPreSyn = false;
			}
			else {
				if (!StringUtils.isEmptyString(manualUrl)) {
					createMediaPlayerIfNeeded();
					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mPlayer.setDataSource(manualUrl);
					isCanPlay = true;
				}
			}
			if (!isCanPlay) {
				return;
			}
			mState = State.PREPARING;
			setUpAsForeground();

			MediaButtonHelper.registerMediaButtonEventReceiverCompat(mAudioManager, mMediaButtonReceiverComponent);
			if (mRemoteControlClientCompat == null) {
				Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				intent.setComponent(mMediaButtonReceiverComponent);
				mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(this, 0, intent, 0));
				RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);
			}

			mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			mRemoteControlClientCompat.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
					| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT | RemoteControlClient.FLAG_KEY_MEDIA_STOP);

			mRemoteControlClientCompat.editMetadata(true).putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mCurrentTrack.getUsername())
					.putString(MediaMetadataRetriever.METADATA_KEY_AUTHOR, mCurrentTrack.getUsername()).putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mSongTitle)
					.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, mCurrentTrack.getDuration()).apply();

			if (isNeedAllowPreSyn) {
				mPlayer.prepareAsync();
			}
			mWifiLock.acquire();

		}
		catch (IOException ex) {
			DBLog.d("MusicService", "IOException playing next song: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		mState = State.STOPPED;
		SettingManager.setPlayingState(this, false);
		if (SettingManager.getRepeat(this)) {
			playNextSong();
		}
		else {
			processNextRequest();
		}
		broadcastAction(getPackageName() + ACTION_NEXT);
	}

	@Override
	/** Called when media player is done preparing. */
	public void onPrepared(MediaPlayer player) {
		broadcastAction(getPackageName() + ACTION_DIMISS_LOADING);
		mState = State.PLAYING;
		initEqualizer();

		configAndStartMediaPlayer();
		updateNotification(mSongTitle);
	}

	private void updateNotification(String text) {
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}

	private void updateStatusPlayPause() {
		if (mPlayer != null) {
			if (notificationView != null) {
				if (mBitmapTrack != null) {
					notificationView.setImageViewBitmap(R.id.img_play, mBitmapTrack);
				}
				else {
					notificationView.setImageViewResource(R.id.img_play, R.drawable.ic_music_default);
				}
				notificationView.setImageViewResource(R.id.btn_play, mPlayer.isPlaying() ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
				mNotificationManager.notify(NOTIFICATION_ID, mNotification);
			}
		}
	}

	private void setUpAsForeground() {
		if (mCurrentTrack == null) {
			return;
		}
		Intent mIntent = new Intent(this.getApplicationContext(), MainActivity.class);
		mIntent.putExtra(KEY_SONG_ID, mCurrentTrack.getId());
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		mNotification = new Notification(R.drawable.mr_ic_play_light, mCurrentTrack.getTitle(), System.currentTimeMillis());
		notificationView = new RemoteViews(getPackageName(), R.layout.item_small_notification_music);
		notificationView.setTextViewText(R.id.tv_song, mCurrentTrack.getTitle());
		notificationView.setTextViewText(R.id.tv_singer, StringUtils.isEmptyString(mCurrentTrack.getUsername()) ? getString(R.string.title_unknown) : mCurrentTrack.getUsername());
		notificationView.setImageViewResource(R.id.btn_play, R.drawable.ic_pause_white_36dp);

		if (mBitmapTrack != null) {
			notificationView.setImageViewBitmap(R.id.img_play, mBitmapTrack);
		}
		else {
			notificationView.setImageViewResource(R.id.img_play, R.drawable.ic_music_default);
		}

		String packageName = getPackageName();

		Intent toggleIntent = new Intent(packageName + ACTION_TOGGLE_PLAYBACK);
		PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 100, toggleIntent, 0);
		notificationView.setOnClickPendingIntent(R.id.btn_play, pendingToggleIntent);

		Intent nextIntent = new Intent(packageName + ACTION_NEXT);
		PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 100, nextIntent, 0);
		notificationView.setOnClickPendingIntent(R.id.btn_next, pendingNextIntent);

		Intent prevIntent = new Intent(packageName + ACTION_PREVIOUS);
		PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(this, 100, prevIntent, 0);
		notificationView.setOnClickPendingIntent(R.id.btn_prev, pendingPrevIntent);

		Intent stopIntent = new Intent(packageName + ACTION_STOP);
		PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 100, stopIntent, 0);
		notificationView.setOnClickPendingIntent(R.id.btn_close, pendingStopIntent);

		mNotification.contentView = notificationView;
		mNotification.contentIntent = pi;
		mNotification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFICATION_ID, mNotification);
	}

	/**
	 * Called when there's an error playing media. When this happens, the media
	 * player goes to the Error state. We warn the user about the error and
	 * reset the media player.
	 */

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		DBLog.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
		broadcastAction(getPackageName() + ACTION_DIMISS_LOADING);
		mState = State.PAUSED;
		processStopRequest(true);
		return true;
	}

	public void onGainedAudioFocus() {
		mAudioFocus = AudioFocus.FOCUSED;
		if (mState == State.PLAYING) {
			configAndStartMediaPlayer();
		}
	}

	public void onLostAudioFocus(boolean canDuck) {
		mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;
		if (mPlayer != null && mPlayer.isPlaying()) {
			configAndStartMediaPlayer();
		}
	}

	private void startUpdatePosition() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mPlayer != null && mCurrentTrack != null) {
					int current = mPlayer.getCurrentPosition();
					Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
					mIntent.putExtra(KEY_POSITION, current);
					mIntent.putExtra(KEY_ACTION, getPackageName() + ACTION_UPDATE_POS);
					sendBroadcast(mIntent);
					if (current < mCurrentTrack.getDuration()) {
						startUpdatePosition();
					}
				}
			}
		}, 1000);
	}

	@Override
	public void onDestroy() {
		onDestroyBitmap();
		mHandler.removeCallbacksAndMessages(null);
		mState = State.STOPPED;
		try {
			relaxResources(true);
			giveUpAudioFocus();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
