package com.ypyproductions.musicplayer.dataMng;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.ypyproductions.musicplayer.DBFragmentActivity;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.object.PlaylistObject;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.IOUtils;
import com.ypyproductions.utils.StringUtils;

public class TotalDataManager implements IMyMusicPlayerConstants {

	public static final String TAG = TotalDataManager.class.getSimpleName();

	private static TotalDataManager totalDataManager;

	private ArrayList<TrackObject> listLibraryTrackObjects;
	private ArrayList<TrackObject> listCurrrentTrackObjects;
	private ArrayList<PlaylistObject> listPlaylistObjects;
	private ArrayList<TrackObject> listSavedTrackObjects;

	public static TotalDataManager getInstance() {
		if (totalDataManager == null) {
			totalDataManager = new TotalDataManager();
		}
		return totalDataManager;
	}

	private TotalDataManager() {

	}

	public void onDestroy() {
		if (listLibraryTrackObjects != null) {
			listLibraryTrackObjects.clear();
			listLibraryTrackObjects = null;
		}
		if (listPlaylistObjects != null) {
			listPlaylistObjects.clear();
			listPlaylistObjects = null;
		}
		if (listCurrrentTrackObjects != null) {
			listCurrrentTrackObjects.clear();
			listCurrrentTrackObjects = null;
		}
		if (listSavedTrackObjects != null) {
			listSavedTrackObjects.clear();
			listSavedTrackObjects = null;
		}
		totalDataManager = null;
	}

	public ArrayList<TrackObject> getListCurrrentTrackObjects() {
		return listCurrrentTrackObjects;
	}

	public void setListCurrrentTrackObjects(ArrayList<TrackObject> listCurrrentTrackObjects) {
		this.listCurrrentTrackObjects = listCurrrentTrackObjects;
	}

	public ArrayList<TrackObject> getListLibraryTrackObjects() {
		return listLibraryTrackObjects;
	}

	public void setListLibraryTrackObjects(ArrayList<TrackObject> listLibraryTrackObjects) {
		this.listLibraryTrackObjects = listLibraryTrackObjects;
		if(listLibraryTrackObjects!=null && listLibraryTrackObjects.size()>0){
			Collections.sort(listLibraryTrackObjects, new Comparator<TrackObject>() {
				@Override
				public int compare(TrackObject lhs, TrackObject rhs) {
					String fullName1=lhs.getTitle();
		        	String fullName2= rhs.getTitle();
		        	if(fullName1!=null && fullName2!=null){
		        		return fullName1.compareToIgnoreCase(fullName2);
		        	}
		        	return -1;
				}
			});
		}
	}
	
	public ArrayList<TrackObject> searchTrackObjects(String query){
		if(listLibraryTrackObjects!=null && listLibraryTrackObjects.size()>0){
			ArrayList<TrackObject> listTrackObjects = new ArrayList<TrackObject>();
			String search= query.toLowerCase(Locale.US);
			for(TrackObject mTrackObject:listLibraryTrackObjects){
				String title = mTrackObject.getTitle().toLowerCase(Locale.US);
				String username= mTrackObject.getUsername().toLowerCase(Locale.US);
				if(title.contains(search) || username.contains(search)){
					listTrackObjects.add(mTrackObject);
				}
			}
			return listTrackObjects;
		}
		return null;
	}

	public void readLibraryTrack(Context mContext) {
		File mFile = new File(Environment.getExternalStorageDirectory(), NAME_FOLDER_LIBRARY);
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		ArrayList<TrackObject> mListSavedTrackObject = getListMusicsFromLibrary(mContext);// SoundCloundJsonParsingUtils.parsingListTrackObject(dataSaved);
		setListLibraryTrackObjects(mListSavedTrackObject == null ? new ArrayList<TrackObject>() : mListSavedTrackObject);
		DBLog.d(TAG, "========>mListSavedTrackObject=" + (mListSavedTrackObject != null ? mListSavedTrackObject.size() : 0));
	}

