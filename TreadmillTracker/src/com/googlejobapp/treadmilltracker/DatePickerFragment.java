package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	private static final String TAG = "DatePickerFragment";
	private static final String BUNDLE_YEAR = "BUNDLE_YEAR";
	private static final String BUNDLE_MONTH = "BUNDLE_MONTH";
	private static final String BUNDLE_DAY = "BUNDLE_DAY";

	private HasDateTime mCallback;

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
			mCallback = (HasDateTime) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DateTimeUpdater");
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		mCallback.updateDate(year, month, day);
	}

}
