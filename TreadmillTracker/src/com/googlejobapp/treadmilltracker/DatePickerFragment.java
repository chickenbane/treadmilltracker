package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	private DateTimeActivity mCallback;

	@Override
	public Dialog onCreateDialog(Bundle bundle) {
		DateTime dateTime = mCallback.getDateTime();

		int year = dateTime.getYear();
		int month = dateTime.getMonth();
		int day = dateTime.getDay();

		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// I learned this from you!
		// http://developer.android.com/training/basics/fragments/communicating.html

		try {
			mCallback = (DateTimeActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DateTimeActivity");
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		mCallback.updateDate(year, month, day);
	}

	public interface DateTimeActivity {

		DateTime getDateTime();

		void updateDate(int year, int month, int day);
	}
}
