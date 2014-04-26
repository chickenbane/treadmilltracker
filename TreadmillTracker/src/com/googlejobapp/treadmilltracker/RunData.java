package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RunData {
	private final long mStartTime;
	private final int mMinutes;
	private final String mDistance;

	final BigDecimal bdMiles;
	final BigDecimal bdMinutes;

	private static final BigDecimal SIXTY = BigDecimal.valueOf(60);

	public RunData(final long startTime, final int minutes,
			final String distance) {
		mStartTime = startTime;
		mMinutes = minutes;
		mDistance = distance;

		bdMiles = new BigDecimal(distance);
		bdMinutes = BigDecimal.valueOf(minutes);
	}

	public long getStartTime() {
		return mStartTime;
	}

	public int getMinutes() {
		return mMinutes;
	}

	public String getDistance() {
		return mDistance;
	}

	public String getMiles() {
		return String.format("%.1f", bdMiles);
	}

	public String getPace() {
		final BigDecimal seconds = SIXTY.multiply(bdMinutes);
		final BigDecimal paceSecs = seconds.divideToIntegralValue(bdMiles);
		final BigDecimal[] dr = paceSecs.divideAndRemainder(SIXTY);

		return String.format("%d:%02d", dr[0].intValue(), dr[1].intValue());
	}

	public String getMph() {
		final BigDecimal mph = bdMiles.multiply(SIXTY).divide(bdMinutes, 1,
				RoundingMode.HALF_UP);
		return String.format("%.1f", mph);
	}
}
