package com.googlejobapp.treadmilltracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.CursorWrapper;

public class RunDataCursor extends CursorWrapper {

	private final List<RunData> mList = new ArrayList<RunData>();
	private final Map<String, List<RunData>> mMap = new HashMap<String, List<RunData>>();

	public RunDataCursor(final Cursor cursor) {
		super(cursor);
	}

	/**
	 * This function is designed to be called on a background thread.
	 */
	public void fillCache() {
		final Cursor c = getWrappedCursor();
		while (c.moveToNext()) {
			final RunData runData = RunDao.createRunData(c);
			mList.add(runData);
			final String week = runData.getWeek();
			List<RunData> weekList = mMap.get(week);
			if (weekList == null) {
				weekList = new ArrayList<RunData>();
				mMap.put(week, weekList);
			}
			weekList.add(runData);
		}
	}

	public RunData getRunData(final int pos) {
		return mList.get(pos);
	}

	public RunData getRunData() {
		return mList.get(getPosition());
	}

}
