package com.ypyproductions.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ypyproductions.abtractclass.fragment.IDBFragmentConstants;
import com.ypyproductions.musicplayer.constants.IMyMusicPlayerConstants;
import com.ypyproductions.musicplayer.setting.SettingManager;
import com.ypyproductions.utils.ApplicationUtils;
import com.ypyproductions.utils.DBLog;
import com.ypyproductions.utils.StringUtils;


public class ShowUrlActivity extends DBFragmentActivity implements IMyMusicPlayerConstants
	, IDBFragmentConstants {

	public static final String TAG = ShowUrlActivity.class.getSimpleName();

	private ProgressBar mProgressBar;
	private WebView mWebViewShowPage;

	private String mUrl;

	private String mNameHeader;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		this.setContentView(R.layout.layout_show_url);

		Intent args = getIntent();
		if (args != null) {
			mUrl = args.getStringExtra(KEY_URL);
			mNameHeader = args.getStringExtra(KEY_HEADER);
			DBLog.d(TAG, "===========>url=" + mUrl);
		}
		if (!StringUtils.isEmptyString(mNameHeader)) {
			setTitle(mNameHeader);
		}

		this.mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		this.mProgressBar.setVisibility(View.VISIBLE);
		this.mWebViewShowPage = (WebView) findViewById(R.id.webview);
		this.mWebViewShowPage.getSettings().setJavaScriptEnabled(true);
		this.mWebViewShowPage.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mProgressBar.setVisibility(View.GONE);
			}
		});
		this.mWebViewShowPage.loadUrl(mUrl);
		this.setUpLayoutAdmob();
	}
	
	private void setUpLayoutAdmob() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_ad);
		if (ApplicationUtils.isOnline(this)) {
			boolean b=SHOW_ADVERTISEMENT;
			if(b){
				adView = new AdView(this);
				adView.setAdUnitId(ADMOB_ID_BANNER);
				adView.setAdSize(AdSize.BANNER);
				
				layout.addView(adView);
				AdRequest mAdRequest = new AdRequest.Builder().build();
				adView.loadAd(mAdRequest);
				
			}
			else{
				layout.setVisibility(View.GONE);
			}
		}
		else {
			layout.setVisibility(View.GONE);
		}
	}
	

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mWebViewShowPage != null) {
			mWebViewShowPage.destroy();
		}
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
	
	private void backToHome(){
		if(SettingManager.getOnline(this)){
			finish();
		}
		else{
			Intent mIntent = new Intent(this, SplashActivity.class);
			startActivity(mIntent);
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mWebViewShowPage.canGoBack()) {
				mWebViewShowPage.goBack();
			}
			else {
				backToHome();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
