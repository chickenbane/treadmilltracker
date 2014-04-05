package com.googlejobapp.treadmilltracker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Just use Joda?
 */
public class DateTime {
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

		// Rounding sounds like a good idea
		int floorMin;
		if (minute >= 45) {
			floorMin = 45;
		} else if (minute >= 30) {
			floorMin = 30;
		} else if (minute >= 15) {
			floorMin = 15;
		} else {
			floorMin = 0;
		}

		mCalendar.set(year, month, day, hourOfDay, floorMin);
	}

	/**
	 * Updates the date time to now minus "seconds". Therefore, if you pass 60
	 * you'll set the time (and the date!) to one minute ago.
	 * 
	 * Obviously I am making this more complicated, but I am designing for
	 * minimal modification of date and time, instead doing it implicitly like
	 * so. I want good defaults!
	 * 
	 * @param seconds
	 *            Seconds to subtract.
	 */
	public void updateDateTimeSecondsFromNow(int seconds) {
		Calendar c = Calendar.getInstance();
		long past = c.getTimeInMillis() - (1000 * seconds);
		mCalendar.setTimeInMillis(past);

		// To keep things consistent, send this through updateTime's rounding
		// nonsense
		int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
		int min = mCalendar.get(Calendar.MINUTE);
		updateTime(hour, min);
	}

	public void updateDate(int year, int month, int day) {
		mCalendar.set(year, month, day);
	}

	public String getDateText() {
		Calendar today = Calendar.getInstance();
		int todayYear = today.get(Calendar.YEAR);
		int todayMonth = today.get(Calendar.MONTH);
		int todayDay = today.get(Calendar.DAY_OF_MONTH);
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
