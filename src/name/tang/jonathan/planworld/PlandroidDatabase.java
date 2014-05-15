package name.tang.jonathan.planworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class PlandroidDatabase extends SQLiteOpenHelper {

	public PlandroidDatabase(Context context) {
		super(context, "plandroid", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE planwatch (" +
				"username varchar(255) PRIMARY KEY," +
				"last_plan text," +
				"last_update integer," +
				"last_check integer," +
				"has_update integer" +
			");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing yet
	}
	
	public class UpdatePlanwatch extends AsyncTask<PlanwatchData, Integer, Void> {
		protected Void doInBackground(PlanwatchData... data) {
			Log.d("DB", "Updating planwatch.");
			SQLiteDatabase db = getWritableDatabase();
			for (PlanwatchData entry : data) {
				ContentValues row = new ContentValues();
				row.put("username", entry.username);
				row.put("has_update", entry.hasUpdate);
				row.put("last_update", entry.lastUpdate);
				long newRowId = db.insertWithOnConflict(
						"planwatch", null, row, SQLiteDatabase.CONFLICT_REPLACE);
			}
			Log.d("DB", "Wrote " + data.length + " records");
			return null;
		}
	}
}
