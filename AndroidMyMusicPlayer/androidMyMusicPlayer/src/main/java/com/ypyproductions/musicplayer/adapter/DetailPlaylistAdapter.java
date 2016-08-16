package com.ypyproductions.musicplayer.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ypyproductions.abtractclass.DBBaseAdapter;
import com.ypyproductions.materialdialogs.MaterialDialog;
import com.ypyproductions.materialdialogs.MaterialDialog.ButtonCallback;
import com.ypyproductions.materialdialogs.MaterialDialog.ListCallback;
import com.ypyproductions.musicplayer.R;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.object.TrackObject;
import com.ypyproductions.utils.StringUtils;

/**
 * 
 *
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.ypyproductions.com
 * @Project:AndroidCloundMusicPlayer
 * @Date:Dec 14, 2014 
 *
 */
public class DetailPlaylistAdapter extends DBBaseAdapter implements IMyMusicPlayerConstants {
	public static final String TAG = DetailPlaylistAdapter.class.getSimpleName();

	private Typeface mTypefaceBold;

	private DisplayImageOptions mImgOptions;
	private IDetailPlaylistAdapterListener trackAdapter;

	private Typeface mTypefaceLight;

	public DetailPlaylistAdapter(Activity mContext, ArrayList<TrackObject> listDrawerObjects, 
			Typeface mTypefaceBold, Typeface mTypefaceLight, DisplayImageOptions mImgOptions) {
		super(mContext, listDrawerObjects);
		this.mTypefaceBold = mTypefaceBold;
		this.mTypefaceLight = mTypefaceLight;

		this.mImgOptions = mImgOptions;
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
			convertView = mInflater.inflate(R.layout.item_detail_playlist, null);
			convertView.setTag(mHolder);

			mHolder.mImgSongs = (ImageView) convertView.findViewById(R.id.img_songs);
			mHolder.mImgMenu = (ImageView) convertView.findViewById(R.id.img_menu);
			
			mHolder.mTvSongName = (TextView) convertView.findViewById(R.id.tv_song);
			mHolder.mTvSinger = (TextView) convertView.findViewById(R.id.tv_singer);
			mHolder.mRootLayout = (RelativeLayout) convertView.findViewById(R.id.layout_root);

			mHolder.mTvSongName.setTypeface(mTypefaceBold);
			mHolder.mTvSinger.setTypeface(mTypefaceLight);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		final TrackObject mTrackObject = (TrackObject) mListObjects.get(position);
		mHolder.mTvSongName.setText(mTrackObject.getTitle());

		mHolder.mTvSinger.setText(mTrackObject.getUsername());

		if(!StringUtils.isEmptyString(mTrackObject.getPath())){
			Uri mUri = mTrackObject.getURI();
			if (mUri != null) {
				String uri = mUri.toString();
				ImageLoader.getInstance().displayImage(uri, mHolder.mImgSongs, mImgOptions);
			}
			else {
				mHolder.mImgSongs.setImageResource(R.drawable.music_note);
			}
		}
		
		mHolder.mRootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (trackAdapter != null) {
					trackAdapter.onPlayingTrack(mTrackObject);
				}
			}
		});
		mHolder.mImgMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDiaglogOptions(mTrackObject);
			}
		});
		return convertView;
	}
	
	public void showDiaglogOptions(final TrackObject mTrackObject) {
		MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mContext);
		mBuilder.backgroundColor(mContext.getResources().getColor(R.color.white));
		mBuilder.title(R.string.title_options);
		mBuilder.titleColor(mContext.getResources().getColor(R.color.black_text));
		mBuilder.items(R.array.list_track_playlist);
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
					if(trackAdapter!=null){
						trackAdapter.onRemoveToPlaylist(mTrackObject);
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


	public interface IDetailPlaylistAdapterListener {
		public void onRemoveToPlaylist(TrackObject mTrackObject);
		public void onPlayingTrack(TrackObject mTrackObject);
	}

	public void setDetailPlaylistAdapterListener(IDetailPlaylistAdapterListener trackAdapter) {
		this.trackAdapter = trackAdapter;
	}

	private static class ViewHolder {
		public RelativeLayout mRootLayout;
		public ImageView mImgSongs;
		public ImageView mImgMenu;
		public TextView mTvSongName;
		public TextView mTvSinger;
	}

}
