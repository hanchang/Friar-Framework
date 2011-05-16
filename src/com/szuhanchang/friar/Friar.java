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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Friar extends Activity {
	final String BASE_URL = "file:///android_asset/book/";
	final String MIME_TYPE = "text/html";
	final String ENCODING = "utf-8";
	
	FriarWebView webView;
	List<String> htmlFiles;
	int currentPage = 0;
	int totalPages = 0;
	
	GestureDetector gestureDetector;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("onCreate!");
		super.onCreate(savedInstanceState);

		htmlFiles = loadBook();
		totalPages = htmlFiles.size();

		webView = new FriarWebView(this);
		setContentView(webView);

		webView.setWebViewClient(new FriarWebViewClient());
		final WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(false);

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
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
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
	
	public WebView getWebView() {
		return webView;
	}

	class FriarWebView extends WebView {
		Context context;
		GestureDetector gd;

		public FriarWebView(Context context) {
			super(context);
			this.context = context;
			FriarGestureDetector fgd = new FriarGestureDetector();
			gd = new GestureDetector(context, fgd);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return gd.onTouchEvent(event) || super.onTouchEvent(event);
		}
		
		class FriarGestureDetector extends GestureDetector.SimpleOnGestureListener {
			private int swipe_Min_Distance = 100;
			private int swipe_Max_Distance = 350;
			private int swipe_Min_Velocity = 100;

			private void show_toast(final String text) {
				Toast t = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				t.show();
				System.out.println(text);
			}
			
			private void showUrl(int pageNum) {
				assert pageNum >= 0 && pageNum < totalPages;
				
				String filename = htmlFiles.get(pageNum);
				String url = BASE_URL + filename;
				getWebView().loadUrl(url);
				getWebView().reload();
				show_toast(url + ", " + currentPage);
			}

			@Override
			public boolean onDown(MotionEvent event) {
				return false;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				final float xDistance = Math.abs(e1.getX() - e2.getX());
				final float yDistance = Math.abs(e1.getY() - e2.getY());

				if (xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance) {
					return false;
				}

				velocityX = Math.abs(velocityX);
				velocityY = Math.abs(velocityY);

				if (velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance) {
					if (e1.getX() > e2.getX()) { // right to left
						show_toast("Swipe Left");
						if (currentPage + 1 > totalPages) {
							show_toast("This is the last page of the book.");
						}
						else {
							showUrl(++currentPage);
						}
					}
					else {
						show_toast("Swipe Right");
						if (currentPage - 1 < 0) {
							show_toast("This is the first page of the book.");
						}
						else {
							showUrl(--currentPage);
						}
					}
					setContentView(webView);
					return true;
				}

				return false;
			}
		}
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