package com.googlejobapp.treadmilltracker;

public class RunData {
	private final long mStartTime;
	private final int mMinutes;
	private final String mDistance;

	public RunData(final long startTime, final int minutes,
			final String distance) {
		mStartTime = startTime;
		mMinutes = minutes;
		mDistance = distance;
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

}
