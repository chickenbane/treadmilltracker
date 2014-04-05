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
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final DateTime dateTime = mCallback.getDateTime();

		final int hour = dateTime.getHourOfDay();
		final int minute = dateTime.getMinute();

		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		try {
			mCallback = (DateTimeActivity) activity;
		} catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DateTimeActivity");
		}
	}

	@Override
	public void onTimeSet(final TimePicker view, final int hourOfDay,
			final int minute) {
		mCallback.updateTime(hourOfDay, minute);
	}

	protected interface DateTimeActivity {

		DateTime getDateTime();

		void updateTime(int hourOfDay, int minute);
	}
}
