package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AddEntryActivity extends Activity implements
		DatePickerFragment.DateTimeActivity,
		TimePickerFragment.DateTimeActivity {
	private final static String TAG = "AddEntryActivity";

	private SQLiteOpenHelper mSqliteHelper;
	private DateTime mDateTime;

	private EditText mDurationEditText;
	private EditText mDistanceEditText;
	private Button mDateButton;
	private Button mTimeButton;
	private Button mSaveButton;

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_add_entry);

		mDateTime = new DateTime(DateFormat.getDateFormat(this));
		mDurationEditText = (EditText) findViewById(R.id.editTextDuration);
		mDistanceEditText = (EditText) findViewById(R.id.editTextDistance);
		mDateButton = (Button) findViewById(R.id.buttonStartDate);
		mTimeButton = (Button) findViewById(R.id.buttonStartTime);
		mSaveButton = (Button) findViewById(R.id.buttonSave);

		mSqliteHelper = RunDao.getInstance(this);

		setupDateTimeButtons();

		initTextListeners();
		setupSaveButton();

	}

	private void initTextListeners() {
		mDurationEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(final CharSequence s, final int start,
					final int before, final int count) {

			}

			@Override
			public void beforeTextChanged(final CharSequence s,
					final int start, final int count, final int after) {

			}

			@Override
			public void afterTextChanged(final Editable s) {
				/*
				 * After the edit the duration, see if we can enable the save
				 * button. Also, recalculate the DateTime based on the duration
				 * we just entered.
				 */
				setupSaveButton();
				final String minutes = s.toString();
				int seconds = 0;
				if (!TextUtils.isEmpty(minutes)) {
					seconds = Integer.parseInt(minutes) * 60;
				}
				updateDateTimeSecondsFromNow(seconds);

			}

		});

		mDistanceEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(final CharSequence s, final int start,
					final int before, final int count) {

			}

			@Override
			public void beforeTextChanged(final CharSequence s,
					final int start, final int count, final int after) {

			}

			@Override
			public void afterTextChanged(final Editable s) {
				setupSaveButton();
			}
		});

		mDurationEditText.postDelayed(new Runnable() {
			@Override
			public void run() {
				final InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(mDurationEditText, 0);
			}
		}, 50);
	}

	private void setupSaveButton() {
		final String minutes = mDurationEditText.getText().toString();
		final String miles = mDistanceEditText.getText().toString();

		// If either field is blank, then we can't save
		final boolean canSave = !(TextUtils.isEmpty(minutes) || TextUtils
				.isEmpty(miles));

		mSaveButton.setEnabled(canSave);
	}

	private void setupDateTimeButtons() {
		mDateButton.setText(mDateTime.getDateText());
		mTimeButton.setText(mDateTime.getTimeText());
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateDateTimeSecondsFromNow(0);

	}

	public void clickDate(final View view) {
		final DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public void clickTime(final View view) {
		final DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}

	public void clickSave(final View view) {
		final int minutes = Integer.parseInt(mDurationEditText.getText()
				.toString());
		final String miles = mDistanceEditText.getText().toString();
		final ContentValues values = TreadmillTracker.createContentValues(
				mDateTime.getMillis(), minutes, miles);

		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressSave);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(ProgressBar.VISIBLE);

		new SaveRunTask().execute(values);

		Log.v(TAG, "Saved! values=" + values.toString());
	}

	private class SaveRunTask extends AsyncTask<ContentValues, Void, Long> {

		@Override
		protected Long doInBackground(final ContentValues... params) {
			final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
			return RunDao.insertRun(db, params[0]);
		}

		@Override
		protected void onPostExecute(final Long result) {
			Toast.makeText(getApplicationContext(), "Saved, rowId=" + result,
					Toast.LENGTH_SHORT).show();

			final Intent intent = new Intent(getApplicationContext(),
					RunListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}

	}

	@Override
	public DateTime getDateTime() {
		return mDateTime;
	}

	@Override
	public void updateDate(final int year, final int month, final int day) {
		mDateTime.updateDate(year, month, day);
		setupDateTimeButtons();
	}

	@Override
	public void updateTime(final int hourOfDay, final int minute) {
		mDateTime.updateTime(hourOfDay, minute);
		setupDateTimeButtons();
	}

	private void updateDateTimeSecondsFromNow(final int seconds) {
		mDateTime.updateDateTimeSecondsFromNow(seconds);
		setupDateTimeButtons();
	}

}
