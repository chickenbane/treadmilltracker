package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
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
import android.view.Menu;
import android.view.View;
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

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_entry);

		mDateTime = new DateTime(DateFormat.getDateFormat(this));

		initTextListeners();
		setupDateTimeButtons();
		setupSaveButton();

		mSqliteHelper = RunSqlite.createSQLiteOpenHelper(this);
	}

	private void initTextListeners() {
		final EditText duration = (EditText) findViewById(R.id.editTextDuration);
		duration.addTextChangedListener(new TextWatcher() {

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

		final EditText distance = (EditText) findViewById(R.id.editTextDistance);
		distance.addTextChangedListener(new TextWatcher() {

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
	}

	private void setupSaveButton() {
		boolean canSave = true;

		final EditText duration = (EditText) findViewById(R.id.editTextDuration);
		final EditText distance = (EditText) findViewById(R.id.editTextDistance);

		final String minutes = duration.getText().toString();
		if (TextUtils.isEmpty(minutes)) {
			// duration.setError("Enter minutes");
			canSave = false;
		}
		final String miles = distance.getText().toString();
		if (TextUtils.isEmpty(miles)) {
			// distance.setError("Enter miles");
			canSave = false;
		}

		final Button save = (Button) findViewById(R.id.buttonSave);
		save.setEnabled(canSave);
	}

	private void setupDateTimeButtons() {
		final Button dateButton = (Button) findViewById(R.id.buttonStartDate);
		dateButton.setText(mDateTime.getDateText());

		final Button timeButton = (Button) findViewById(R.id.buttonStartTime);
		timeButton.setText(mDateTime.getTimeText());
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateDateTimeSecondsFromNow(0);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_entry, menu);
		return true;
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

		final EditText duration = (EditText) findViewById(R.id.editTextDuration);
		final EditText distance = (EditText) findViewById(R.id.editTextDistance);

		final int minutes = Integer.parseInt(duration.getText().toString());
		final String miles = distance.getText().toString();

		final ContentValues values = TreadmillTracker.createContentValues(
				mDateTime.getMillis(), minutes, miles);

		if (mSqliteHelper == null) {
			Log.wtf(TAG, "DB isn't around?");
			return;
		}

		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressSave);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(ProgressBar.VISIBLE);

		new SaveRunTask(mSqliteHelper).execute(values);

		Log.v(TAG, "Saved! values=" + values.toString());
	}

	private class SaveRunTask extends AsyncTask<ContentValues, Void, Long> {

		private final SQLiteOpenHelper mSqliteHelper;

		public SaveRunTask(final SQLiteOpenHelper sqliteHelper) {
			mSqliteHelper = sqliteHelper;
		}

		@Override
		protected Long doInBackground(final ContentValues... params) {
			final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
			final long rowId = db.insert(TreadmillTracker.Run.TABLE_NAME, null,
					params[0]);
			return rowId;
		}

		@Override
		protected void onPostExecute(final Long result) {
			Toast.makeText(getApplicationContext(), "Saved, rowId=" + result,
					Toast.LENGTH_SHORT).show();

			startActivity(new Intent(getApplicationContext(),
					EntryListActivity.class));
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
