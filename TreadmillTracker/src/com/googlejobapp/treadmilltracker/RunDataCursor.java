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
	private Map<String, RunAggregate> mAggregateWeeks;
	private String mPenultimateWeek;
	private String mUltimateWeek;

	public RunDataCursor(final Cursor cursor) {
		super(cursor);
	}

	public void fillCacheInBackground() {
		mList = new ArrayList<RunData>();
		mWeekMap = new HashMap<String, List<RunData>>();
		mAggregateWeeks = new HashMap<String, RunAggregate>();
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
			final RunAggregate aggregate = createRunAggregate(mWeekMap
					.get(weekKey));
			mAggregateWeeks.put(weekKey, aggregate);
		}

		mPenultimateWeek = String.valueOf(penultimate);
		mUltimateWeek = String.valueOf(ultimate);
	}

	public RunData getRunData(final int pos) {
		return mList.get(pos);
	}

	public RunData getRunData() {
		return mList.get(getPosition());
	}

	public List<RunData> getRunWeek(final String week) {
		return mWeekMap.get(week);
	}

	public RunAggregate getAggregateWeek(final String week) {
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

	static RunAggregate createRunAggregate(final List<RunData> list) {
		int minutes = 0;
		BigDecimal miles = BigDecimal.ZERO;
		for (final RunData run : list) {
			minutes += run.getMinutes();
			miles = miles.add(run.getMiles());
		}
		return new RunAggregate(minutes, miles, BigDecimal.valueOf(list.size()));
	}
}
