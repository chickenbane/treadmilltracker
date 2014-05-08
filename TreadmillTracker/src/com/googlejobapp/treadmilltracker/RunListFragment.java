package com.googlejobapp.treadmilltracker;

import java.util.List;

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

		setListAdapter(mAdapter);
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

		return String.format("%d | %s  %d | %s  %s | %s (%d)",
				data.getAvgMinutes(), data.getAvgMiles(),
				data.getAggregrateMinutes(), data.getAggregateMiles(),
				data.getPace(), data.getMph(), data.getRuns());
	}

	private class ListSummaryTask extends AsyncTask<Cursor, Void, Void> {
		private String mThisWeek;
		private String mLastWeek;
		private String mTwoWeeksAgo;

		@Override
		protected Void doInBackground(final Cursor... params) {
			if (!(params[0] instanceof RunDataCursor)) {
				Log.e(TAG, "Expecting a RunDataCursor");
				return null;
			}
			final RunDataCursor cursor = (RunDataCursor) params[0];

			final List<String> weekList = cursor.getSortedWeekList();

			final int size = weekList.size();

			String thisWeek = null;
			String lastWeek = null;
			String twoWeek = null;

			if (size > 0) {
				thisWeek = weekList.get(size - 1);
				if (size > 1) {
					lastWeek = weekList.get(size - 2);

					if (size > 2) {
						twoWeek = weekList.get(size - 3);
					}
				}
			}

			mThisWeek = getWeekStats(cursor, thisWeek);
			mLastWeek = getWeekStats(cursor, lastWeek);
			mTwoWeeksAgo = getWeekStats(cursor, twoWeek);

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mThisWeekTextView.setText(mThisWeek);
			mLastWeekTextView.setText(mLastWeek);
			mStreakTextView.setText(mTwoWeeksAgo);
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
