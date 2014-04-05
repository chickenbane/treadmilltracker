package com.googlejobapp.treadmilltracker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Just use Joda?
 */
public class DateTime {
	/**
	 * TODO How long will this variable stick around?
	 */
	private static final Calendar TODAY = Calendar.getInstance();
	private final Calendar mCalendar;
	private final DateFormat mDateFormat;
	private final DateFormat mTimeFormat;

	public DateTime(DateFormat dateFormat) {
		mDateFormat = dateFormat;
		mCalendar = Calendar.getInstance();
		mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
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

	public int getHourOfDay() {
		return mCalendar.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return mCalendar.get(Calendar.MINUTE);
	}

	public void updateTime(int hourOfDay, int minute) {
		int year = mCalendar.get(Calendar.YEAR);
		int month = mCalendar.get(Calendar.MONTH);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);
		mCalendar.set(year, month, day, hourOfDay, minute);
	}

	public void updateDate(int year, int month, int day) {
		mCalendar.set(year, month, day);
	}

	public String getDateText() {
		int todayYear = TODAY.get(Calendar.YEAR);
		int todayMonth = TODAY.get(Calendar.MONTH);
		int todayDay = TODAY.get(Calendar.DAY_OF_MONTH);
		int year = mCalendar.get(Calendar.YEAR);
		int month = mCalendar.get(Calendar.MONTH);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);

		StringBuilder sb = new StringBuilder(mDateFormat.format(mCalendar
				.getTime()));
		if (year == todayYear && month == todayMonth && day == todayDay) {
			sb.append(" (today)");
		}

		return sb.toString();
	}

	public String getTimeText() {
		return mTimeFormat.format(mCalendar.getTime());
	}

}
