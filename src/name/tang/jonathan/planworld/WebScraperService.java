package name.tang.jonathan.planworld;

import java.util.concurrent.ExecutionException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * An {@link IntentService} subclass for scraping planwatch and plan contents
 * from a hidden WebView.
 */
public class WebScraperService extends IntentService {
	
	public static final String ACTION_REFRESH = "name.tang.jonathan.planworld.WebScraperService.ACTION_REFRESH";
	
	public WebScraperService() {
		super("WebScraperService");
	}

	private WebView browser;
	private Command currentCommand;
	private PlandroidDatabase dbHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		
		dbHelper = new PlandroidDatabase(this);
		
        browser = createHiddenWebview();
		browser.setWebViewClient(new WebScraperClient());
		browser.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("WebScraperService", "Handling intent " + intent.getAction());
		if (planwatchIsEmpty()) {
			currentCommand = new FetchFullPlanwatch();
			currentCommand.run();
		}
	}
	
	private boolean planwatchIsEmpty() {
		try {
			return dbHelper.new GetPlanwatchSize().get() == 0;
		} catch (InterruptedException | ExecutionException e) {
			Log.e("WebScraperService.planwatchIsEmpty", e.getMessage());
			return false;
		}
	}

	private WebView createHiddenWebview() {
		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutParams params = new WindowManager.LayoutParams(
        		WindowManager.LayoutParams.WRAP_CONTENT,
        		WindowManager.LayoutParams.WRAP_CONTENT,
        		WindowManager.LayoutParams.TYPE_PHONE,
        		WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        		PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        params.width = 0;
        params.height = 0;

        LinearLayout view = new LinearLayout(this);
        view.setLayoutParams(new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT,
        		RelativeLayout.LayoutParams.MATCH_PARENT));

        WebView webview = new WebView(this);
        webview.setLayoutParams(new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.MATCH_PARENT,
        		LinearLayout.LayoutParams.MATCH_PARENT));
        view.addView(webview);

        windowManager.addView(view, params);
        return webview;
	}
	
	private class WebScraperClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (url.indexOf("shibidp.amherst.edu") != -1) {
				// Show webview
				return;
			}
		}
	}

	private abstract class Command implements Runnable {
		public abstract void run();
	}
	
	private class FetchFullPlanwatch extends Command {
		public void run() {
			Log.d("WebScraperService", "Fetching full planwatch.");
		}
	}
}
