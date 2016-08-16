package com.ypyproductions.musicplayer.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Setting Manager
 * 
 * @author Nov 16, 2012
 * 
 * 
 */
public class SettingManager implements ISettingConstants {

	public static final String TAG = SettingManager.class.getSimpleName();

	public static final String DOBAO_SHARPREFS = "dobao_prefs";

	public static void saveSetting(Context mContext, String mKey, String mValue) {
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(DOBAO_SHARPREFS, Context.MODE_PRIVATE);
		Editor editor = mSharedPreferences.edit();
		editor.putString(mKey, mValue);
		editor.commit();
	}
	
	public static int getSearchType(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_SEARCH_TYPE, String.valueOf(TYPE_SEARCH_TEXT)));
	}
	
	public static void setSearchType(Context mContext, int mValue){
		saveSetting(mContext, KEY_SEARCH_TYPE, String.valueOf(mValue));
	}

	public static String getSetting(Context mContext, String mKey, String mDefValue) {
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(DOBAO_SHARPREFS, Context.MODE_PRIVATE);
		return mSharedPreferences.getString(mKey, mDefValue);
	}

	public static boolean getFirstTime(Context mContext) {
		return Boolean.parseBoolean(getSetting(mContext, KEY_FIRST_TIME, "false"));
	}

	public static void setFirstTime(Context mContext, boolean mValue) {
		saveSetting(mContext, KEY_FIRST_TIME, String.valueOf(mValue));
	}

	public static String getLastKeyword(Context mContext) {
		return getSetting(mContext, KEY_LAST_KEYWORD, "Touliver");
	}

	public static void setLastKeyword(Context mContext, String mValue) {
		saveSetting(mContext, KEY_LAST_KEYWORD, mValue);
	}
	public static String getLanguage(Context mContext) {
		return getSetting(mContext, KEY_LANGUAGE, "VN");
	}
	
	public static void setLanguage(Context mContext, String mValue) {
		saveSetting(mContext, KEY_LANGUAGE, mValue);
	}
	public static void setOnline(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_IS_ONLINE, String.valueOf(mValue));
	}
	
	public static boolean getOnline(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_IS_ONLINE, "false"));
	}
	
	public static boolean getEqualizer(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_EQUALIZER_ON, "false"));
	}
	
	public static void setEqualizer(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_EQUALIZER_ON, String.valueOf(mValue));
	}

	public static String getEqualizerPreset(Context mContext){
		return getSetting(mContext, KEY_EQUALIZER_PRESET, "0");
	}
	
	public static void setEqualizerPreset(Context mContext, String mValue){
		saveSetting(mContext, KEY_EQUALIZER_PRESET, mValue);
	}
	
	public static String getEqualizerParams(Context mContext){
		return getSetting(mContext, KEY_EQUALIZER_PARAMS, "");
	}
	
	public static void setEqualizerParams(Context mContext, String mValue){
		saveSetting(mContext, KEY_EQUALIZER_PARAMS, mValue);
	}
	
	public static void setRepeat(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_REPEAT, String.valueOf(mValue));
	}
	public static boolean getRepeat(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_REPEAT, "false"));
	}
	
	public static void setShuffle(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_SHUFFLE, String.valueOf(mValue));
	}
	public static boolean getShuffle(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_SHUFFLE, "true"));
	}
	
	public static boolean getPlayingState(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_STATE, "false"));
	}
	
	public static void setPlayingState(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_STATE, String.valueOf(mValue));
	}
}
