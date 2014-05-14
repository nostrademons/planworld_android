package name.tang.jonathan.planworld;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.util.Log;

/**
 * Data helper to capture information scraped off the planwatch.
 * 
 * This has a constructor for parsing the raw string received from JS, but then
 * is just public final fields for access.
 * 
 * @author jdtang
 */
public class PlanwatchData {

	private static final DateTimeZone TIME_ZONE =
			DateTimeZone.forID("America/New_York");
	private static final DateTimeFormatter TIME_FORMAT =
			DateTimeFormat.forPattern(" (h:mma)").withZone(TIME_ZONE);
	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormat.forPattern(" (M/d/YY)").withZone(TIME_ZONE);
	
	public final String username;
	public final boolean hasUpdate;
	public final long updateTimestamp;
	public final boolean hasExactTime;
	
	public PlanwatchData(String jsData) {
		String[] pieces = jsData.split(";");
		if (pieces.length != 3) {
			Log.e("PlanwatchData", "Missing fields when parsing " + jsData);
			this.username = jsData;
			this.hasUpdate = false;
			this.updateTimestamp = 0;
			this.hasExactTime = false;
			return;
		}
		this.username = pieces[0];
		this.hasUpdate = "1".equals(pieces[1]);
		
		if (" (Never)".equals(pieces[2])) {
			this.updateTimestamp = 0;
			this.hasExactTime = false;
			return;
		}
		
		DateTime lastUpdate = DateTime.now();
		boolean hasExactTime = false;
		try {
			lastUpdate = TIME_FORMAT.parseDateTime(pieces[2]);
			hasExactTime = true;
		} catch (IllegalArgumentException e) {
			lastUpdate = DATE_FORMAT.parseDateTime(pieces[2]);
			// Let a failure here fall through so I know about it as a developer,
			// although eventually we may just want to log it and use the current
			// time as a fallback.
			hasExactTime = false;
		}
		this.updateTimestamp = lastUpdate.getMillis() / 1000;
		this.hasExactTime = hasExactTime;
	}
}
