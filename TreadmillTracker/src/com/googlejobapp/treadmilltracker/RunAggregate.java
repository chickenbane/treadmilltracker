package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RunAggregate {
	private final RunData mTotals;
	private final int mRuns;

	private final int mAvgMinutes;
	private final String mAvgMiles;

	public RunAggregate(final int minutes, final BigDecimal miles,
			final int runs) {
		mTotals = new RunData(0, minutes, miles);
		mRuns = runs;

		if (runs == 0) {
			mAvgMinutes = 0;
			mAvgMiles = "0";
		} else {
			mAvgMinutes = minutes / runs;
			final BigDecimal avgMiles = mTotals.getMiles().divide(
					BigDecimal.valueOf(runs), 1, RoundingMode.HALF_UP);
			mAvgMiles = String.format("%.1f", avgMiles);
		}
	}

	public int getTotalMinutes() {
		return mTotals.getMinutes();
	}

	public String getTotalMilesFormatted() {
		return mTotals.getMilesFormatted();
	}

	public String getPace() {
		return mTotals.getPace();
	}

	public String getMph() {
		return mTotals.getMph();
	}

	public int getRuns() {
		return mRuns;
	}

	public int getAvgMinutes() {
		return mAvgMinutes;
	}

	public String getAvgMilesFormatted() {
		return mAvgMiles;
	}

	public static RunAggregate createRunAggregate(final List<RunData> list) {
		BigDecimal miles = BigDecimal.ZERO;
		int minutes = 0;
		for (final RunData run : list) {
			miles = miles.add(run.getMiles());
			minutes += run.getMinutes();
		}
		return new RunAggregate(minutes, miles, list.size());
	}

}
