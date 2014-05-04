package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RunAggregate {
	private final RunData mAggregate;
	private final BigDecimal mRuns;

	private final int mAvgMinutes;
	private final BigDecimal mAvgMiles;

	public RunAggregate(final int minutes, final BigDecimal miles,
			final BigDecimal runs) {
		mAggregate = new RunData(0, minutes, miles);
		mRuns = runs;

		if (BigDecimal.ZERO.compareTo(runs) == 0) {
			mAvgMinutes = 0;
			mAvgMiles = BigDecimal.ZERO;
		} else {
			mAvgMinutes = BigDecimal.valueOf(minutes)
					.divide(runs, 0, RoundingMode.HALF_UP).intValue();
			mAvgMiles = miles.divide(runs, 1, RoundingMode.HALF_UP);
		}
	}

	public int getAggregrateMinutes() {
		return mAggregate.getMinutes();
	}

	public BigDecimal getAggregateMiles() {
		return mAggregate.getMiles();
	}

	public String getAggregateMilesFormatted() {
		return mAggregate.getMilesFormatted();
	}

	public BigDecimal getAvgMiles() {
		return mAvgMiles;
	}

	public String getPace() {
		return mAggregate.getPace();
	}

	public String getMph() {
		return mAggregate.getMph();
	}

	public BigDecimal getRuns() {
		return mRuns;
	}

	public int getAvgMinutes() {
		return mAvgMinutes;
	}

	public String getAvgMilesFormatted() {
		return String.format("%.1f", mAvgMiles);

	}
}
