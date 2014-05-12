package name.tang.jonathan.planworld;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {

	private static final String PLANWATCH_EXTRACTION_JS = "javascript:"
			+ "var names = [];" 
			+ "var links = document.querySelectorAll('a.planwatch');"
			+ "for (var i = 0, link; link = links[i++];) {"
				+ "names.push(link.innerText);"
			+ "}"
			+ "alert(names);";
	
	private ListView planWatch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WebView planWorldWeb = (WebView) findViewById(R.id.webview);
		planWorldWeb.setWebViewClient(new PlanScraperClient());
		planWorldWeb.getSettings().setJavaScriptEnabled(true);
		planWorldWeb.addJavascriptInterface(new JSPlanDroid(), "plandroid");
		planWorldWeb.loadUrl("https://neon.note.amherst.edu/planworld");
		planWorldWeb.loadUrl("javascript:plandroid.execute()");
		
		planWatch = (ListView) findViewById(R.id.planwatch);
		planWatch.setAdapter(new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, new String[] {
						"person1", "person2", "person3", "person4", "person5"
				}));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class PlanScraperClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
	
	private class JSPlanDroid implements Runnable {
		@JavascriptInterface
		public void execute() {
			planWatch.post(this);
		}
		
		// Executes in UI thread, called back via planWatch.post();
		public void run() {
			planWatch.setAdapter(new ArrayAdapter(
					MainActivity.this, android.R.layout.simple_list_item_1,
					new String[] { "JS Invoked!" }));
		}
	}
}
