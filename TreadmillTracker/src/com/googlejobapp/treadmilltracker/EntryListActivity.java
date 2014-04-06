package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EntryListActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "EntryListActivity";

	private SimpleCursorAdapter mAdapter;

	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry_list);

		mProgressBar = (ProgressBar) findViewById(R.id.progressSave);
		mProgressBar.setIndeterminate(true);

		mAdapter = new SimpleCursorAdapter(this, R.layout.row_entry_list, null,
				RunSqlite.QUERY_COLUMNS, new int[] { R.id.textViewMain,
						R.id.textViewDate }, 0);
		mAdapter.setViewBinder(new SimpleViewBinder(
				android.text.format.DateFormat.getDateFormat(this),
				android.text.format.DateFormat.getTimeFormat(this)));
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry_list, menu);
		return true;
	}

	public void clickAdd(final View view) {
		startActivity(new Intent(this, AddEntryActivity.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		Log.v(TAG, "creating loader");
		mProgressBar.setVisibility(ProgressBar.VISIBLE);
		return RunSqlite.createLoader(this);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mProgressBar.setVisibility(ProgressBar.GONE);
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mProgressBar.setVisibility(ProgressBar.GONE);
		mAdapter.swapCursor(null);
	}

	/*
	 * This class should be moved to the RunSqlite impl class
	 */
	private static class SimpleViewBinder implements
			SimpleCursorAdapter.ViewBinder {

		private final DateFormat mDateFormat;
		private final DateFormat mTimeFormat;
		private final Calendar mCalendar;

		public SimpleViewBinder(final DateFormat dateFormat,
				final DateFormat timeFormat) {
			mDateFormat = dateFormat;
			mTimeFormat = timeFormat;
			mCalendar = Calendar.getInstance();
		}

		@Override
		public boolean setViewValue(final View view, final Cursor cursor,
				final int columnIndex) {

			if (columnIndex == 0) {
				final TextView tv = (TextView) view;
				final BigDecimal miles = new BigDecimal(cursor.getString(0));
				final int minutes = Integer.parseInt(cursor.getString(2));
				tv.setText(String.format("%.1f miles, %d minutes", miles,
						minutes));
				return true;
			}

			else if (columnIndex == 1) {
				final TextView tv = (TextView) view;
				final String string = cursor.getString(columnIndex);
				mCalendar.setTimeInMillis(Long.parseLong(string));
				final StringBuilder sb = new StringBuilder();
				final Date dateTime = mCalendar.getTime();
				sb.append(mDateFormat.format(dateTime));
				sb.append(" ");
				sb.append(mTimeFormat.format(dateTime));
				tv.setText(sb.toString());
				final Calendar c = mCalendar;
				tv.setText(String.format("%tD %tl:%tM %tp%n", c, c, c, c));
				return true;
			}
			final String string = cursor.getString(columnIndex);
			Log.v(TAG, "index=" + columnIndex + " string=" + string);
			return false;
		}
	}

}
