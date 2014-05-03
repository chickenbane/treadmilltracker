package com.googlejobapp.treadmilltracker;

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
import android.widget.ResourceCursorAdapter;
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
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return RunDao.createLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		new ListSummaryTask().execute(data);
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private static class RunListCursorAdapter extends ResourceCursorAdapter {

		public RunListCursorAdapter(final Context context) {
			super(context, R.layout.run_list_row, null, 0);
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

	private class ListSummaryTask extends AsyncTask<Cursor, Void, Void> {

		// private final static long WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000;

		private String mThisWeek;
		private String mLastWeek;
		private String mStreak;

		@Override
		protected Void doInBackground(final Cursor... params) {
			if (!(params[0] instanceof RunDataCursor)) {
				return null;
			}
			final RunDataCursor cursor = (RunDataCursor) params[0];

			final RunData thisWeek = cursor.getAggregateUltimateWeek();
			final RunData lastWeek = cursor.getAggregatePenultimateWeek();
			final RunData all = cursor.getAggregrateAll();

			if (thisWeek == null) {
				mThisWeek = "nothing this week";
			} else {
				mThisWeek = String.format("%d minutes, %s miles",
						thisWeek.getMinutes(), thisWeek.getMilesFormatted());
			}

			if (lastWeek == null) {
				mLastWeek = "nothing last week";
			} else {
				mLastWeek = String.format("%d minutes, %s miles last week",
						lastWeek.getMinutes(), lastWeek.getMilesFormatted());
			}

			if (all == null) {
				mStreak = "no aggregate";
			} else {
				mStreak = String.format("%d minutes, %s miles total",
						all.getMinutes(), all.getMilesFormatted());
			}

			//
			//
			// final SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
			// final long now = Calendar.getInstance().getTimeInMillis();
			// final long weekAgo = now - WEEK_MILLIS;
			// final long twoWeeksAgo = now - (2 * WEEK_MILLIS);
			// final RunData week = RunDao.queryForSummary(db, weekAgo, now);
			//
			// private int mWeekMinutes, mLastMinutes;
			// private BigDecimal mWeekMiles, mLastMiles;
			// private int mStreakDays;
			//
			// mWeekMinutes = week.getMinutes();
			// mWeekMiles = week.getMiles();
			// final RunData lastWeek = RunDao.queryForSummary(db, twoWeeksAgo,
			// weekAgo);
			// mLastMinutes = lastWeek.getMinutes();
			// mLastMiles = lastWeek.getMiles();
			//
			// mStreakDays = RunDao.queryForStreak(db, now);
			//
			// mThisWeek = String.format("%d minutes, %.1f miles", mWeekMinutes,
			// mWeekMiles);
			// mLastWeek = String.format(
			// "%d minutes, %.1f miles last week", mLastMinutes, mLastMiles);
			// mStreak = mStreakDays + " days in a row";
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mThisWeekTextView.setText(mThisWeek);
			mLastWeekTextView.setText(mLastWeek);
			mStreakTextView.setText(mStreak);
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
