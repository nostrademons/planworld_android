package name.tang.jonathan.planworld;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
	public final long lastUpdate;
	public final boolean hasExactTime;
	
	private PlanwatchData(String username, boolean hasUpdate,
						  long lastUpdate, boolean hasExactTime) {
		this.username = username;
		this.hasUpdate = hasUpdate;
		this.lastUpdate = lastUpdate;
		this.hasExactTime = hasExactTime;
	}
	
	public static class JsonDeserializer implements com.google.gson.JsonDeserializer<PlanwatchData> {		
		@Override
		public PlanwatchData deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String username = obj.get("username").getAsString();
			boolean hasUpdate = obj.get("hasUpdate").getAsBoolean();
			String updateTime = obj.get("updateTime").getAsString();
			DateTime lastUpdate = DateTime.now();
			boolean hasExactTime = false;

			if (" (Never)".equals(updateTime)) {
				lastUpdate = new DateTime(0);
				hasExactTime = false;
			} else {	
				try {
					lastUpdate = TIME_FORMAT.parseDateTime(updateTime);
					hasExactTime = true;
				} catch (IllegalArgumentException e) {
					lastUpdate = DATE_FORMAT.parseDateTime(updateTime);
					// Let a failure here fall through so I know about it as a developer,
					// although eventually we may just want to log it and use the current
					// time as a fallback.
					hasExactTime = false;
				}
			}
			return new PlanwatchData(
					username, hasUpdate, lastUpdate.getMillis() / 1000, hasExactTime);
		}
	}
}
