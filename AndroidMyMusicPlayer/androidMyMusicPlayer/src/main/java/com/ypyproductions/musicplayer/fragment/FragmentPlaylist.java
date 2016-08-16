package com.ypyproductions.musicplayer.fragment;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ypyproductions.abtractclass.fragment.DBFragment;
import com.ypyproductions.musicplayer.MainActivity;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.adapter.DetailPlaylistAdapter;
import com.ypyproductions.musicplayer.adapter.DetailPlaylistAdapter.IDetailPlaylistAdapterListener;
import com.ypyproductions.musicplayer.adapter.PlaylistAdapter;
import com.ypyproductions.musicplayer.adapter.PlaylistAdapter.OnPlaylistListener;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.dataMng.TotalDataManager;
import com.ypyproductions.musicplayer.object.PlaylistObject;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.task.DBTask;
import com.ypyproductions.task.IDBCallback;
import com.ypyproductions.task.IDBTaskListener;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.IOUtils;

/**
 * 
 * 
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.ypyproductions.com
 * @Project:MusicPlayer
 * @Date:Dec 25, 2014
 * 
 */
public class FragmentPlaylist extends DBFragment implements IMyMusicPlayerConstants {

	public static final String TAG = FragmentPlaylist.class.getSimpleName();
	private MainActivity mContext;
	private DBTask mDBTask;
	private ArrayList<PlaylistObject> mListPlaylistObjects;
	private PlaylistAdapter mPlaylistAdapter;
	private ListView mListViewPlaylist;

	private View mHeaderPlaylistView;
	private ListView mListViewDetailPlaylist;
	private TextView mTvNamePlaylist;
	private View mHeaderDetailPlaylistView;
	private Button mBtnBack;
	private DetailPlaylistAdapter mDetailPlaylistAdapter;

