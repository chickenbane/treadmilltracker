package com.googlejobapp.treadmilltracker;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	private static final String TAG = "DatePickerFragment";
	private static final String BUNDLE_YEAR = "BUNDLE_YEAR";
	private static final String BUNDLE_MONTH = "BUNDLE_MONTH";
	private static final String BUNDLE_DAY = "BUNDLE_DAY";

	private DateTimeUpdater mCallback;

	@Override
	public Dialog onCreateDialog(Bundle bundle) {
		int year, month, day;

		if (bundle == null) {
			Log.w(TAG, "Created without a bundle");

			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
		} else {
			year = bundle.getInt(BUNDLE_YEAR);
			month = bundle.getInt(BUNDLE_MONTH);
			day = bundle.getInt(BUNDLE_DAY);
		}

		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// I learned this from you!
		// http://developer.android.com/training/basics/fragments/communicating.html

		try {
			mCallback = (DateTimeUpdater) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DateTimeUpdater");
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		mCallback.updateDate(year, month, day);
	}

	public static DatePickerFragment newInstance(DateTime dateTime) {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_YEAR, dateTime.getYear());
		bundle.putInt(BUNDLE_MONTH, dateTime.getMonth());
		bundle.putInt(BUNDLE_DAY, dateTime.getDay());

		DatePickerFragment f = new DatePickerFragment();
		f.setArguments(bundle);
		Log.w(TAG, "Allo, my day is=" + dateTime.getDay());

		return f;
	}
}
