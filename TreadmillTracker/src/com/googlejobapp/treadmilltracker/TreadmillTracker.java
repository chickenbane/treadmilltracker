package com.googlejobapp.treadmilltracker;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The Contract class for this project.
 * 
 */
public class TreadmillTracker {

	private static final String SCHEME = "content://";
	private static final String AUTHORITY = "com.googlejobapp.treadmilltracker.provider";
	private static final String PATH_RUNS = "/treadmill";
	private static final String uriString = SCHEME + AUTHORITY + PATH_RUNS;

	public static final Uri RUN_DIR_URI = Uri.parse(uriString);
	public static final String[] RUN_DIR_PROJECTION = null;

	public static final class Run implements BaseColumns {
		public static final String TABLE_NAME = "treadmill";
		public static final String COLUMN_NAME_DURATION_MINS = "duration";
		public static final String COLUMN_NAME_START_TIME = "start";
		public static final String COLUMN_NAME_DISTANCE_MILES = "distance";
	}

}
