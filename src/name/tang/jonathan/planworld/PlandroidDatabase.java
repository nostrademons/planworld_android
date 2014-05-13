package name.tang.jonathan.planworld;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PlandroidDatabase extends SQLiteOpenHelper {

	public PlandroidDatabase(Context context) {
		super(context, "plandroid", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE planwatch (" +
				"username varchar(255)," +
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
}