	private ArrayList<TrackObject> getListMusicsFromLibrary(Context mContext) {
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cur = mContext.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
		DBLog.d(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
		if (cur == null) {
			DBLog.d(TAG, "Failed to retrieve music: cursor is null :-(");
			return null;
		}
		if (!cur.moveToFirst()) {
			DBLog.d(TAG, "Failed to move cursor to first row (no query results).");
			return null;
		}
		int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
		int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
		int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
		int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
		int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
		int dataColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
		int dateColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

		ArrayList<TrackObject> listTrackObjects = new ArrayList<TrackObject>();
		do {
			DBLog.d(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
			long id = cur.getLong(idColumn);
			String singer = cur.getString(artistColumn);
			String title = cur.getString(titleColumn);
			long duration = cur.getLong(durationColumn);
			String album = cur.getString(albumColumn);
			String path = cur.getString(dataColumn);
			Date mDate = new Date(cur.getLong(dateColumn) * 1000);

			if (!StringUtils.isEmptyString(path)) {
				File mFile = new File(path);
				if (mFile.exists() && mFile.isFile()) {
					TrackObject mTrackObject = new TrackObject(id, title, mDate, duration, singer, album, path);
					listTrackObjects.add(mTrackObject);
				}
			}

		}
		while (cur.moveToNext());
		return listTrackObjects;

	}

	public void readPlaylistCached(Context mContext, File mFile) {
		String data = IOUtils.readString(mContext, mFile.getAbsolutePath(), NAME_PLAYLIST_FILE);
		ArrayList<PlaylistObject> mListPlaylist = JsonParsingUtils.parsingPlaylistObject(data);
		if (mListPlaylist != null && mListPlaylist.size() > 0) {
			setListPlaylistObjects(mListPlaylist);
		}
		else {
			mListPlaylist = new ArrayList<PlaylistObject>();
			setListPlaylistObjects(mListPlaylist);
		}
		for (PlaylistObject mPlaylistObject : mListPlaylist) {
			filterSongOfPlaylistId(mPlaylistObject);
		}
	}

	private void filterSongOfPlaylistId(PlaylistObject mPlaylistObject) {
		if (listSavedTrackObjects != null && listSavedTrackObjects.size() > 0) {
			ArrayList<Long> mListId = mPlaylistObject.getListTrackIds();
			if (mListId != null && mListId.size() > 0) {
				for (Long mId : mListId) {
					for (TrackObject mTrackObject : listSavedTrackObjects) {
						if (mTrackObject.getId() == mId) {
							mPlaylistObject.addTrackObject(mTrackObject, false);
							break;
						}
					}
				}
			}
		}
	}

	public void readSavedTrack(Context mContext, File mFile) {
		String dataSaved = IOUtils.readString(mContext, mFile.getAbsolutePath(), NAME_SAVED_TRACK);
		ArrayList<TrackObject> mListSavedTrackObject = JsonParsingUtils.parsingListSavingTrackObject(dataSaved);
		setListSavedTrackObjects(mListSavedTrackObject == null ? new ArrayList<TrackObject>() : mListSavedTrackObject);
		DBLog.d(TAG, "========>mListSavedTrackObject=" + (mListSavedTrackObject != null ? mListSavedTrackObject.size() : 0));
	}

	public ArrayList<PlaylistObject> getListPlaylistObjects() {
		return listPlaylistObjects;
	}

	public void setListPlaylistObjects(ArrayList<PlaylistObject> listPlaylistObjects) {
		this.listPlaylistObjects = listPlaylistObjects;
	}

	public void addPlaylistObject(final Context mContext, PlaylistObject mPlaylistObject) {
		if (listPlaylistObjects != null && mPlaylistObject != null) {
			listPlaylistObjects.add(mPlaylistObject);
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					savePlaylistObjects(mContext);
				}
			});
		}
	}

	public void editPlaylistObject(final Context mContext, PlaylistObject mPlaylistObject, String newName) {
		if (listPlaylistObjects != null && mPlaylistObject != null && !StringUtils.isEmptyString(newName)) {
			mPlaylistObject.setName(newName);
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					savePlaylistObjects(mContext);
				}
			});
		}
	}

	public void removePlaylistObject(final Context mContext, PlaylistObject mPlaylistObject) {
		if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
			listPlaylistObjects.remove(mPlaylistObject);
			ArrayList<TrackObject> mListTrack = mPlaylistObject.getListTrackObjects();
			boolean isNeedToSaveTrack = false;
			if (mListTrack != null && mListTrack.size() > 0) {
				for (TrackObject mTrackObject : mListTrack) {
					boolean isAllowRemoveToList = true;
					for (PlaylistObject mPlaylist : listPlaylistObjects) {
						if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
							isAllowRemoveToList = false;
							break;
						}
					}
					if (isAllowRemoveToList) {
						listSavedTrackObjects.remove(mTrackObject);
						isNeedToSaveTrack = true;
					}
				}
				mListTrack.clear();
			}
			mPlaylistObject = null;
			final boolean isGlobal = isNeedToSaveTrack;
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					savePlaylistObjects(mContext);
					if (isGlobal) {
						saveTrackObjects(mContext);
					}
				}
			});
		}
	}

	public void removePlaylistObject(final Context mContext, int playlistId) {
		if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
			Iterator<PlaylistObject> mListIterator = listPlaylistObjects.iterator();
			while (mListIterator.hasNext()) {
				PlaylistObject playlistObject = (PlaylistObject) mListIterator.next();
				if (playlistObject.getId() == playlistId) {
					mListIterator.remove();
					break;
				}
			}
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					savePlaylistObjects(mContext);
				}
			});
		}
	}

	public synchronized void addTrackToPlaylist(final DBFragmentActivity mContext, final TrackObject mParentTrackObject,
			final PlaylistObject mPlaylistObject, boolean isShowMsg, IDBCallback mCallback) {
		if (mParentTrackObject != null && mPlaylistObject != null) {
			if (!mPlaylistObject.isSongAlreadyExited(mParentTrackObject.getId())) {
				TrackObject mTrackObject = mParentTrackObject.clone();
				mPlaylistObject.addTrackObject(mTrackObject, true);

				boolean isAllowAddToList = true;
				for (TrackObject mTrackObject1 : listSavedTrackObjects) {
					if (mTrackObject1.getId() == mTrackObject.getId()) {
						isAllowAddToList = false;
					}
				}
				if (isAllowAddToList) {
					listSavedTrackObjects.add(mTrackObject);
				}
				if(mCallback!=null){
					mCallback.onAction();
				}
				mContext.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mContext.showToast(String.format(mContext.getString(R.string.info_add_playlist), mParentTrackObject.getTitle(), mPlaylistObject.getName()));
					}
				});
				DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
					@Override
					public void onAction() {
						savePlaylistObjects(mContext);
						saveTrackObjects(mContext);
					}
				});
			}
			else {
				if (isShowMsg) {
					mContext.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mContext.showToast(R.string.info_song_already_playlist);
						}
					});
				}

			}
		}
	}
	public synchronized void saveTrackObjects(Context mContext) {
		if (!ApplicationUtils.hasSDcard()) {
			return;
		}
		File mFile = IOUtils.getDiskCacheDir(mContext, mContext.getString(R.string.app_name));
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		JSONArray mJsArray = new JSONArray();
		if (listSavedTrackObjects != null && listSavedTrackObjects.size() > 0) {
			for (TrackObject mTrackObject : listSavedTrackObjects) {
				mJsArray.put(mTrackObject.toJsonObject());
			}
		}
		DBLog.d(TAG, "=============>saveTrackObjects=" + mJsArray.toString());
		IOUtils.writeString(mFile.getAbsolutePath(), NAME_SAVED_TRACK, mJsArray.toString());
	}
	public ArrayList<TrackObject> getListSavedTrackObjects() {
		return listSavedTrackObjects;
	}

	public void setListSavedTrackObjects(ArrayList<TrackObject> listSavedTrackObjects) {
		this.listSavedTrackObjects = listSavedTrackObjects;
	}

	public synchronized void savePlaylistObjects(Context mContext) {
		if (!ApplicationUtils.hasSDcard()) {
			return;
		}
		File mFile = IOUtils.getDiskCacheDir(mContext, mContext.getString(R.string.app_name));
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		JSONArray mJsArray = new JSONArray();
		if (listPlaylistObjects != null) {
			for (PlaylistObject mPlaylist : listPlaylistObjects) {
				mJsArray.put(mPlaylist.toJson());
			}
		}
		DBLog.d(TAG, "=============>savePlaylistObjects=" + mJsArray.toString());
		IOUtils.writeString(mFile.getAbsolutePath(), NAME_PLAYLIST_FILE, mJsArray.toString());
	}
	
	public synchronized void removeTrackToPlaylist(final DBFragmentActivity mContext, TrackObject mTrackObject, PlaylistObject mPlaylistObject, final IDBCallback mCallback) {
		if (mTrackObject != null && mPlaylistObject != null) {
			mPlaylistObject.removeTrackObject(mTrackObject);
			boolean isAllowRemoveToList = true;
			for (PlaylistObject mPlaylist : listPlaylistObjects) {
				if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
					isAllowRemoveToList = false;
					break;
				}
			}
			DBLog.d(TAG, "============>removeTrackToPlaylist=" + isAllowRemoveToList);
			if (isAllowRemoveToList) {
				listSavedTrackObjects.remove(mTrackObject);
				mTrackObject = null;
			}
			if(mCallback!=null){
				mCallback.onAction();
			}
			DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
				@Override
				public void onAction() {
					savePlaylistObjects(mContext);
					saveTrackObjects(mContext);
				}
			});
		}
	}

}
