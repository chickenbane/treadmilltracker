package com.googlejobapp.treadmilltracker;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEntryActivity extends Activity implements
		DatePickerFragment.DateTimeActivity,
		TimePickerFragment.DateTimeActivity {
	private final static String TAG = "AddEntryActivity";
	public final static int[] DURATIONS = { 30, 40, 45, 50, 60 };

	private SQLiteOpenHelper mSqliteHelper;
	private DateTime mDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_entry);

		mDateTime = new DateTime(DateFormat.getDateFormat(this));

		setupStartDateButton();
		setupDurationSpinner();
		setupStartTimeButton();
		// setupStartTimePicker(0);

		mSqliteHelper = Db.createSQLiteOpenHelper(this);
	}

	private void setupStartDateButton() {
		Button button = (Button) findViewById(R.id.buttonStartDate);
		button.setText(mDateTime.getDateText());
	}

	private void setupStartTimeButton() {
		Button button = (Button) findViewById(R.id.buttonStartTime);
		button.setText(mDateTime.getTimeText());
	}

	private void setupDurationSpinner() {
		String[] spinnerLabels = new String[DURATIONS.length];
		for (int i = 0; i < DURATIONS.length; i++) {
			spinnerLabels[i] = DURATIONS[i] + " min";
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, spinnerLabels);

		Spinner spinner = (Spinner) findViewById(R.id.spinnerDuration);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Log.v(TAG, "new pos: " + pos);

				// setupStartTimePicker(DURATIONS[pos]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void setupStartTimePicker(int minus) {
		final Calendar c = Calendar.getInstance();
		long startTime = c.getTimeInMillis() - (minus * 60 * 1000);
		c.setTimeInMillis(startTime);
		// int hour = c.get(Calendar.HOUR_OF_DAY);
		// int minute = c.get(Calendar.MINUTE);
		//
		// // TODO This looks wrong
		// if (minute - minus < 0) {
		// minute = 60 + minute - minus;
		// hour--;
		// }

		Log.v(TAG, "Herro!");
		TimePicker picker = (TimePicker) findViewById(0);
		picker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(c.get(Calendar.MINUTE));

	}

	@Override
	protected void onResume() {
		super.onResume();
		// setupStartTimePicker(0);
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

		Spinner spinner = (Spinner) findViewById(R.id.spinnerDuration);
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

		ContentValues values = Db.createContentValues(DURATIONS[pos], distance,
				startTime);

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
		setupStartDateButton();

	}

	@Override
	public void updateTime(int hourOfDay, int minute) {
		int floorMin;
		if (minute > 45) {
			floorMin = 45;
		} else if (minute > 30) {
			floorMin = 30;
		} else if (minute > 15) {
			floorMin = 15;
		} else {
			floorMin = 0;
		}

		mDateTime.updateTime(hourOfDay, floorMin);
		setupStartTimeButton();
	}

}
