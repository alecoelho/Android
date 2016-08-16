package com.ypyproductions.musicplayer.dataMng;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;

import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.utils.DBLog;

/**
 * 
 *
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.ypyproductions.com
 * @Project:YPlayer
 * @Date:Jan 20, 2015 
 *
 */
public class SoundCloundDataMng {
	
	public static final String TAG = SoundCloundDataMng.class.getSimpleName();
	private static SoundCloundDataMng instance;
	private ArrayList<TrackObject> listTrackObjects;
	
	private int currentIndex=-1;
	private TrackObject currentTrackObject;
	
	private Random mRandom;
	private MediaPlayer player;
	private Equalizer equalizer;

	private SoundCloundDataMng() {
		mRandom = new Random();
	}

	public static SoundCloundDataMng getInstance() {
		if (null == instance) {
			instance = new SoundCloundDataMng();
		}
		return instance;
	}
	
	public void onDestroy() {
		if (listTrackObjects != null) {
			listTrackObjects.clear();
			listTrackObjects = null;
		}
		mRandom=null;
		instance = null;
	}
	
	public void onResetMedia(){
		try {
			if(equalizer!=null){
				equalizer.release();
				equalizer=null;
			}
			if(player!=null){
				if(player.isPlaying()){
					player.stop();
				}
				player.release();
				player=null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setListPlayingTrackObjects(ArrayList<TrackObject> listTrackObjects) {
		if(listTrackObjects!=null){
			if(listTrackObjects.size()>0){
				boolean isNeedToReset=true;
				if(currentTrackObject!=null){
					for(TrackObject mTrackObject:listTrackObjects){
						if(currentTrackObject.getId()==mTrackObject.getId()){
							isNeedToReset=false;
							currentIndex=listTrackObjects.indexOf(mTrackObject);
							break;
						}
					}
					
				}
				if(isNeedToReset){
					currentIndex=0;
				}
			}
			else{
				currentIndex=-1;
			}
		}
		this.listTrackObjects = listTrackObjects;
	}
	
	public TrackObject getTrackObject(long id){
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			for(TrackObject mTrackObject:listTrackObjects){
				if(mTrackObject.getId()==id){
					return mTrackObject;
				}
			}
		}
		return null;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
		if(listTrackObjects!=null && listTrackObjects.size()>0 && currentIndex>=0 && currentIndex<listTrackObjects.size()){
			this.currentTrackObject = listTrackObjects.get(currentIndex);
		}
	}
	public boolean setCurrentIndex(TrackObject mTrackObject) {
		if(listTrackObjects!=null && listTrackObjects.size()>0 && mTrackObject!=null){
			this.currentTrackObject = mTrackObject;
			this.currentIndex = listTrackObjects.indexOf(mTrackObject);
			DBLog.d(TAG, "===========>mTrackObject="+mTrackObject.getId()+"===>currentIndex="+currentIndex);
			if(currentIndex<0){
				currentIndex=0;
				return false;
			}
			return true;
		}
		return false;
	}
	

	public ArrayList<TrackObject> getListPlayingTrackObjects() {
		return listTrackObjects;
	}
	
	public TrackObject getCurrentTrackObject(){
		return currentTrackObject;
	}
	
	public TrackObject getNextTrackObject(Context mContext){
		if(listTrackObjects!=null){
			int size = listTrackObjects.size();
			DBLog.d(TAG, "==========>currentIndex="+currentIndex);
			if( size>0 && currentIndex>=0 && currentIndex<=size){
				if(SettingManager.getShuffle(mContext)){
					currentIndex = mRandom.nextInt(size);
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
				else{
					currentIndex++;
					if(currentIndex>=size){
						currentIndex=0;
					}
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
			}
		}
		return null;
	}
	
	public TrackObject getPrevTrackObject(Context mContext){
		if(listTrackObjects!=null){
			int size = listTrackObjects.size();
			if( size>0 && currentIndex>=0 && currentIndex<=size){
				if(SettingManager.getShuffle(mContext)){
					currentIndex = mRandom.nextInt(size);
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
				else{
					currentIndex--;
					if(currentIndex<0){
						currentIndex=size-1;
					}
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
			}
		}
		return null;
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	public void setPlayer(MediaPlayer player) {
		this.player = player;
	}
	
	public Equalizer getEqualizer() {
		return equalizer;
	}

	public void setEqualizer(Equalizer equalizer) {
		this.equalizer = equalizer;
	}
	
}
