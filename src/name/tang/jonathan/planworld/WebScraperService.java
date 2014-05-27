package name.tang.jonathan.planworld;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * An {@link IntentService} subclass for scraping planwatch and plan contents
 * from a hidden WebView.
 */
public class WebScraperService extends Service {
	
	public static final String ACTION_REFRESH = "name.tang.jonathan.planworld.WebScraperService.ACTION_REFRESH";

	private WebView browser;
	private Queue<WebFetch> pendingFetches;
	private PlandroidDatabase dbHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("WebScraperService", "Starting WebScraperService");
		
		pendingFetches = new ArrayDeque<>();
		dbHelper = new PlandroidDatabase(this);
		
        browser = createHiddenWebview();
		browser.setWebViewClient(new WebScraperClient());
		browser.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("WebScraperService", "Handling intent " + intent.getAction());
		if (browser.getVisibility() == View.VISIBLE) {
			Log.w("WebScraperService", "Login window is visible; ignoring intent");
			return START_STICKY;
		}
		new UpdatePlanwatch().execute();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(browser);
	}

	private WebView createHiddenWebview() {
		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutParams params = new WindowManager.LayoutParams(
        		WindowManager.LayoutParams.MATCH_PARENT,
        		WindowManager.LayoutParams.MATCH_PARENT,
        		WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
        		WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        		PixelFormat.OPAQUE);
        params.gravity = Gravity.FILL_VERTICAL | Gravity.FILL_HORIZONTAL;

        WebView webview = new WebView(this);
        webview.setLayoutParams(params);
        webview.setVisibility(View.GONE);

        windowManager.addView(webview, params);
        return webview;
	}
	
	private void showWebView() {
		Log.d("WebScraperService", "Showing login screen");
		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		WindowManager.LayoutParams params = (WindowManager.LayoutParams) browser.getLayoutParams();
		params.flags = 0;
		browser.setVisibility(View.VISIBLE);
		browser.requestFocus(View.FOCUS_DOWN);
		windowManager.updateViewLayout(browser, params);
	}
	
	private void hideWebView() {
		Log.d("WebScraperService", "Hiding login screen");
		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		WindowManager.LayoutParams params = (WindowManager.LayoutParams) browser.getLayoutParams();
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		browser.setVisibility(View.GONE);
		windowManager.updateViewLayout(browser, params);
	}
	
	private class WebScraperClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			WebFetch currentFetch = pendingFetches.peek();
			Log.d("WebScraperService", "Page load finished for " + currentFetch.url + "; current URL = " + url);
			if (browser.getUrl().indexOf("shibidp.amherst.edu") != -1) {
				Log.d("WebScraperService", "Should show login page.");
				showWebView();
				return;
			}
			hideWebView();
			
			// If a login happened, the page may redirect to the PlanWorld home screen
			// rather than the desired user's plan; send it back.
			if (!browser.getUrl().equals(currentFetch.url)) {
				browser.loadUrl(currentFetch.url);
			}
			currentFetch.onPageFinished();
		}
	}

	private interface Command {
		void execute();
	}
	
	private abstract class WebFetch implements Command, Runnable {
		private String url;
		private String js;
		
		protected WebFetch(String url, String js) {
			this.url = url;
			this.js = js;
		}
		
		public void execute() {
			Log.d("WebScraperService", "Executing web fetch");
			pendingFetches.add(this);
			browser.post(this);
		}
		
		public void run() {
			Log.d("WebScraperService", "Loading url " + url);
			browser.addJavascriptInterface(this, "plandroid");
			browser.loadUrl(url);
		}
		
		public void onPageFinished() {
			browser.loadUrl("javascript:" + js);
		}
		
		@JavascriptInterface
		public void recordData(String data) {
			Log.d("WebScraperService", "Received browser callback");
			handleData(data);
			browser.post(new Runnable() {
				public void run() {
					handleUIPostProcessing();
				}
			});
		}
		
		protected void cleanup() {
			pendingFetches.remove();
			
			// If we've enqueued any further fetches in handleData, start the next one.
			WebFetch nextFetch = pendingFetches.peek();
			if (nextFetch != null) {
				browser.post(nextFetch);
			}
		}

		protected void handleData(String data) {}

		protected void handleUIPostProcessing() {}
	}
	
	private class UpdatePlanwatch extends WebFetch {
		public UpdatePlanwatch() {
			super("https://neon.note.amherst.edu/planworld/",
				  "var planwatch = [], snoop = [];" 
				+ "var links = document.querySelectorAll('a.planwatch');"
				+ "for (var i = 0, link; link = links[i++];) {"
					+ "var records = (link.previousElementSibling.previousElementSibling.innerText == 'Snoop' || snoop.length) ? snoop : planwatch;"
					+ "records.push({"
						+ "username: link.innerText,"
						+ "hasUpdate: link.previousElementSibling.classList.contains('new'),"
						+ "updateTime: link.nextSibling.textContent"
					+ "});"
				+ "}"
				+ "plandroid.recordData(JSON.stringify({ "
					+ "planwatch: planwatch,"
					+ "snoop: snoop"
				+ "}));");
		}
		
		private class WebData {
			public PlanwatchData[] planwatch;
			public PlanwatchData[] snoop;
		}
		
		@Override
		protected void handleData(String json) {
			Gson gson = new Gson();
			WebData webdata = gson.fromJson(json, WebData.class);
			Log.d("WebScraperService", "Scraped " + json + " from planWatch");
			dbHelper.new UpdatePlanwatch(webdata.planwatch, webdata.snoop).execute();
		}
	}
	
	private class UpdatePlan extends WebFetch {
		public UpdatePlan(String username) {
			super("https://neon.note.amherst.edu/planworld/?id=" + username,
				  "var header = document.querySelector('td.content tt:nth-of-type(2)');"
				+ "var content = header.parentNode.cloneNode(true);"
				+ "var node;"
				+ "while ((node = content.firstChild) && (node.nodeType != 1 || node.innerText.indexOf('Login name:') != 0)) content.removeChild(node);"
				+ "content.removeChild(node);"
				+ "plandroid.recordData(JSON.stringify({"
					+ "username: document.querySelector('td.content strong').innerText, "
					+ "content: content.innerHTML,"
					+ "updated: header.innerText.substring("
						+ "header.innerText.indexOf('Last updated: ') +"
						+ "'Last updated: '.length, "
						+ "header.innerText.indexOf(' (archives)'))"
				+ "}));");
		}
		
		@Override
		protected void handleData(String json) {
			Gson gson = new Gson();
			EntryData webdata = gson.fromJson(json, EntryData.class);
			Log.d("WebScraperService", "Scraped " + webdata.username + " (" + webdata.updated + "): " + webdata.content);
			dbHelper.new UpdatePlan().execute(webdata);
		}
	}

	@ Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
