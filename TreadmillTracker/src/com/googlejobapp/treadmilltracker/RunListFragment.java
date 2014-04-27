package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.util.Calendar;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class RunListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "RunListFragment";

	private RunListCursorAdapter mAdapter;
	private SQLiteOpenHelper mSqliteHelper;
	private TextView mThisWeekTextView;
	private TextView mLastWeekTextView;
	private TextView mStreakTextView;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new RunListCursorAdapter(getActivity());
		getLoaderManager().initLoader(0, null, this);
		setListAdapter(mAdapter);

		final ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		mSqliteHelper = RunDao.getInstance(getActivity());

		final View headerStats = getActivity().getLayoutInflater().inflate(
				R.layout.run_list_stats, null);
		listView.addHeaderView(headerStats, null, false);

		mThisWeekTextView = (TextView) headerStats
				.findViewById(R.id.textViewThisWeek);
		mLastWeekTextView = (TextView) headerStats
				.findViewById(R.id.textViewLastWeek);
		mStreakTextView = (TextView) headerStats
				.findViewById(R.id.textViewStreak);

		final View headerTitles = getActivity().getLayoutInflater().inflate(
				R.layout.run_list_header, null);
		listView.addHeaderView(headerTitles, null, false);

		new ListSummaryTask().execute();
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return RunDao.createLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		new ListSummaryTask().execute();
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private static class RunListCursorAdapter extends SimpleCursorAdapter {

		public RunListCursorAdapter(final Context context) {
			super(context, R.layout.run_list_row, null, RunDao.QUERY_COLUMNS,
					new int[] { R.id.textViewMinutes, R.id.textViewMiles,
							R.id.textViewPace, R.id.textViewDate }, 0);
		}

		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor) {
			final RunData runData = RunDao.createRunData(cursor);

			RunListRow row = (RunListRow) view.getTag();
			if (row == null) {
				row = new RunListRow();
				row.tvMinutes = (TextView) view
						.findViewById(R.id.textViewMinutes);
				row.tvMiles = (TextView) view.findViewById(R.id.textViewMiles);
				row.tvPace = (TextView) view.findViewById(R.id.textViewPace);
				row.tvMph = (TextView) view.findViewById(R.id.textViewMph);
				row.tvDate = (TextView) view.findViewById(R.id.textViewDate);
				view.setTag(row);
			}

			row.tvMinutes.setText("" + runData.getMinutes());
			row.tvMiles.setText(runData.getMilesFormatted());
			row.tvPace.setText(runData.getPace());
			row.tvMph.setText(runData.getMph());

			final String date = DateUtils.formatDateTime(null,
					runData.getStartTime(), DateUtils.FORMAT_SHOW_DATE);
			row.tvDate.setText(date);
		}
	}

	private static class RunListRow {
		TextView tvMinutes;
		TextView tvMiles;
		TextView tvPace;
		TextView tvMph;
		TextView tvDate;
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
			mWeekMiles = week.getMiles();
			final RunData lastWeek = RunDao.queryForSummary(db, twoWeeksAgo,
					weekAgo);
			mLastMinutes = lastWeek.getMinutes();
			mLastMiles = lastWeek.getMiles();

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
			Log.d(TAG, "Removing row=" + params[0]);
			RunDao.deleteRun(db, params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			Toast.makeText(getActivity(), "Deleted!", Toast.LENGTH_SHORT)
					.show();

			getLoaderManager().restartLoader(0, null, RunListFragment.this);
		}

	}

	public void deleteRow(final long deleteId) {
		new DeleteRunTask().execute(deleteId);
	}
}
