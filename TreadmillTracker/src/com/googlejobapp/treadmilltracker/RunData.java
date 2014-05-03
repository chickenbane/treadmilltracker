package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

import android.text.format.Time;

public class RunData {
	private final long mStartTime;
	private final int mMinutes;
	private final String mWeek;
	private final String mPace;
	private final String mMph;
	private final String mMiles;

	final BigDecimal bdMiles;

	private static final BigDecimal SIXTY = BigDecimal.valueOf(60);

	public RunData(final long startTime, final int minutes, final String miles) {
		mStartTime = startTime;
		mMinutes = minutes;

		bdMiles = new BigDecimal(miles);
		final BigDecimal bdMinutes = BigDecimal.valueOf(minutes);

		mMiles = String.format("%.1f", bdMiles);

		final BigDecimal seconds = SIXTY.multiply(bdMinutes);
		final BigDecimal paceSecs = seconds.divideToIntegralValue(bdMiles);
		final BigDecimal[] dr = paceSecs.divideAndRemainder(SIXTY);

		mPace = String.format("%d:%02d", dr[0].intValue(), dr[1].intValue());

		final BigDecimal mph = bdMiles.multiply(SIXTY).divide(bdMinutes, 1,
				RoundingMode.HALF_UP);
		mMph = String.format("%.1f", mph);

		mWeek = getWeekFromMillis(startTime);
	}

	public long getStartTime() {
		return mStartTime;
	}

	public int getMinutes() {
		return mMinutes;
	}

	public String getWeek() {
		return mWeek;
	}

	public String getMilesFormatted() {
		return mMiles;
	}

	public BigDecimal getMiles() {
		return bdMiles;
	}

	public String getPace() {
		return mPace;
	}

	public String getMph() {
		return mMph;
	}

	/**
	 * A string which is the time's year and week number appended together. The
	 * week number is zero padded. For example, "201408" or "201420". This is
	 * returned as a string so it can be used to group weeks across years.
	 * Probably a bad idea, but you also convert to an int and compare them and
	 * they'll naturally be in chronological order.
	 * 
	 * @param millis
	 *            The time in millis
	 * @return A string which represents the year and week number.
	 */
	public static String getWeekFromMillis(final long millis) {
		final Time t = new Time();
		t.set(millis);
		final StringBuilder sb = new StringBuilder();
		sb.append(t.year);
		final int weekNumber = t.getWeekNumber();
		if (weekNumber < 10) {
			sb.append('0');
		}
		sb.append(weekNumber);
		return sb.toString();
	}
}
