package name.tang.jonathan.planworld;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PlanContentProvider extends ContentProvider {

	private PlandroidDatabase dbHelper;
	
	@Override
	public boolean onCreate() {
		dbHelper = new PlandroidDatabase(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		boolean isRow = !"".equals(getRow(uri));
		return "vnd.android.cursor." + (isRow ? "item" : "dir") +
				"/vnd.name.tang.jonathan.planworld.plans";
	}
	
	private static String getRow(Uri uri) {
		return uri.getPathSegments().size() == 2 ? uri.getLastPathSegment() : "";
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String row = getRow(uri);
		if (!"".equals(row)) {
			selection = selection + (selection.isEmpty() ? "_ID = ?" : "AND _ID = ?");
			selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
			selectionArgs[selectionArgs.length - 1] = row;
		}
		return dbHelper.getReadableDatabase().query(
				"users", projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Intentional no-op for now.
		return uri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// Intentional no-op for now.
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Intentionally unimplemented.
		return 0;
	}
}
