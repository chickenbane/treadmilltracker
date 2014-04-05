package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEntryActivity extends Activity implements
		DatePickerFragment.DateTimeActivity,
		TimePickerFragment.DateTimeActivity {
	private final static String TAG = "AddEntryActivity";

	private SQLiteOpenHelper mSqliteHelper;
	private DateTime mDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_entry);

		mDateTime = new DateTime(DateFormat.getDateFormat(this));

		initTextListeners();
		setupDateTimeButtons();
		setupSaveButton();

		mSqliteHelper = Db.createSQLiteOpenHelper(this);
	}

	private void initTextListeners() {
		EditText duration = (EditText) findViewById(R.id.editTextDuration);
		duration.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				/*
				 * After the edit the duration, see if we can enable the save
				 * button. Also, recalculate the DateTime based on the duration
				 * we just entered.
				 */
				setupSaveButton();
				String minutes = s.toString();
				int seconds = 0;
				if (!TextUtils.isEmpty(minutes)) {
					seconds = Integer.parseInt(minutes) * 60;
				}
				updateDateTimeSecondsFromNow(seconds);

			}

		});

		EditText distance = (EditText) findViewById(R.id.editTextDistance);
		distance.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				setupSaveButton();
			}
		});
	}

	private void setupSaveButton() {
		boolean canSave = true;

		EditText duration = (EditText) findViewById(R.id.editTextDuration);
		EditText distance = (EditText) findViewById(R.id.editTextDistance);

		String minutes = duration.getText().toString();
		if (TextUtils.isEmpty(minutes)) {
			// duration.setError("Enter minutes");
			canSave = false;
		}
		String miles = distance.getText().toString();
		if (TextUtils.isEmpty(miles)) {
			// distance.setError("Enter miles");
			canSave = false;
		}

		Button save = (Button) findViewById(R.id.buttonSave);
		save.setEnabled(canSave);
	}

	private void setupDateTimeButtons() {
		Button dateButton = (Button) findViewById(R.id.buttonStartDate);
		dateButton.setText(mDateTime.getDateText());

		Button timeButton = (Button) findViewById(R.id.buttonStartTime);
		timeButton.setText(mDateTime.getTimeText());
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateDateTimeSecondsFromNow(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_entry, menu);
		return true;
	}

	public void clickDate(View view) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public void clickTime(View view) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}

	public void clickSave(View view) {

		Spinner spinner = (Spinner) findViewById(0);
		int pos = spinner.getSelectedItemPosition();

		TimePicker picker = (TimePicker) findViewById(0);

		long startTime = picker.getCurrentHour() * 60
				+ picker.getCurrentMinute();

		EditText dist = (EditText) findViewById(R.id.editTextDistance);
		String distance = dist.getText().toString();

		Log.v(TAG, "Saved! Pos=" + pos + " time=" + startTime + " dist="
				+ distance);

		if (mSqliteHelper == null) {
			Log.wtf(TAG, "DB isn't around?");
			return;
		}

		ContentValues values = Db.createContentValues(0, distance, startTime);

		SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
		long rowId = db.insert(Db.Entry.TABLE_NAME, null, values);

		Toast.makeText(getApplicationContext(), "Saved, rowId=" + rowId,
				Toast.LENGTH_SHORT).show();

		startActivity(new Intent(this, EntryListActivity.class));
	}

	@Override
	public DateTime getDateTime() {
		return mDateTime;
	}

	@Override
	public void updateDate(int year, int month, int day) {
		mDateTime.updateDate(year, month, day);
		setupDateTimeButtons();
	}

	@Override
	public void updateTime(int hourOfDay, int minute) {
		mDateTime.updateTime(hourOfDay, minute);
		setupDateTimeButtons();
	}

	private void updateDateTimeSecondsFromNow(int seconds) {
		mDateTime.updateDateTimeSecondsFromNow(seconds);
		setupDateTimeButtons();
	}

}
