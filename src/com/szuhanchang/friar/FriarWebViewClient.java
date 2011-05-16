package com.szuhanchang.friar;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FriarWebViewClient extends WebViewClient {
	String currentUrl;
	
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		currentUrl = url;
	}
	
	public String getCurrentUrl() {
		return currentUrl;
	}
}
