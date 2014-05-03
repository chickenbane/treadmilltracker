package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

public class RunDataCursor extends CursorWrapper {
	private final static String TAG = "RunDataCursor";

	private List<RunData> mList;
	private Map<String, List<RunData>> mWeekMap;
	private RunData mAggregrateAll;
	private Map<String, RunData> mAggregateWeeks;
	private String mPenultimateWeek;
	private String mUltimateWeek;

	public RunDataCursor(final Cursor cursor) {
		super(cursor);
	}

	public void fillCacheInBackground() {
		mList = new ArrayList<RunData>();
		mWeekMap = new HashMap<String, List<RunData>>();
		mAggregateWeeks = new HashMap<String, RunData>();
		final Cursor c = getWrappedCursor();
		int ultimate = 0;
		while (c.moveToNext()) {
			final RunData runData = RunDao.createRunData(c);
			mList.add(runData);
			final String week = runData.getWeek();
			List<RunData> weekList = mWeekMap.get(week);
			if (weekList == null) {
				weekList = new ArrayList<RunData>();
				mWeekMap.put(week, weekList);
			}
			weekList.add(runData);

			final int weekNum = Integer.parseInt(week);
			if (weekNum > ultimate) {
				ultimate = weekNum;
			}
		}

		int penultimate = 0;
		for (final String weekKey : mWeekMap.keySet()) {
			final int weekNum = Integer.parseInt(weekKey);
			if (weekNum > penultimate && weekNum < ultimate) {
				penultimate = weekNum;
			}
			final List<RunData> list = mWeekMap.get(weekKey);
			final RunData createRunAggregate = createRunAggregate(list);
			mAggregateWeeks.put(weekKey, createRunAggregate);
		}

		mAggregrateAll = createRunAggregate(mList);
		mPenultimateWeek = String.valueOf(penultimate);
		mUltimateWeek = String.valueOf(ultimate);

		Log.e(TAG, "last=" + mUltimateWeek + " penult=" + mPenultimateWeek
				+ " keyset=" + mWeekMap.keySet());
	}

	public RunData getRunData(final int pos) {
		return mList.get(pos);
	}

	public RunData getRunData() {
		return mList.get(getPosition());
	}

	public RunData getAggregrateAll() {
		return mAggregrateAll;
	}

	public RunData getAggregateUltimateWeek() {
		return mAggregateWeeks.get(mUltimateWeek);
	}

	public RunData getAggregatePenultimateWeek() {
		return mAggregateWeeks.get(mPenultimateWeek);
	}

	public static RunData createRunAggregate(final List<RunData> list) {
		BigDecimal miles = BigDecimal.ZERO;
		int minutes = 0;
		for (final RunData run : list) {
			miles = miles.add(run.getMiles());
			minutes += run.getMinutes();
		}
		return new RunData(0, minutes, miles.toString());
	}
}
