package com.googlejobapp.treadmilltracker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Just use Joda?
 */
public class DateTime implements DateTimeUpdater {
	private final Calendar mCalendar;
	private final DateFormat mDateFormat;

	public DateTime(DateFormat dateFormat) {
		mDateFormat = dateFormat;
		mCalendar = Calendar.getInstance();
	}

	public String getDateText() {
		return mDateFormat.format(mCalendar.getTime());
	}

	public int getYear() {
		return mCalendar.get(Calendar.YEAR);
	}

	public int getMonth() {
		return mCalendar.get(Calendar.MONTH);

	}

	public int getDay() {
		return mCalendar.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public void updateTime(int hour, int min) {
		int year = mCalendar.get(Calendar.YEAR);
		int month = mCalendar.get(Calendar.MONTH);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);
		mCalendar.set(year, month, day, hour, min);
	}

	@Override
	public void updateDate(int year, int month, int day) {
		mCalendar.set(year, month, day);
	}

}
