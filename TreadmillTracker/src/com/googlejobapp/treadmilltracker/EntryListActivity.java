package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.Calendar;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EntryListActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "EntryListActivity";

	private SimpleCursorAdapter mAdapter;
	private ProgressBar mProgressBar;
	private SQLiteOpenHelper mSqliteHelper;
	private TextView mThisWeekTextView;
	private TextView mLastWeekTextView;
	private TextView mStreakTextView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry_list);

		mProgressBar = (ProgressBar) findViewById(R.id.progressSave);
		mProgressBar.setIndeterminate(true);

		mAdapter = new SimpleCursorAdapter(this, R.layout.row_entry_list, null,
				RunDao.QUERY_COLUMNS, new int[] { R.id.textViewMain,
						R.id.textViewDate }, 0);
		mAdapter.setViewBinder(new SimpleViewBinder());
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);

		mSqliteHelper = RunDao.createSQLiteOpenHelper(this);
		final View header = getLayoutInflater().inflate(
				R.layout.header_entry_list, null);
		getListView().addHeaderView(header, null, false);

		mThisWeekTextView = (TextView) header
				.findViewById(R.id.textViewThisWeek);
		mLastWeekTextView = (TextView) header
				.findViewById(R.id.textViewLastWeek);
		mStreakTextView = (TextView) header.findViewById(R.id.textViewStreak);
		new ListSummaryTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			startActivity(new Intent(this, AddEntryActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void clickAdd(final View view) {
		startActivity(new Intent(this, AddEntryActivity.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		Log.v(TAG, "creating loader");
		mProgressBar.setVisibility(ProgressBar.VISIBLE);
		return RunDao.createLoader(this);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mProgressBar.setVisibility(ProgressBar.GONE);
		new ListSummaryTask().execute();
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mProgressBar.setVisibility(ProgressBar.GONE);
		mAdapter.swapCursor(null);
	}

	@Override
	protected void onListItemClick(final ListView l, final View v,
			final int position, final long id) {

		final Intent intent = new Intent(this, AddEntryActivity.class);
		intent.putExtra(AddEntryActivity.EXTRA_RUN_ID, id);
		Log.i(TAG, "Putting runId=" + id);
		startActivity(intent);
	}

	private static class SimpleViewBinder implements
			SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(final View view, final Cursor cursor,
				final int columnIndex) {

			final TextView tv = (TextView) view;
			final RunData runData = RunDao.createRunData(cursor);

			if (columnIndex == 0) {
				final BigDecimal miles = new BigDecimal(runData.getDistance());
				final int minutes = runData.getMinutes();
				tv.setText(String.format("%.1f miles, %d minutes", miles,
						minutes));
				return true;
			}

			else if (columnIndex == 1) {
				final Calendar c = Calendar.getInstance();
				c.setTimeInMillis(runData.getStartTime());
				tv.setText(String.format("%tD %tl:%tM %tp%n", c, c, c, c));
				return true;
			}
			return false;
		}
	}

	/*
	 * TODO I am very sure there has to be a better way to do this. However, I
	 * don't know what that way is. In the meantime, just do three queries.
	 */
	private class ListSummaryTask extends AsyncTask<Void, Void, Void> {

		private final static long WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000;

		private int mWeekMinutes, mLastMinutes;
		private BigDecimal mWeekMiles, mLastMiles;
		private int mStreakDays;

		@Override
		protected Void doInBackground(final Void... params) {
			final SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
			final long now = Calendar.getInstance().getTimeInMillis();
			final long weekAgo = now - WEEK_MILLIS;
			final long twoWeeksAgo = now - (2 * WEEK_MILLIS);
			final RunData week = RunDao.queryForSummary(db, weekAgo, now);
			mWeekMinutes = week.getMinutes();
			mWeekMiles = new BigDecimal(week.getDistance());
			final RunData lastWeek = RunDao.queryForSummary(db, twoWeeksAgo,
					weekAgo);
			mLastMinutes = lastWeek.getMinutes();
			mLastMiles = new BigDecimal(lastWeek.getDistance());

			mStreakDays = RunDao.queryForStreak(db, now);
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mThisWeekTextView.setText(String.format("%.1f miles, %d minutes",
					mWeekMiles, mWeekMinutes));
			mLastWeekTextView.setText(String.format(
					"%.1f miles, %d minutes last week", mLastMiles,
					mLastMinutes));
			mStreakTextView.setText(mStreakDays + " days in a row");
		}

	}

}
