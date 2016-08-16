package com.ypyproductions.musicplayer.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ypyproductions.abtractclass.DBBaseAdapter;
import com.ypyproductions.materialdialogs.MaterialDialog;
import com.ypyproductions.materialdialogs.MaterialDialog.ButtonCallback;
import com.ypyproductions.materialdialogs.MaterialDialog.ListCallback;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.object.PlaylistObject;


public class PlaylistAdapter extends DBBaseAdapter implements IMyMusicPlayerConstants {
	public static final String TAG = PlaylistAdapter.class.getSimpleName();

	private Typeface mTypefaceBold;

	private OnPlaylistListener onPlaylistListener;

	private Typeface mTypefaceLight;

	public PlaylistAdapter(Activity mContext, ArrayList<PlaylistObject> listPlaylistObjects, Typeface mTypefaceBold,Typeface mTypefaceLight) {
		super(mContext, listPlaylistObjects);
		this.mTypefaceBold = mTypefaceBold;
		this.mTypefaceLight=mTypefaceLight;
	}

	@Override
	public View getAnimatedView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	@Override
	public View getNormalView(int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		LayoutInflater mInflater;
		if (convertView == null) {
			mHolder = new ViewHolder();
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.item_playlist, null);
			convertView.setTag(mHolder);
			
			mHolder.mTvPlaylistName = (TextView) convertView.findViewById(R.id.tv_playlist_name);
			mHolder.mTvPlaylistName.setTypeface(mTypefaceBold);
			
			mHolder.mTvNumberMusic = (TextView) convertView.findViewById(R.id.tv_number_music);
			mHolder.mTvNumberMusic.setTypeface(mTypefaceLight);
			
			mHolder.mTvOrder = (TextView) convertView.findViewById(R.id.tv_number);
			mHolder.mTvOrder.setTypeface(mTypefaceLight);
			
			mHolder.mBtnMenu =(ImageView) convertView.findViewById(R.id.img_menu);
			
			mHolder.mLayoutRoot = (RelativeLayout) convertView.findViewById(R.id.layout_root);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		final PlaylistObject mPlaylistObject = (PlaylistObject) mListObjects.get(position);
		mHolder.mTvPlaylistName.setText(mPlaylistObject.getName());
		mHolder.mTvOrder.setText(String.valueOf(position+1)+".");
		
		int size = mPlaylistObject.getListTrackObjects()!=null?mPlaylistObject.getListTrackObjects().size():0;
		String data="";
		if(size<=1){
			data = String.format(mContext.getString(R.string.format_number_music), size);
		}
		else{
			data = String.format(mContext.getString(R.string.format_number_musics), size);
		}
		mHolder.mTvNumberMusic.setText(data);
		mHolder.mLayoutRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onPlaylistListener!=null){
					onPlaylistListener.onGoToDetail(mPlaylistObject);
				}
			}
		});
		mHolder.mBtnMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDiaglogOptions(mPlaylistObject);
			}
		});
		return convertView;
	}
	
	public void showDiaglogOptions(final PlaylistObject mPlaylistObject) {
		MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mContext);
		mBuilder.backgroundColor(mContext.getResources().getColor(R.color.white));
		mBuilder.title(R.string.title_options);
		mBuilder.titleColor(mContext.getResources().getColor(R.color.black_text));
		mBuilder.items(R.array.list_options_playlist);
		mBuilder.contentColor(mContext.getResources().getColor(R.color.black_text));
		mBuilder.positiveColor(mContext.getResources().getColor(R.color.main_color));
		mBuilder.negativeColor(mContext.getResources().getColor(R.color.black_secondary_text));
		mBuilder.positiveText(R.string.title_cancel);
		mBuilder.autoDismiss(true);
		mBuilder.typeface(mTypefaceBold, mTypefaceLight);
		mBuilder.itemsCallback(new ListCallback() {
			
			@Override
			public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
				if (which == 0) {
					if(onPlaylistListener!=null){
						onPlaylistListener.onPlayAllMusic(mPlaylistObject);
					}
				}
				else if (which == 1) {
					if(onPlaylistListener!=null){
						onPlaylistListener.onRenamePlaylist(mPlaylistObject);
					}
				}
				else if (which == 2) {
					if(onPlaylistListener!=null){
						onPlaylistListener.onDeletePlaylist(mPlaylistObject);
					}
				}
			}
		});
		mBuilder.callback(new ButtonCallback() {
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);
			}
		});
		mBuilder.build().show();
	}
	
	public void seOnPlaylistListener(OnPlaylistListener onPlaylistListener) {
		this.onPlaylistListener = onPlaylistListener;
	}

	public interface OnPlaylistListener{
		public void onGoToDetail(PlaylistObject mPlaylistObject);
		public void onPlayAllMusic(PlaylistObject mPlaylistObject);
		public void onRenamePlaylist(PlaylistObject mPlaylistObject);
		public void onDeletePlaylist(PlaylistObject mPlaylistObject);
	}

	private static class ViewHolder {
		public TextView mTvPlaylistName;
		public TextView mTvNumberMusic;
		public TextView mTvOrder;
		public ImageView mBtnMenu;
		public RelativeLayout mLayoutRoot;
	}

}
