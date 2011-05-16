package com.szuhanchang.friar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
        setContentView(R.layout.main);
        
        gestureDetector = new GestureDetector(new FriarGestureListener());
        
        htmlFiles = loadBook();
        totalPages = htmlFiles.size();

        webView = (FriarWebView) findViewById(R.id.webview);
        webView.setWebViewClient(new FriarWebViewClient());
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        
        webView.loadUrl(String.format("file:///android_asset/book/%s", htmlFiles.get(currentPage)));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	System.out.println("onTouchEvent");
        super.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
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
        return htmlFiles;
    }
    
    private class FriarWebView extends WebView {
    	Context context;
    	GestureDetector gestureDetector;

    	public FriarWebView(Context context) {
    		super(context);
	    	this.context = context;
	    	gestureDetector = new GestureDetector(context, sogl);
    	}
    	
    	@Override
    	public boolean onTouchEvent(MotionEvent event) {
    		return gestureDetector.onTouchEvent(event);
    	}

    	GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
    		public boolean onDown(MotionEvent event) {
    			return true;
    		}
    		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
    			if (event1.getRawX() > event2.getRawX()) {
    				show_toast("swipe left");
    			} else {
    				show_toast("swipe right");
    			}
    			return true;
    		}
    	};
    	
    	void show_toast(final String text) {
    		Toast t = Toast.makeText(context, text, Toast.LENGTH_SHORT);
    		t.show();
		}
    }
    
    private class FriarWebViewClient extends WebViewClient {
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		try {
    			URI uri = new URI(url);
				String[] segments = uri.getPath().split("/");
				String filename = segments[segments.length - 1];
				if (filename.contains("-")) {
					currentPage = Integer.parseInt(filename.substring(0, filename.indexOf("-")));
				}
				else {
					currentPage = Integer.parseInt(filename.substring(0, filename.indexOf(".")));
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
    		System.out.println(currentPage + " " + url);
    	}
    }
    
    private class FriarGestureListener extends GestureDetector.SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 120;
	    private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		@Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			System.out.println("onFling");
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
	                return false;
	            }
	            
	            // right to left swipe
	            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	if (currentPage + 1 < totalPages) {
	            		webView.loadUrl(String.format("file:///android_asset/book/%s", htmlFiles.get(++currentPage)));
	            	}
	            	System.out.println("Right to left swipe.");
	            	return true;
	            }  
	            // left to right swipe
	            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	if (currentPage - 1 >= 0) {
	            		webView.loadUrl(String.format("file:///android_asset/book/%s", htmlFiles.get(--currentPage)));
	            	}
	            	System.out.println("Left to right swipe.");
	            	return true;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return false;
	    }
	}
}