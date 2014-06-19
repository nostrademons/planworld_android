package name.tang.jonathan.planworld;

import android.app.AlarmManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

	private static final int PLANWATCH_LOADER = 0;
	
	private ListView planWatch;
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

		dbHelper = new PlandroidDatabase(this);
		
		getLoaderManager().initLoader(PLANWATCH_LOADER, null, this);
		
		Intent refreshData = new Intent(this, WebScraperService.class);
		refreshData.setAction(WebScraperService.ACTION_REFRESH);
		AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 30 * 60 * 1000,
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				Uri.parse("content://name.tang.jonathan.planworld.provider/planwatch"),
				new String[] { "rowid _id", "username", "last_update", "is_read" },
				null, null, "last_update DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		planWatch.setAdapter(new SimpleCursorAdapter(
				this, android.R.layout.simple_list_item_1, cursor,
				new String[] { "username" }, new int[] { android.R.id.text1 }, 0));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) planWatch.getAdapter();
		adapter.changeCursor(null);
	}
}
