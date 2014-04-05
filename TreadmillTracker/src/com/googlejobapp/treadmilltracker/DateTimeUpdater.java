package com.googlejobapp.treadmilltracker;

public interface DateTimeUpdater {
	void updateTime(int hour, int min);

	void updateDate(int year, int month, int day);
}
