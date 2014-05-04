package com.googlejobapp.treadmilltracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Set;

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
			if (!(cursor instanceof RunDataCursor)) {
				Log.e(TAG, "Expecting a RunDataCursor");
				return;
			}
			final RunData runData = ((RunDataCursor) cursor).getRunData();

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

	private static String getWeekStats(final RunDataCursor cursor,
			final String week) {
		final RunAggregate data = cursor.getAggregateWeek(week);
		if (data == null) {
			return "";
		}

		return String.format("%d | %s  %s | %s (%d|%s|%.0f)",
				data.getAvgMinutes(), data.getAvgMilesFormatted(),
				data.getPace(), data.getMph(), data.getAggregrateMinutes(),
				data.getAggregateMilesFormatted(), data.getRuns());
	}

	private static String getAggregateStats(final RunDataCursor cursor) {

		final ArrayList<RunAggregate> list = new ArrayList<RunAggregate>();
		final Set<String> weeks = cursor.getWeeks();
		for (final String week : weeks) {
			list.add(RunDataCursor.createRunAggregate(cursor.getRunWeek(week)));
		}
		int minutes = 0;
		BigDecimal miles = BigDecimal.ZERO;
		int totalMinutes = 0;
		BigDecimal totalMiles = BigDecimal.ZERO;
		BigDecimal runs = BigDecimal.ZERO;
		int count = 0;
		for (final RunAggregate run : list) {
			minutes += run.getAvgMinutes();
			miles = miles.add(run.getAvgMiles());
			totalMinutes += run.getAggregrateMinutes();
			totalMiles = totalMiles.add(run.getAggregateMiles());
			runs = runs.add(run.getRuns());
			count++;
		}
		final BigDecimal bdCount = BigDecimal.valueOf(count);
		final RunAggregate data = new RunAggregate(minutes / count,
				miles.divide(bdCount, 1, RoundingMode.HALF_UP), runs.divide(
						bdCount, 1, RoundingMode.HALF_UP));

		return String.format("%d | %s  %s | %s (%d|%.1f|%.1f)",
				data.getAggregrateMinutes(), data.getAggregateMilesFormatted(),
				data.getPace(), data.getMph(), totalMinutes / count,
				totalMiles.divide(bdCount, 1, RoundingMode.HALF_UP),
				data.getRuns());
	}

	private class ListSummaryTask extends AsyncTask<Cursor, Void, Void> {
		private String mThisWeek;
		private String mLastWeek;
		private String mStreak;

		@Override
		protected Void doInBackground(final Cursor... params) {
			if (!(params[0] instanceof RunDataCursor)) {
				Log.e(TAG, "Expecting a RunDataCursor");
				return null;
			}
			final RunDataCursor cursor = (RunDataCursor) params[0];

			final String thisWeek = cursor.getUltimateWeek();
			final String lastWeek = cursor.getPenultimateWeek();

			mThisWeek = getWeekStats(cursor, thisWeek);
			mLastWeek = getWeekStats(cursor, lastWeek);
			mStreak = getAggregateStats(cursor);

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
