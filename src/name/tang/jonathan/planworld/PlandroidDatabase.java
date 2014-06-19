package name.tang.jonathan.planworld;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class PlandroidDatabase extends SQLiteOpenHelper {

	public PlandroidDatabase(Context context) {
		super(context, "plandroid", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE users (" +
				"username varchar(255) PRIMARY KEY," +
				"last_plan text," +
				"last_update integer," +
				"last_check integer," +
				"has_update integer," +
				"is_read integer," +
				"is_planwatch integer," +
				"is_snoop integer" +
			");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing yet
	}
	
	public class UpdatePlanwatch extends AsyncTask<Void, Integer, Void> {
		private Map<String, ContentValues> rows = new HashMap<>();
		
		public UpdatePlanwatch(PlanwatchData[] planwatch, PlanwatchData[] snoop) {
			for (PlanwatchData entry : planwatch) {
				updateRow(entry, true, false);
			}
			for (PlanwatchData entry : snoop) {
				updateRow(entry, false, true);
			}
		}
		
		private void updateRow(PlanwatchData entry, boolean updatePlanwatch, boolean updateSnoop) {
			ContentValues row = rows.get(entry.username);
			if (null == row) {
				row = new ContentValues();
				row.put("username", entry.username);
				row.put("has_update", entry.hasUpdate);
			}
			if (updatePlanwatch) {
				row.put("is_planwatch", true);
			}
			if (updateSnoop) {
				row.put("is_snoop", true);
			}
			rows.put(entry.username, row);
		}
		
		protected Void doInBackground(Void... unused) {
			Log.d("DB", "Updating planwatch.");
			SQLiteDatabase db = getWritableDatabase();
			for (ContentValues row : rows.values()) {
				db.insertWithOnConflict(
						"users", null, row, SQLiteDatabase.CONFLICT_REPLACE);
			}
			db.close();
			Log.d("DB", "Wrote " + rows.size() + " records");
			return null;
		}
	}
	
	public class UpdatePlan extends AsyncTask<EntryData, Void, Void> {
		protected Void doInBackground(EntryData... plans) {
			EntryData plan = plans[0];
			Log.d("DB", "Updating plan for user " + plan.username);
			SQLiteDatabase db = getWritableDatabase();
			ContentValues row = new ContentValues();
			row.put("last_plan", plan.content);
			row.put("last_update", plan.getLastUpdateTime());
			row.put("last_check", System.currentTimeMillis());
			row.put("has_update", false);
			row.put("is_read", false);
			db.update("users", row, "username = ?", new String[] { plan.username });
			return null;
		}
	}
	
	public class GetPlanwatchSize extends AsyncTask<Void, Void, Integer> {
		protected Integer doInBackground(Void... unused) {
			Log.d("DB", "Querying number of planwatch rows");
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", new String[0]);
			cursor.moveToFirst();
			int count = cursor.getInt(0);
			cursor.close();
			db.close();
			return count;
		}
	}
}
