package com.ypyproductions.musicplayer;

import java.util.ArrayList;

import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ypyproductions.musicplayer.adapter.PresetAdapter;
import com.ypyproductions.musicplayer.dataMng.SoundCloundDataMng;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.musicplayer.view.SliderView;
import com.ypyproductions.musicplayer.view.SliderView.OnValueChangedListener;
import com.ypyproductions.musicplayer.view.SwitchView;
import com.ypyproductions.musicplayer.view.SwitchView.OnCheckListener;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.StringUtils;


public class EqualizerActivity extends DBFragmentActivity {

	public static final String TAG = EqualizerActivity.class.getSimpleName();

	private LinearLayout mLayoutBands;
	private Spinner mSpinnerPresents;
	private SwitchView mSwitchBtn;

	private MediaPlayer mMediaPlayer;

	private Equalizer mEqualizer;

	private String[] mLists;
	private ArrayList<SliderView> listSeekBars = new ArrayList<SliderView>();

	private short bands;

	private short minEQLevel;

	private short maxEQLevel;

	private String[] mEqualizerParams;

	private boolean isCreateLocal;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		this.setContentView(R.layout.activity_equalizer);
		setTitle(R.string.title_equalizer);
		
		mLayoutBands = (LinearLayout) findViewById(R.id.layout_bands);
		mSpinnerPresents = (Spinner) findViewById(R.id.list_preset);
		mSwitchBtn = (SwitchView) findViewById(R.id.switch1);
		
		mSwitchBtn.setOncheckListener(new OnCheckListener() {
			@Override
			public void onCheck(boolean check) {
				SettingManager.setEqualizer(EqualizerActivity.this, check);
				startCheckEqualizer();
			}
		});
		
