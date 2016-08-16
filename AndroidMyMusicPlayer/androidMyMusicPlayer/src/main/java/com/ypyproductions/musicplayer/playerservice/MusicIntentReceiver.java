package com.ypyproductions.musicplayer.playerservice;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.utils.StringUtils;

public class MusicIntentReceiver extends BroadcastReceiver implements IMusicConstant {

	public static final String TAG = MusicIntentReceiver.class.getSimpleName();

	private ArrayList<TrackObject> mListTrack;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (StringUtils.isEmptyString(action)) {
			return;
		}
		mListTrack = SoundCloundDataMng.getInstance().getListPlayingTrackObjects();
		String packageName = context.getPackageName();
		if (action.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			context.startService(new Intent(packageName + MusicService.ACTION_PAUSE));
		}
		else if (action.equals(packageName + ACTION_NEXT)) {
			context.startService(new Intent(packageName + MusicService.ACTION_NEXT));
		}
		else if (action.equals(packageName + ACTION_TOGGLE_PLAYBACK)) {
			context.startService(new Intent(packageName + MusicService.ACTION_TOGGLE_PLAYBACK));
		}
		else if (action.equals(packageName + ACTION_PREVIOUS)) {
			context.startService(new Intent(packageName + MusicService.ACTION_PREVIOUS));
		}
		else if (action.equals(packageName + ACTION_STOP)) {
			context.startService(new Intent(packageName + MusicService.ACTION_STOP));
		}
		else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			if (mListTrack == null || mListTrack.size() == 0) {
				SoundCloundDataMng.getInstance().onDestroy();
				context.startService(new Intent(packageName + MusicService.ACTION_STOP));
				return;
			}
			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
			if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
				return;
			}
			switch (keyEvent.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				if (SettingManager.getOnline(context)) {
					context.startService(new Intent(packageName + MusicService.ACTION_TOGGLE_PLAYBACK));
				}
				else {
					context.startService(new Intent(packageName + MusicService.ACTION_STOP));
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				context.startService(new Intent(packageName + MusicService.ACTION_PLAY));
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				if (SettingManager.getOnline(context)) {
					context.startService(new Intent(packageName + MusicService.ACTION_PAUSE));
				}
				else {
					context.startService(new Intent(packageName + MusicService.ACTION_STOP));
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				context.startService(new Intent(packageName + MusicService.ACTION_STOP));
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				context.startService(new Intent(packageName + MusicService.ACTION_NEXT));
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				context.startService(new Intent(packageName + MusicService.ACTION_PREVIOUS));
				break;
			}
		}
	}
}
