package com.mintech.blur;

import java.nio.channels.OverlappingFileLockException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;

public class MainActivity extends Activity {
	
	public static final String TAG = "MainActivity";
	private Context mContext;
	private MainWebView mWebView;
	private ControllerView mController;
	private ScrollView mScrollView;
	private BlurView mBlurView;
	private Bitmap mBitmap;
	private BlurEngine mBlurEngine;
	
	private String html = "<!DOCTYPE html><html>" +
			"<head>" +
				"<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0\">" +
				"<script src=\"http://mintshop.com/lazy_loader.js\"></script>" +
				"<style>img { display: block; } </style>" +
			"</head>" +
			"<body>" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47372/mintshop_001.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47372/mintshop_002.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47372/mintshop_003.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47372/mintshop_004.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47373/mintshop_001.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47373/mintshop_002.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47373/mintshop_003.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
				"<img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47373/mintshop_004.JPEG' width=\"100%\" style=\"min-height: 1000px;\"> <img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47373/mintshop_005.JPEG' width=\"100%\" style=\"min-height: 1000px;\"> <img lazy_src='https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_f46e842e-c688-460e-a70b-e6a4d30e9885/mintshopimage/production/CroppedImage/WebImage/47374/mintshop_001.JPEG' width=\"100%\" style=\"min-height: 1000px;\">" +
			"</body></html>";
	
	public Bitmap getBitmapForVisibleRegion(WebView webview) {
	    Bitmap returnedBitmap = null;
	    webview.setDrawingCacheEnabled(true);	    
//	    returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
	    returnedBitmap = Bitmap.createScaledBitmap(webview.getDrawingCache(true), webview.getWidth()/2, webview.getHeight()/4, true);
	    webview.setDrawingCacheEnabled(false);
	    return returnedBitmap;
	}
	
	private void captureWebView() {
		mBlurView.setAlpha(1.f);
		mBitmap = mBlurEngine.picture2Bitmap(mWebView.capturePicture());
//		mBitmap = getBitmapForVisibleRegion(mWebView);
		mBlurView.setImageBitmap(mBitmap);
		
		mBlurView.setScaleY(2.f);
		mBlurView.setPivotY(0.f);
		mBlurView.setBottom(mWebView.getHeight()); 
		mBlurView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(mWebView.getScrollX(), mWebView.getScrollY() + mScrollView.getTop());
			}
		});
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		setContentView(R.layout.activity_main);
		mContext = this;
		
		mBlurEngine = new BlurEngine(mContext);
		
		mWebView = (MainWebView)findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadData(html, "text/html", "UTF-8");
		mWebView.setWebChromeClient(new WebChromeClient());

		mWebView.setWebViewClient(new WebViewClient() {
			boolean loadingFinished = true;
			boolean redirect = false;
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
				if (!loadingFinished) {
					redirect = true;
				}

				loadingFinished = false;
				view.loadUrl(urlNewString);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap facIcon) {
				loadingFinished = false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if(!redirect){
					loadingFinished = true;
				}

				if(loadingFinished && !redirect){
					captureWebView();
				} else{
					redirect = false; 
				}
			}
		});
		
		mScrollView = (ScrollView) findViewById(R.id.scrollView);
		mWebView.setDelegateScrollView(mScrollView);
		mBlurView = (BlurView) findViewById(R.id.blurView);
		
		mController = (ControllerView) findViewById(R.id.controller);
		mController.setReferenceView(mWebView);
		mController.setOnTouchListener(new OnTouchListener() {
			private float oldY = 0.f;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float y = event.getRawY();
				if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					int top = mController.resizeHeightByDrag(oldY, y);
					mScrollView.setLayoutParams(mController.getLayoutParams());
					mScrollView.scrollTo(mWebView.getScrollX(), mWebView.getScrollY() + top);
//				} else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//				} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				}
				oldY = y;
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
