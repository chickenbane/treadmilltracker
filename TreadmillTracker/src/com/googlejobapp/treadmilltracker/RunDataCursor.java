package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.database.Cursor;
import android.database.CursorWrapper;

public class RunDataCursor extends CursorWrapper {
	// private final static String TAG = "RunDataCursor";

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

	public List<RunData> getRunWeek(final String week) {
		return mWeekMap.get(week);
	}

	public RunData getAggregateWeek(final String week) {
		return mAggregateWeeks.get(week);
	}

	public Set<String> getWeeks() {
		return mWeekMap.keySet();
	}

	/**
	 * This won't return null, but there's no promise getRunWeek() or
	 * getAggregateWeek() won't return null for this value.
	 */
	public String getPenultimateWeek() {
		return mPenultimateWeek;
	}

	/**
	 * This won't return null, but there's no promise getRunWeek() or
	 * getAggregateWeek() won't return null for this value.
	 */
	public String getUltimateWeek() {
		return mUltimateWeek;
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
