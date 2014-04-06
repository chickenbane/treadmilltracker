package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;

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
		mAdapter.setViewBinder(new SimpleViewBinder());
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

		@Override
		public boolean setViewValue(final View view, final Cursor cursor,
				final int columnIndex) {

			final TextView tv = (TextView) view;
			final String value = cursor.getString(columnIndex);

			if (columnIndex == 0) {
				final BigDecimal miles = new BigDecimal(value);
				final int minutes = Integer.parseInt(cursor
						.getString(RunSqlite.QUERY_COLUMN_DURATION_MINS));
				tv.setText(String.format(Locale.US, "%.1f miles, %d minutes",
						miles, minutes));
				return true;
			}

			else if (columnIndex == 1) {
				final Calendar c = Calendar.getInstance();
				c.setTimeInMillis(Long.parseLong(value));
				tv.setText(String.format("%tD %tl:%tM %tp%n", c, c, c, c));
				return true;
			}
			Log.v(TAG, "index=" + columnIndex + " string=" + value);
			return false;
		}
	}

}
