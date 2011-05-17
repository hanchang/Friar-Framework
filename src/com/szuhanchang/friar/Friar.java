package com.szuhanchang.friar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Friar extends Activity {
	final String BASE_URL = "file:///android_asset/book/";
	final String MIME_TYPE = "text/html";
	final String ENCODING = "utf-8";
	
	WebView webView;
	List<String> htmlFiles;
	int currentPage = 0;
	int totalPages = 0;
	
	GestureDetector gestureDetector;
	SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		private final int SWIPE_MIN_DISTANCE = 100;
		private final int SWIPE_MAX_DISTANCE = 350;
		private final int SWIPE_MIN_VELOCITY = 100;
		
		@Override
		public boolean onDown(MotionEvent event) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			final float xDistance = Math.abs(e1.getX() - e2.getX());
			final float yDistance = Math.abs(e1.getY() - e2.getY());

			if (xDistance > this.SWIPE_MAX_DISTANCE || yDistance > this.SWIPE_MAX_DISTANCE) {
				return false;
			}

			velocityX = Math.abs(velocityX);
			velocityY = Math.abs(velocityY);

			if (velocityX > this.SWIPE_MIN_VELOCITY && xDistance > this.SWIPE_MIN_DISTANCE) {
				if (e1.getX() > e2.getX()) { // right to left
					// showToast("Swipe Left");
					if (currentPage + 1 > totalPages) {
						showToast("This is the last page of the book.");
					}
					else {
						showUrl(++currentPage);
						return true;
					}
				}
				else {
					// showToast("Swipe Right");
					if (currentPage - 1 < 0) {
						showToast("This is the first page of the book.");
					}
					else {
						showUrl(--currentPage);
						return true;
					}
				}
			}

			return false;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		System.out.println("onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		htmlFiles = loadBook();
		totalPages = htmlFiles.size();

		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new FriarWebViewClient());
		webView.getSettings().setJavaScriptEnabled(false);
		
		gestureDetector = new GestureDetector(gestureListener);
		webView.setOnTouchListener(
				new View.OnTouchListener() {
		            public boolean onTouch(View wv, MotionEvent event) {
		                gestureDetector.onTouchEvent(event);
		                return false;
		            }
		        }
		);

		webView.loadUrl(BASE_URL + htmlFiles.get(currentPage));
		System.out.println(BASE_URL + htmlFiles.get(currentPage));
	}
	
	// Handle Android physical back button.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private List<String> loadBook() {
		List<String> htmlFiles = new ArrayList<String>();
		try {
			String[] files = getAssets().list("book");
			for (String file : files) {
				if (file.endsWith(".html")) {
					htmlFiles.add(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(htmlFiles, new AlphanumComparator());
		return htmlFiles;
	}
	
	private void showUrl(int pageNum) {
		assert pageNum >= 0 && pageNum < totalPages;
		
		String filename = htmlFiles.get(pageNum);
		String url = BASE_URL + filename;
		webView.loadUrl(url);
		// showToast(url + ", " + currentPage);
	}

	private void showToast(final String text) {
		Toast t = Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT);
		t.show();
		System.out.println(text);
	}
	
	class FriarWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			try {
				URI uri = new URI(url);
				String[] segments = uri.getPath().split("/");
				String filename = segments[segments.length - 1];
				if (filename.contains("-")) {
					currentPage = Integer.parseInt(filename.substring(0, filename.indexOf("-")));
				} else {
					currentPage = Integer.parseInt(filename.substring(0, filename.indexOf(".")));
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			System.out.println(currentPage + " " + url);
		}
	}
}