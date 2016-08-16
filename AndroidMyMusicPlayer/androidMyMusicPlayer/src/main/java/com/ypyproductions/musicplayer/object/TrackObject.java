package com.ypyproductions.musicplayer.object;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentUris;
import android.net.Uri;

import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.utils.DateTimeUtils;

/**
 * 
 * TrackObject.java
 * 
 * @author :DOBAO
 * @Email :dotrungbao@gmail.com
 * @Skype :baopfiev_k50
 * @Phone :+84983028786
 * @Date :6 Oct, 2014
 * @project :Player de Musicas
 * @Package :com.ypyproductions.soundclound.object
 */
public class TrackObject implements IMyMusicPlayerConstants {
	private long id;
	private Date createdDate;
	private long duration;
	private String genre;
	private String title;
	private String username;
	private String album;

	private boolean isLocalMusic;
	private String path;

	public TrackObject(long id, String title, Date createdDate, long duration, String username, String album, String path) {
		super();
		this.id = id;
		this.title = title;
		this.createdDate = createdDate;
		this.duration = duration;
		this.username = username;
		this.album = album;
		this.path = path;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String artworkUrl) {
		this.album = artworkUrl;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isLocalMusic() {
		return isLocalMusic;
	}

	public void setLocalMusic(boolean isLocalMusic) {
		this.isLocalMusic = isLocalMusic;
	}

	public Uri getURI() {
		return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
	}

	public JSONObject toJsonObject() {
		JSONObject mJsonObject = new JSONObject();
		try {
			mJsonObject.put("id", id);
			mJsonObject.put("title", title);
			mJsonObject.put("username", username);
			mJsonObject.put("created_at", DateTimeUtils.convertDateToString(createdDate, DATE_PATTERN));
			mJsonObject.put("duration", duration);
			mJsonObject.put("album", album == null ? "" : album);
			mJsonObject.put("path", path);

			return mJsonObject;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	public TrackObject clone() {
		TrackObject mTrackObject = new TrackObject(id, title, createdDate, duration, username, album, path);
		mTrackObject.setCreatedDate(new Date());
		return mTrackObject;
	}
}
