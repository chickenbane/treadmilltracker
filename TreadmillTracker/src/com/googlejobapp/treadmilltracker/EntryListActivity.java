package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.Calendar;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class EntryListActivity extends Activity implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "EntryListActivity";

	private SimpleCursorAdapter mAdapter;
	private ProgressBar mProgressBar;
	private SQLiteOpenHelper mSqliteHelper;
	private TextView mThisWeekTextView;
	private TextView mLastWeekTextView;
	private TextView mStreakTextView;
	private ListView mListView;

	private ActionMode mActionMode;
	private int mActionModeCheckedPos;
	private long mDeleteId;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry_list);

		mProgressBar = (ProgressBar) findViewById(R.id.progressSave);
		mProgressBar.setIndeterminate(true);

		mAdapter = new SimpleCursorAdapter(this, R.layout.row_entry_list, null,
				RunDao.QUERY_COLUMNS, new int[] { R.id.textViewMinutes,
						R.id.textViewMiles, R.id.textViewPace,
						R.id.textViewDate }, 0);
		mAdapter.setViewBinder(new SimpleViewBinder());
		getLoaderManager().initLoader(0, null, this);

		mListView = (ListView) findViewById(R.id.listViewRuns);
		mListView.setAdapter(mAdapter);
		mListView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		mListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(final AdapterView<?> parent,
							final View view, final int position, final long id) {
						if (mActionMode != null) {
							return false;
						}

						mActionMode = EntryListActivity.this
								.startActionMode(mActionModeCallback);
						mListView.setItemChecked(position, true);
						mActionModeCheckedPos = position;
						mDeleteId = id;

						return true;
					}
				});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				if (mActionMode != null) {
					mActionMode.finish();
				}
				mListView.setItemChecked(position, false);
				mActionModeCheckedPos = position;
			}

		});

		mSqliteHelper = RunDao.getInstance(this);

		final View header = getLayoutInflater().inflate(
				R.layout.header_entry_list, null);
		mListView.addHeaderView(header);

		mThisWeekTextView = (TextView) findViewById(R.id.textViewThisWeek);
		mLastWeekTextView = (TextView) findViewById(R.id.textViewLastWeek);
		mStreakTextView = (TextView) findViewById(R.id.textViewStreak);

		new ListSummaryTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
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

	private static class SimpleViewBinder implements
			SimpleCursorAdapter.ViewBinder {

		private static final BigDecimal SIXTY = BigDecimal.valueOf(60);

		// TODO Seems like a better approach would be to @Override bindView()
		@Override
		public boolean setViewValue(final View view, final Cursor cursor,
				final int columnIndex) {

			final TextView tv = (TextView) view;

			if (columnIndex == 0) {
				final int minutes = cursor
						.getInt(RunDao.QUERY_COLUMN_DURATION_MINS);
				tv.setText(String.format("%d mins", minutes));
				return true;
			}

			else if (columnIndex == 1) {
				final String mi = cursor
						.getString(RunDao.QUERY_COLUMN_DISTANCE_MILES);
				final BigDecimal milesBd = new BigDecimal(mi);
				tv.setText(String.format("%.1f m", milesBd));
				return true;
			}

			else if (columnIndex == 2) {
				final int mins = cursor
						.getInt(RunDao.QUERY_COLUMN_DURATION_MINS);
				final BigDecimal seconds = SIXTY.multiply(new BigDecimal(mins));
				final String mi = cursor
						.getString(RunDao.QUERY_COLUMN_DISTANCE_MILES);
				final BigDecimal miles = new BigDecimal(mi);

				final BigDecimal paceSecs = seconds
						.divideToIntegralValue(miles);
				final BigDecimal[] dandr = paceSecs.divideAndRemainder(SIXTY);

				tv.setText(String.format("%d:%02d min/m", dandr[0].intValue(),
						dandr[1].intValue()));
				return true;

			} else if (columnIndex == 3) {
				final Calendar c = Calendar.getInstance();
				c.setTimeInMillis(cursor
						.getLong(RunDao.QUERY_COLUMN_START_TIME));
				tv.setText(String.format("%tD", c));
				return true;
			}

			Log.i(TAG, "setViewValue() for colIndex=" + columnIndex);
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

	private class DeleteRunTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(final Long... params) {
			final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
			RunDao.deleteRun(db, params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			Toast.makeText(getApplicationContext(), "Deleted!",
					Toast.LENGTH_SHORT).show();

			mProgressBar.setVisibility(ProgressBar.GONE);
			Log.d(TAG, "Removed row now");
			// mAdapter.notifyDataSetInvalidated();
			// mAdapter.notifyDataSetChanged();
			getLoaderManager().restartLoader(0, null, EntryListActivity.this);
		}

	}

	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
			final MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.entry_list_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode,
				final Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(final ActionMode mode,
				final MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_delete:
				mProgressBar.setVisibility(ProgressBar.VISIBLE);
				new DeleteRunTask().execute(mDeleteId);
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {
			mListView.setItemChecked(mActionModeCheckedPos, false);
			mActionMode = null;
		}

	};

}