	@Override
	public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_playlist_home, container, false);
	}

	@Override
	public void findView() {
		//setAllowFindViewContinous(true);
		mContext = (MainActivity) getActivity();
		mListViewPlaylist = (ListView) mRootView.findViewById(R.id.list_playlist);
		mListViewDetailPlaylist = (ListView) mRootView.findViewById(R.id.list_detail_playlist);
		
		setUpHeaderForPlaylist();
		setUpHeaderForDetailPlaylist();
		
		startGetPlaylist();
	}

	private void setUpHeaderForPlaylist() {
		mHeaderPlaylistView = mRootView.findViewById(R.id.header_playlist);
		TextView mTvAddPlaylist = (TextView) mHeaderPlaylistView.findViewById(R.id.tv_add_new_playlist);
		mTvAddPlaylist.setTypeface(mContext.mTypefaceBold);
		
		mHeaderPlaylistView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialogPlaylist(false, null);
			}
		});
	}
	private void setUpHeaderForDetailPlaylist() {
		mHeaderDetailPlaylistView = mRootView.findViewById(R.id.header_detail_playlist);
		mTvNamePlaylist = (TextView) mHeaderDetailPlaylistView.findViewById(R.id.tv_name_playlist);
		mTvNamePlaylist.setTypeface(mContext.mTypefaceBold);
		mBtnBack =(Button) mHeaderDetailPlaylistView.findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backToPlaylist();
			}
		});
	}
	
	public boolean backToPlaylist(){
		if(mListViewDetailPlaylist.getVisibility()==View.VISIBLE){
			mListViewDetailPlaylist.setVisibility(View.GONE);
			mListViewPlaylist.setVisibility(View.VISIBLE);
			mHeaderDetailPlaylistView.setVisibility(View.GONE);
			mHeaderPlaylistView.setVisibility(View.VISIBLE);
			return true;
		}
		return false;
	
	}
	
	private void showDetailPlaylist(PlaylistObject mPlaylistObject){
		mListViewDetailPlaylist.setVisibility(View.VISIBLE);
		mListViewPlaylist.setVisibility(View.GONE);
		mHeaderDetailPlaylistView.setVisibility(View.VISIBLE);
		mHeaderPlaylistView.setVisibility(View.GONE);
		mTvNamePlaylist.setText(mPlaylistObject.getName());
		setUpInfoDetailPlaylist(mPlaylistObject);
		
	}
	private void startGetPlaylist() {
		if (!ApplicationUtils.hasSDcard()) {
			return;
		}
		ArrayList<PlaylistObject> mListPlaylist = TotalDataManager.getInstance().getListPlaylistObjects();
		if (mListPlaylist != null) {
			setUpInfoListPlaylist(mListPlaylist);
			return;
		}
		final File mFile = IOUtils.getDiskCacheDir(mContext, mContext.getString(R.string.app_name));
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		mDBTask = new DBTask(new IDBTaskListener() {

			private ArrayList<PlaylistObject> mListPlaylist;

			@Override
			public void onPreExcute() {
				mContext.showProgressDialog();
			}

			@Override
			public void onDoInBackground() {
				TotalDataManager.getInstance().readSavedTrack(mContext, mFile);
				TotalDataManager.getInstance().readPlaylistCached(mContext, mFile);
				mListPlaylist = TotalDataManager.getInstance().getListPlaylistObjects();
			}

			@Override
			public void onPostExcute() {
				mContext.dimissProgressDialog();
				setUpInfoListPlaylist(mListPlaylist);
			}

		});
		mDBTask.execute();
	}

	private void setUpInfoListPlaylist(ArrayList<PlaylistObject> mListPlaylistObjects) {
		this.mListPlaylistObjects = mListPlaylistObjects;
		if (this.mListPlaylistObjects != null) {
			mPlaylistAdapter = new PlaylistAdapter(mContext, mListPlaylistObjects, mContext.mTypefaceBold, mContext.mTypefaceLight);
			mListViewPlaylist.setAdapter(mPlaylistAdapter);
			mPlaylistAdapter.seOnPlaylistListener(new OnPlaylistListener() {

				@Override
				public void onRenamePlaylist(PlaylistObject mPlaylistObject) {
					createDialogPlaylist(true, mPlaylistObject);
				}

				@Override
				public void onPlayAllMusic(PlaylistObject mPlaylistObject) {
					final ArrayList<TrackObject> mListTrackObjects=mPlaylistObject.getListTrackObjects(); 
					if(mListTrackObjects!=null && mListTrackObjects.size()>0){
						SoundCloundDataMng.getInstance().setListPlayingTrackObjects(mListTrackObjects);
						mContext.setInfoForPlayingTrack(mListTrackObjects.get(0),true,true);
					}
					else{
						mContext.showToast(R.string.info_nosong_playlist);
					}
				}

				@Override
				public void onDeletePlaylist(PlaylistObject mPlaylistObject) {
					showDialogDelete(mPlaylistObject);
				}

				@Override
				public void onGoToDetail(PlaylistObject mPlaylistObject) {
					showDetailPlaylist(mPlaylistObject);
				}

			});
		}
	}
	
	private void setUpInfoDetailPlaylist(final PlaylistObject mPlaylistObject) {
		final ArrayList<TrackObject> mListTrackObjects=mPlaylistObject.getListTrackObjects(); 
		if (mListTrackObjects != null) {
			if(mDetailPlaylistAdapter==null){
				mDetailPlaylistAdapter = new DetailPlaylistAdapter(mContext, mListTrackObjects, mContext.mTypefaceBold, mContext.mTypefaceLight,mContext.mImgTrackOptions);
				mListViewDetailPlaylist.setAdapter(mDetailPlaylistAdapter);
				mDetailPlaylistAdapter.setDetailPlaylistAdapterListener(new IDetailPlaylistAdapterListener() {
					@Override
					public void onRemoveToPlaylist(TrackObject mTrackObject) {
						TotalDataManager.getInstance().removeTrackToPlaylist(mContext, mTrackObject, mPlaylistObject, new IDBCallback() {
							@Override
							public void onAction() {
								if(mDetailPlaylistAdapter!=null){
									mDetailPlaylistAdapter.notifyDataSetChanged();
								}
								if(mPlaylistAdapter!=null){
									mPlaylistAdapter.notifyDataSetChanged();
								}
							}
						});
					}
					
					@Override
					public void onPlayingTrack(TrackObject mTrackObject) {
						SoundCloundDataMng.getInstance().setListPlayingTrackObjects((ArrayList<TrackObject>) mDetailPlaylistAdapter.getListObjects());
						mContext.setInfoForPlayingTrack(mTrackObject,true,true);
					}
				});
			}
			else{
				mDetailPlaylistAdapter.setListObjects(mListTrackObjects, false);
			}
			
		}
	}

	private void showDialogDelete(final PlaylistObject mPlaylistObject) {
		mContext.showFullDialog(R.string.title_confirm, R.string.info_delete_playlist, R.string.title_ok, R.string.title_cancel, new IDBCallback() {
			@Override
			public void onAction() {
				TotalDataManager.getInstance().removePlaylistObject(mContext, mPlaylistObject);
				if (mPlaylistAdapter != null) {
					mPlaylistAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void createDialogPlaylist(final boolean isEdit, final PlaylistObject mPlaylistObject) {
		mContext.createDialogPlaylist(isEdit, mPlaylistObject, new IDBCallback() {
			@Override
			public void onAction() {
				notifyData();
			}
		});
	}

	public void notifyData() {
		if (mPlaylistAdapter != null) {
			mPlaylistAdapter.notifyDataSetChanged();
		}
	}

}
