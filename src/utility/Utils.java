package utility;

import com.google.android.gms.maps.model.LatLng;

import android.util.Log;

public class Utils {
	
	public static String bloc = "yhsPYjqyT1";
	public static String sloc = "ruvfqOS1Pt";
	
	/**
	 * Default map center. Use when data is not available.
	 */
	public static final LatLng center = new LatLng(22.745152, 79.844197);
	/**
	 * Default map zoom level.
	 */
	public static final int zoom = 4;
	/**
	 * Default map bearing value.
	 */
	public static final int bearing = 0; // Orientation to north
	
	/**
	 * Prints debugging log messages
	 * 
	 * @param classname String
	 * @param msg String
	 */
	public static void logv(String classname, String msg){
		Log.v("sb",classname+" : "+msg);
	}


	/**
	 * Prints debugging log messages along with exception info
	 * 
	 * @param classname
	 * @param msg
	 * @param ex (not null)
	 */
	public static void logv(String classname, String msg, Exception ex){
		Log.v("sb", classname+": "+msg, ex);
	}

	public static class TimeProfile {
		long startTime, stopTime;
		public TimeProfile() {
			startTime = System.currentTimeMillis();
		}

		public void print(String classname){
			stopTime = System.currentTimeMillis();
			Utils.logv(classname,(stopTime - startTime)+"ms");
		}
	}
}
