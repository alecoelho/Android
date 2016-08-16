package com.ypyproductions.musicplayer.playerservice;

public interface IMusicFocusableListener {
	public void onGainedAudioFocus();
	public void onLostAudioFocus(boolean canDuck);
}
