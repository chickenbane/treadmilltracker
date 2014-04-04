package com.googlejobapp.treadmilltracker;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEntryActivity extends Activity {
	private final static String TAG = "AddEntryActivity";
	public final static int[] DURATIONS = { 30, 40, 45, 50, 60 };

	private SQLiteOpenHelper mSqliteHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_entry);
		setupDurationSpinner();
		setupStartTimePicker();
		mSqliteHelper = Db.createSQLiteOpenHelper(getApplicationContext());
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
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Log.v(TAG, "new pos: " + pos);

				setupStartTimePicker(DURATIONS[pos]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupStartTimePicker();
	}

	private void setupStartTimePicker() {
		setupStartTimePicker(0);
	}

	private void setupStartTimePicker(int minus) {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// TODO This looks wrong
		if (minute - minus < 0) {
			minute = 60 + minute - minus;
			hour--;
		}

		TimePicker picker = (TimePicker) findViewById(R.id.timePickerStartTime);
		picker.setCurrentHour(hour);
		picker.setCurrentMinute(minute);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_entry, menu);
		return true;
	}

	public void clickSave(View view) {

		Spinner spinner = (Spinner) findViewById(R.id.spinnerDuration);
		int pos = spinner.getSelectedItemPosition();

		TimePicker picker = (TimePicker) findViewById(R.id.timePickerStartTime);

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
}