		mMediaPlayer = SoundCloundDataMng.getInstance().getPlayer();
		if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
			isCreateLocal=true;
			mMediaPlayer = new MediaPlayer();
		}
		setupEqualizerFxAndUI();
		setUpPresetName();
		startCheckEqualizer();
		setUpParams();
	}
	
	private void setUpParams(){
		if(mEqualizer!=null){
			String presetStr = SettingManager.getEqualizerPreset(this);
			if(!StringUtils.isEmptyString(presetStr)){
				if(StringUtils.isNumber(presetStr)){
					short preset = Short.parseShort(presetStr);
					short numberPreset = mEqualizer.getNumberOfPresets();
					if(numberPreset>0){
						if(preset<numberPreset-1 && preset>=0){
							mEqualizer.usePreset(preset);
							mSpinnerPresents.setSelection(preset);
							return;
						}
					}
				}
			}
			setUpEqualizerCustom();
		}
	}
	
	private void setUpEqualizerCustom(){
		if(mEqualizer!=null){
			String params = SettingManager.getEqualizerParams(this);
			if(!StringUtils.isEmptyString(params)){
				mEqualizerParams = params.split(":");
				if(mEqualizerParams!=null && mEqualizerParams.length>0){
					int size = mEqualizerParams.length;
					for(int i=0;i<size;i++){
						mEqualizer.setBandLevel((short)i, Short.parseShort(mEqualizerParams[i]));
						listSeekBars.get(i).setValue(Short.parseShort(mEqualizerParams[i])-minEQLevel);
					}
					mSpinnerPresents.setSelection(mLists.length-1);
					SettingManager.setEqualizerPreset(this,String.valueOf(mLists.length-1));
				}
			}
		}
	}
	
	private void saveEqualizerParams(){
		if(mEqualizer!=null){
			if(bands>0){
				String data="";
				for(short i=0;i<bands;i++){
					if(i<bands-1){
						data=data+mEqualizer.getBandLevel(i)+":";
					}
				}
				DBLog.d(TAG, "================>dataSave="+data);
				SettingManager.setEqualizerPreset(this,String.valueOf(mLists.length-1));
				SettingManager.setEqualizerParams(this, data);
			}
		}
	}
	
	private void startCheckEqualizer(){
		boolean b = SettingManager.getEqualizer(this);
		mSpinnerPresents.setEnabled(b);
		if(mEqualizer!=null){
			mEqualizer.setEnabled(b);
		}
		if(listSeekBars.size()>0){
			for(int i=0;i<listSeekBars.size();i++){
				listSeekBars.get(i).setEnabled(b);
			}
		}
		mSwitchBtn.setChecked(b);
	}

	private void setupEqualizerFxAndUI() {
		mEqualizer = SoundCloundDataMng.getInstance().getEqualizer();
		if(mEqualizer==null){
			mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
			mEqualizer.setEnabled(SettingManager.getEqualizer(this));
		}
		bands = mEqualizer.getNumberOfBands();
		if(bands==0){
			return;
		}
		short[] bandRange= mEqualizer.getBandLevelRange();
		if(bandRange==null || bandRange.length<2){
			return;
		}
		minEQLevel = bandRange[0];
		maxEQLevel = bandRange[1];

		for (short i = 0; i < bands; i++) {
			final short band = i;

			TextView freqTextView = new TextView(this);
			freqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
			freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
			freqTextView.setTextColor(getResources().getColor(R.color.black));
			freqTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

			mLayoutBands.addView(freqTextView);

			LinearLayout row = new LinearLayout(this);
			row.setOrientation(LinearLayout.HORIZONTAL);

			TextView minDbTextView = new TextView(this);
			minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			minDbTextView.setText((minEQLevel / 100) + " dB");

			minDbTextView.setTextColor(getResources().getColor(R.color.black));
			minDbTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

			TextView maxDbTextView = new TextView(this);
			maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			maxDbTextView.setText((maxEQLevel / 100) + " dB");

			maxDbTextView.setTextColor(getResources().getColor(R.color.black));
			maxDbTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.weight = 1;
			
			SliderView mSliderView = new SliderView(this);
			mSliderView.setMax(maxEQLevel - minEQLevel);
			mSliderView.setMin(0);
			mSliderView.setShowNumberIndicator(false);
			mSliderView.setValue(mEqualizer.getBandLevel(band)-minEQLevel);
			mSliderView.setBackgroundColor(getResources().getColor(R.color.main_color));
			mSliderView.setLayoutParams(layoutParams);
			
			mSliderView.setOnValueChangedListener(new OnValueChangedListener() {
				@Override
				public void onValueChanged(int value) {
					mEqualizer.setBandLevel(band, (short) (value + minEQLevel));
					saveEqualizerParams();
					mSpinnerPresents.setSelection(mLists.length-1);
				}
			});
			listSeekBars.add(mSliderView);

			row.addView(minDbTextView);
			row.addView(mSliderView);
			row.addView(maxDbTextView);
			mLayoutBands.addView(row);
		}
	}

	private void setUpPresetName() {
		if (mEqualizer != null) {
			short numberPreset = mEqualizer.getNumberOfPresets();
			if (numberPreset > 0) {
				mLists = new String[numberPreset+1];
				for (short i = 0; i < numberPreset; i++) {
					mLists[i] = mEqualizer.getPresetName(i);
				}
				mLists[numberPreset]=getString(R.string.title_custom);
				
				PresetAdapter dataAdapter = new PresetAdapter(this, R.layout.item_preset_name, mLists, mTypefaceNormal);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mSpinnerPresents.setAdapter(dataAdapter);
				
				mSpinnerPresents.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						SettingManager.setEqualizerPreset(EqualizerActivity.this, String.valueOf(position));
						if(position<mLists.length-1){
							mEqualizer.usePreset((short) position);
						}
						else{
							setUpEqualizerCustom();
						}
						for (short i = 0; i < bands; i++){
							SliderView bar = listSeekBars.get(i);
							bar.setValue(mEqualizer.getBandLevel(i)-minEQLevel);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				});
			}
			else{
				mSpinnerPresents.setVisibility(View.INVISIBLE);
			}
		}
		else{
			mSpinnerPresents.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToHome();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backToHome();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void backToHome() {
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (listSeekBars != null) {
			listSeekBars.clear();
			listSeekBars = null;
		}
		if(isCreateLocal){
			if(mMediaPlayer!=null){
				mMediaPlayer.release();
				mMediaPlayer=null;
			}
		}
	}

}
