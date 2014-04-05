package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	private DateTimeActivity mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		DateTime dateTime = mCallback.getDateTime();

		int hour = dateTime.getHourOfDay();
		int minute = dateTime.getMinute();

		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallback = (DateTimeActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DateTimeActivity");
		}
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mCallback.updateTime(hourOfDay, minute);
	}

	public interface DateTimeActivity {

		DateTime getDateTime();

		void updateTime(int hourOfDay, int minute);
	}
}
