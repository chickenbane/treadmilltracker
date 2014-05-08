package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.CursorWrapper;

public class RunDataCursor extends CursorWrapper {
	// private final static String TAG = "RunDataCursor";

	// All the RunData objects, in the order returned by the cursor.
	private List<RunData> mList;

	// All the RunData objects, grouped by the weekNumber
	private Map<String, List<RunData>> mWeekMap;

	// The RunAggregates, key is the weekNumber, calculated from the
	// List<RunData> for that week
	private Map<String, RunAggregate> mAggregateWeeks;

	// The list of week keys, sorted. The latest weeks will be last in the list
	private List<String> mSortedWeekList;

	public RunDataCursor(final Cursor cursor) {
		super(cursor);
	}

	public void fillCacheInBackground() {
		mList = new ArrayList<RunData>();
		mWeekMap = new HashMap<String, List<RunData>>();

		final ArrayList<Integer> weekNums = new ArrayList<Integer>();
		final Cursor c = getWrappedCursor();
		while (c.moveToNext()) {
			final RunData runData = RunDao.createRunData(c);
			mList.add(runData);
			final String week = runData.getWeek();
			List<RunData> weekList = mWeekMap.get(week);
			if (weekList == null) {
				weekList = new ArrayList<RunData>();
				mWeekMap.put(week, weekList);
				final int weekNum = Integer.parseInt(week);
				weekNums.add(weekNum);
			}
			weekList.add(runData);
		}

		Collections.sort(weekNums);
		mSortedWeekList = new ArrayList<String>(weekNums.size());
		mAggregateWeeks = new HashMap<String, RunAggregate>(weekNums.size());

		for (final Integer weekNum : weekNums) {
			final String weekKey = weekNum.toString();
			final RunAggregate aggregate = createRunAggregate(mWeekMap
					.get(weekKey));
			mAggregateWeeks.put(weekKey, aggregate);
			mSortedWeekList.add(weekKey);
		}
	}

	public RunData getRunData(final int pos) {
		return mList.get(pos);
	}

	public RunData getRunData() {
		return mList.get(getPosition());
	}

	public RunAggregate getAggregateWeek(final String week) {
		return mAggregateWeeks.get(week);
	}

	public List<String> getSortedWeekList() {
		return mSortedWeekList;
	}

	private static RunAggregate createRunAggregate(final List<RunData> list) {
		int minutes = 0;
		BigDecimal miles = BigDecimal.ZERO;
		for (final RunData run : list) {
			minutes += run.getMinutes();
			miles = miles.add(run.getMiles());
		}
		return new RunAggregate(minutes, miles, list.size());
	}
}
