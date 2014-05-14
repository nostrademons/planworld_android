package name.tang.jonathan.planworld;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class PlanScraperFragment extends WebViewFragment {

	private static final String PLANWATCH_EXTRACTION_JS = "javascript:"
			+ "var records = [];" 
			+ "var links = document.querySelectorAll('a.planwatch');"
			+ "for (var i = 0, link; link = links[i++];) {"
				+ "var name = link.innerText;"
				+ "var hasUpdate = link.previousElementSibling.classList.contains('new') ? '1' : '0';"
				+ "var updateTime = link.nextSibling.textContent;"
				+ "records.push([name, hasUpdate, updateTime].join(';'));"
			+ "}"
			+ "plandroid.recordPlanWatch(records.join(','));";
	
	private ScrapeCompletedListener listener;
	private PlanwatchData[] planwatch;
	
	public PlanScraperFragment() {
		planwatch = new PlanwatchData[0];
		listener = new ScrapeCompletedListener() {
			@Override public void onScrapeCompleted() {
				// Null object
			}
		};
	}

	public void setListener(ScrapeCompletedListener listener) {
		this.listener = listener;
	}
	
	public PlanwatchData[] getPlanwatch() {
		return planwatch;
	}
	
	public String[] getUsernames() {
		String[] usernames = new String[planwatch.length];
		for (int i = 0; i < usernames.length; ++i) {
			usernames[i] = getPlanwatch()[i].username;
		}
		return usernames; 
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		WebView browser = (WebView) super.onCreateView(
				inflater, container, savedInstanceState);
		browser.setVisibility(View.GONE);
		browser.setWebViewClient(new PlanScraperClient());
		browser.getSettings().setJavaScriptEnabled(true);
		browser.addJavascriptInterface(new JSPlanDroid(), "plandroid");
		browser.loadUrl("https://neon.note.amherst.edu/planworld");
		return browser;
	}

	public static interface ScrapeCompletedListener {
		public void onScrapeCompleted();
	}
	
	private class PlanScraperClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (url.indexOf("shibidp.amherst.edu") != -1) {
				getWebView().setVisibility(View.VISIBLE);
				return;
			}

			if (url.indexOf("planworld") != -1) {
				getWebView().setVisibility(View.GONE);
				view.loadUrl(PLANWATCH_EXTRACTION_JS);
			}
		}
	}
	
	private class JSPlanDroid implements Runnable {
		@JavascriptInterface
		public void recordPlanWatch(String jsData) {
			String[] records = jsData.split(",");
			PlanScraperFragment.this.planwatch = new PlanwatchData[records.length];
			for (int i = 0; i < records.length; ++i) {
				PlanScraperFragment.this.planwatch[i] = new PlanwatchData(records[i]);
			}
			getWebView().post(this);
		}
		
		// Executes in UI thread, called back via planWatch.post();
		public void run() {
			listener.onScrapeCompleted();
		}
	}
}
