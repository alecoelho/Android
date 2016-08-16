
package com.ypyproductions.musicplayer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ypyproductions.musicplayer.R;

public class PresetAdapter extends ArrayAdapter<String>{

	private Context mContext;
	private String[] mListString;
	private Typeface mTypeFace;


	public PresetAdapter(Context context, int resource, String[] objects, Typeface mTypeFace) {
		super(context, resource, objects);
		this.mContext= context;
		this.mListString = objects;
		this.mTypeFace=mTypeFace;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		LayoutInflater mInflater;
		if (convertView == null) {
			mHolder = new ViewHolder();
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.item_preset_name, null);
			convertView.setTag(mHolder);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.mTvName =(TextView) convertView.findViewById(R.id.tv_name);
		mHolder.mTvName.setText(mListString[position]);
		mHolder.mTvName.setTypeface(mTypeFace);
		return convertView;
	}
	
	private static class ViewHolder {
		public TextView mTvName;
	}
	
	
}
