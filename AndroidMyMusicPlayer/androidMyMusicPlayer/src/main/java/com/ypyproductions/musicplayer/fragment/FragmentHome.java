package com.ypyproductions.musicplayer.fragment;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ypyproductions.abtractclass.fragment.DBFragment;
import com.ypyproductions.musicplayer.MainActivity;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.adapter.LibraryAdapter;
import com.ypyproductions.musicplayer.adapter.LibraryAdapter.OnLibraryListener;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.dataMng.TotalDataManager;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.utils.DBListExcuteAction;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.StringUtils;

public class FragmentHome extends DBFragment implements IMyMusicPlayerConstants{

	public static final String TAG = FragmentHome.class.getSimpleName();

	private TextView mTvNoResult;
	private ListView mListView;

	private MainActivity mContext;

	private LibraryAdapter mAdapter;

	private ArrayList<TrackObject> mListTrackObjects;


	@Override
	public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void findView() {
		//setAllowFindViewContinous(true);
		this.mContext = (MainActivity) getActivity();
		this.mListView = (ListView) mRootView.findViewById(R.id.list_tracks);
		this.mTvNoResult = (TextView) mRootView.findViewById(R.id.tv_no_result);
		this.mTvNoResult.setTypeface(mContext.mTypefaceNormal);
		setUpFullData();
	}
	
	public void setUpFullData(){
		ArrayList<TrackObject> mListDownloaded = TotalDataManager.getInstance().getListLibraryTrackObjects();
		setUpInfo(mListDownloaded);
	}
	
	public void startGetData(final String keyword) {
		if(StringUtils.isEmptyString(keyword)){
			setUpInfo(new ArrayList<TrackObject>());
			return;
		}
		final ArrayList<TrackObject> mSearchListTrack = TotalDataManager.getInstance().searchTrackObjects(keyword);
		DBLog.d(TAG, "================>startGetData="+(mSearchListTrack!=null?mSearchListTrack.size():0));
		mContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTvNoResult.setVisibility(View.GONE);
				setUpInfo(mSearchListTrack);
			}
		});
		
		
	}

	private void setUpInfo(final ArrayList<TrackObject> mListNewTrackObjects) {
		this.mListTrackObjects = mListNewTrackObjects;

		if (mListNewTrackObjects != null) {
			this.mTvNoResult.setVisibility(mListNewTrackObjects.size() > 0?View.GONE:View.VISIBLE);
			this.mListView.setVisibility(View.VISIBLE);
			
			mAdapter = new LibraryAdapter(mContext, mListNewTrackObjects, mContext.mTypefaceBold, 
					mContext.mTypefaceLight,mContext.mImgTrackOptions);
			mListView.setAdapter(mAdapter);
			
			mAdapter.setOnLibraryListener(new OnLibraryListener() {
				@Override
				public void onPlayItem(TrackObject mTrackObject) {
					mContext.hiddenVirtualKeyBoard();	
					SoundCloundDataMng.getInstance().setListPlayingTrackObjects(mListNewTrackObjects);
					mContext.setInfoForPlayingTrack(mTrackObject,true,true);
				}
				
				@Override
				public void onDeleteItem(final TrackObject mTrackObject) {
					mContext.showFullDialog(R.string.title_confirm, R.string.info_delete_songs, R.string.title_ok, 
							R.string.title_cancel, new IDBCallback() {
						@Override
						public void onAction() {
							mContext.showProgressDialog();
							final File mOutPutFile = new File(mTrackObject.getPath());
							if (mOutPutFile.exists() && mOutPutFile.isFile()) {
								DBListExcuteAction.getInstance().queueAction(new IDBCallback() {
									@Override
									public void onAction() {
										boolean b = mOutPutFile.delete();
										if (b) {
											mContext.deleteSongFromMediaStore(mTrackObject.getId());
											mListTrackObjects.remove(mTrackObject);
										}
										mContext.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												mContext.dimissProgressDialog();
												notifyData();
											}
										});
									}
								});
							}
						}
					});
				}

				@Override
				public void onSetAsRingtone(TrackObject mTrackObject) {
					saveAsRingtone(mTrackObject);
				}

				@Override
				public void onSetAsNotification(TrackObject mTrackObject) {
					saveAsNotification(mTrackObject);
				}

				@Override
				public void onAddToPlaylist(TrackObject mTrackObject) {
					mContext.showDialogPlaylist(mTrackObject);
				}

				@Override
				public void onRenameItem(TrackObject mTrackObject) {
					mContext.showDialogTrack(mTrackObject);
				}
			});
		}
		else {
			this.mTvNoResult.setVisibility(View.VISIBLE);
		}
	}
	
	
	public void notifyData(){
		if(mAdapter!=null){
			mAdapter.notifyDataSetChanged();
		}
		mTvNoResult.setVisibility((mListTrackObjects!=null && mListTrackObjects.size() > 0)?View.GONE:View.VISIBLE);
	}
	
	private void saveAsRingtone(TrackObject mSongObject) {
		final File mOutPutFile = new File(mSongObject.getPath());
		if (mOutPutFile != null && mOutPutFile.isFile()) {
			Uri mUri=null;
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, mOutPutFile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, mSongObject.getTitle());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			
			String id=getIdFromContentUri(mOutPutFile.getAbsolutePath());
			if(StringUtils.isEmptyString(id)){
				mUri = mContext.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(mOutPutFile.getAbsolutePath()), values);
			}
			else{
				mContext.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
						, values, MediaColumns._ID+" = ?", new String[]{id});
				mUri=Uri.parse(String.format(FORMAT_URI, id));
			}
			RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, mUri);
			mContext.showToast(R.string.info_set_ringtone_successfully);

		}
	}

	private void saveAsNotification(TrackObject mSongObject) {
		final File mOutPutFile = new File(mSongObject.getPath());
		if (mOutPutFile != null && mOutPutFile.isFile()) {
			Uri mUri=null;
			
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, mOutPutFile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, mSongObject.getTitle());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
			
			String id=getIdFromContentUri(mOutPutFile.getAbsolutePath());
			if(StringUtils.isEmptyString(id)){
				mUri = mContext.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(mOutPutFile.getAbsolutePath()), values);
			}
			else{
				mContext.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
						, values, MediaColumns._ID+" = ?", new String[]{id});
				mUri=Uri.parse(String.format(FORMAT_URI, id));
			}
			
			RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, mUri);
			mContext.showToast(R.string.info_set_notification_successfully);

		}
	}
	
	private String getIdFromContentUri(String path) {
		try {
			if(path!=null){
				String id;
				String[] filePathColumn = {MediaColumns._ID};
				String[] selectionArgs = {path};
				Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
						filePathColumn, MediaColumns.DATA+" = ?", selectionArgs, null);
				if(cursor!=null && cursor.moveToFirst()){
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					id = cursor.getString(columnIndex);
					cursor.close();
					return id;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    return null;
	}

}
