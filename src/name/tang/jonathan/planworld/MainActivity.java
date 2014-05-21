package name.tang.jonathan.planworld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity
		implements PlanScraperFragment.ScrapeCompletedListener {

	private ListView planWatch;
	private PlanScraperFragment scraper;
	private PlandroidDatabase dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		planWatch = (ListView) findViewById(R.id.planwatch);
		planWatch.setAdapter(new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, new String[] {
						"person1", "person2", "person3", "person4", "person5"
				}));
		
		scraper = (PlanScraperFragment) getFragmentManager().findFragmentById(R.id.scraper);
		scraper.setListener(this);

		dbHelper = new PlandroidDatabase(this);
		
		Intent refreshData = new Intent(this, WebScraperService.class);
		refreshData.setAction(WebScraperService.ACTION_REFRESH);
		AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 15 * 1000,
				PendingIntent.getService(this, 0, refreshData, 0));
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
		if (id == R.id.action_login) {
			scraper.getWebView().setVisibility(View.VISIBLE);;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onScrapeCompleted() {
		dbHelper.new UpdatePlanwatch().execute(scraper.getPlanwatch());
		planWatch.setAdapter(new ArrayAdapter(
				MainActivity.this, android.R.layout.simple_list_item_1,
				scraper.getUsernames()));
	}
}
