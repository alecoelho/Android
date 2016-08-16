package com.ypyproductions.musicplayer.dataMng;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.object.PlaylistObject;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.utils.DateTimeUtils;
import com.ypyproductions.utils.StringUtils;

public class JsonParsingUtils implements IMyMusicPlayerConstants {

	public static final String TAG = JsonParsingUtils.class.getSimpleName();

	public static final String TAG_STATUS = "status";
	public static final String TAG_NAME = "name";
	public static final String TAG_IMAGE = "image";

	
	public static ArrayList<PlaylistObject> parsingPlaylistObject(String data) {
		if (!StringUtils.isEmptyString(data)) {
			try {
				JSONArray mJsonArray = new JSONArray(data);
				int size = mJsonArray.length();
				if (size > 0) {
					ArrayList<PlaylistObject> mListPlaylistObjects = new ArrayList<PlaylistObject>();
					for (int i = 0; i < size; i++) {
						JSONObject mJs = mJsonArray.getJSONObject(i);
						long id = mJs.getLong("id");
						String name = mJs.getString("name");

						PlaylistObject mPlaylistObject = new PlaylistObject(id, name);

						JSONArray mJsonArrayId = mJs.getJSONArray("tracks");
						int len = mJsonArrayId.length();
						ArrayList<Long> listIds = new ArrayList<Long>();
						if (len > 0) {
							for (int t = 0; t < len; t++) {
								listIds.add(mJsonArrayId.getLong(t));
							}
						}
						mPlaylistObject.setListTrackIds(listIds);
						mListPlaylistObjects.add(mPlaylistObject);

					}
					return mListPlaylistObjects;
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public static ArrayList<TrackObject> parsingListSavingTrackObject(String data) {
		if (!StringUtils.isEmptyString(data)) {
			try {
				JSONArray mJsonArray =new JSONArray(data);
				int size = mJsonArray.length();
				if (size > 0) {
					ArrayList<TrackObject> mListTrackObjects = new ArrayList<TrackObject>();
					for (int i = 0; i < size; i++) {
						JSONObject mJsonObject = mJsonArray.getJSONObject(i);
						
						long id = mJsonObject.getLong("id");
						long duration = mJsonObject.getLong("duration");
						String title = mJsonObject.getString("title");
						String username = mJsonObject.getString("username");
						String createdAt = mJsonObject.getString("created_at");
						Date mDate = DateTimeUtils.getDateFromString(createdAt, DATE_PATTERN);
						String album = mJsonObject.getString("album");
						
						String path = mJsonObject.getString("path");
						TrackObject mTrackObject = new TrackObject(id, title, mDate, duration, username, album, path);
						mListTrackObjects.add(mTrackObject);
						
					}
					return mListTrackObjects;
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
