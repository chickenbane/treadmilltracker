package com.googlejobapp.treadmilltracker;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.CursorWrapper;

public class RunDataCursor extends CursorWrapper {

	private final List<RunData> mList = new ArrayList<RunData>();

	public RunDataCursor(final Cursor cursor) {
		super(cursor);
	}

	public void fillCache() {
		final Cursor c = getWrappedCursor();
		while (c.moveToNext()) {
			mList.add(RunDao.createRunData(c));
		}
	}

	public RunData getRunData(final int pos) {
		return mList.get(pos);
	}

}
