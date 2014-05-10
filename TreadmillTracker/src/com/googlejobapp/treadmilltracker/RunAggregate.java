package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RunAggregate {
	private final RunData mAggregate;
	private final int mRuns;
	private final long mFirst;
	private final long mLast;

	private final int mAvgMinutes;
	private final String mAvgMiles;
	private final String mString;

	public RunAggregate(final int minutes, final BigDecimal miles,
			final int runs, final long first, final long last) {
		mAggregate = new RunData(minutes, miles);
		mRuns = runs;
		mFirst = first;
		mLast = last;

		if (runs == 0) {
			mAvgMinutes = 0;
			mAvgMiles = "0";
		} else {
			final BigDecimal bdRuns = BigDecimal.valueOf(runs);
			mAvgMinutes = BigDecimal.valueOf(minutes)
					.divide(bdRuns, 0, RoundingMode.HALF_UP).intValue();
			final BigDecimal bdAvgMiles = miles.divide(bdRuns, 1,
					RoundingMode.HALF_UP);
			mAvgMiles = String.format("%.1f", bdAvgMiles);
		}

		mString = String.format("%d | %s  %d | %s  %s | %s (%d)",
				getAvgMinutes(), getAvgMiles(), getAggregrateMinutes(),
				getAggregateMiles(), getPace(), getMph(), getRuns());
	}

	public int getAggregrateMinutes() {
		return mAggregate.getMinutes();
	}

	public String getAggregateMiles() {
		return mAggregate.getMilesFormatted();
	}

	public String getPace() {
		return mAggregate.getPace();
	}

	public String getMph() {
		return mAggregate.getMph();
	}

	public int getRuns() {
		return mRuns;
	}

	public int getAvgMinutes() {
		return mAvgMinutes;
	}

	public String getAvgMiles() {
		return mAvgMiles;
	}

	public long getFirstStartTime() {
		return mFirst;
	}

	public long getLastStartTime() {
		return mLast;
	}

	@Override
	public String toString() {
		return mString;
	}
}
