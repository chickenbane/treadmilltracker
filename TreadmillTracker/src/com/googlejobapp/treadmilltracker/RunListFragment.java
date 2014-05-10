package com.googlejobapp.treadmilltracker;

import java.util.List;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RunListFragment extends Fragment implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "RunListFragment";

	private RunListAdapter mAdapter;
	private SQLiteOpenHelper mSqliteHelper;
	private TextView mThisWeekTextView;
	private TextView mLastWeekTextView;
	private TextView mTwoWeeksTextView;

	private ExpandableListView mExpandableListView;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View v = inflater
				.inflate(R.layout.list_content, container, false);
		mExpandableListView = (ExpandableListView) v
				.findViewById(android.R.id.list);
		return v;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new RunListAdapter(getActivity());
		getLoaderManager().initLoader(0, null, this);

		// final ExpandableListView listView = findViewById(android.R.id.list);
		// mExpandableListView = listView;

		final ExpandableListView listView = mExpandableListView;

		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		mSqliteHelper = RunDao.getInstance(getActivity());

		// final View weekAggregates =
		// getActivity().getLayoutInflater().inflate(
		// R.layout.run_list_stats, null);
		// listView.addHeaderView(weekAggregates, null, false);
		//
		// mThisWeekTextView = (TextView) weekAggregates
		// .findViewById(R.id.textViewThisWeek);
		// mLastWeekTextView = (TextView) weekAggregates
		// .findViewById(R.id.textViewLastWeek);
		// mTwoWeeksTextView = (TextView) weekAggregates
		// .findViewById(R.id.textViewTwoWeeksAgo);

		final View headerTitles = getActivity().getLayoutInflater().inflate(
				R.layout.run_list_header, null);
		listView.addHeaderView(headerTitles, null, false);

		listView.setAdapter(mAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return RunDao.createLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		// new ListSummaryTask().execute(data);
		mAdapter.setRunDataCursor((RunDataCursor) data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.setRunDataCursor(null);
	}

	// private static class RunListCursorAdapter extends ResourceCursorAdapter {
	//
	// public RunListCursorAdapter(final Context context) {
	// super(context, R.layout.run_list_row, null, 0);
	// }
	//
	// @Override
	// public void bindView(final View view, final Context context,
	// final Cursor cursor) {
	// if (!(cursor instanceof RunDataCursor)) {
	// Log.e(TAG, "Expecting a RunDataCursor");
	// return;
	// }
	// final RunData runData = ((RunDataCursor) cursor).getRunData();
	//
	// RunListRow row = (RunListRow) view.getTag();
	// if (row == null) {
	// row = new RunListRow();
	// row.tvMinutes = (TextView) view
	// .findViewById(R.id.textViewMinutes);
	// row.tvMiles = (TextView) view.findViewById(R.id.textViewMiles);
	// row.tvPace = (TextView) view.findViewById(R.id.textViewPace);
	// row.tvMph = (TextView) view.findViewById(R.id.textViewMph);
	// row.tvDate = (TextView) view.findViewById(R.id.textViewDate);
	// view.setTag(row);
	// }
	//
	// row.tvMinutes.setText("" + runData.getMinutes());
	// row.tvMiles.setText(runData.getMilesFormatted());
	// row.tvPace.setText(runData.getPace());
	// row.tvMph.setText(runData.getMph());
	//
	// final String date = DateUtils.formatDateTime(null,
	// runData.getStartTime(), DateUtils.FORMAT_SHOW_DATE);
	// row.tvDate.setText(date);
	// }
	// }
	//
	// private static class RunListRow {
	// TextView tvMinutes;
	// TextView tvMiles;
	// TextView tvPace;
	// TextView tvMph;
	// TextView tvDate;
	// }

	private static String getAggregateWeek(final RunDataCursor cursor,
			final int endDelta) {
		final List<String> weekList = cursor.getSortedWeekList();
		final int index = weekList.size() - endDelta;
		if (index < 0) {
			Log.d(TAG, "Not enough weeks in data set.");
			return "";
		}
		final String weekKey = weekList.get(index);
		final RunAggregate data = cursor.getAggregateWeek(weekKey);
		if (data == null) {
			Log.e(TAG, "Week in list but not week aggregate map?");
			return "";
		}
		return data.toString();
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

			mThisWeek = getAggregateWeek(cursor, 1);
			mLastWeek = getAggregateWeek(cursor, 2);
			mTwoWeeksAgo = getAggregateWeek(cursor, 3);

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mThisWeekTextView.setText(mThisWeek);
			mLastWeekTextView.setText(mLastWeek);
			mTwoWeeksTextView.setText(mTwoWeeksAgo);
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

	public ExpandableListView getListView() {
		// TODO Auto-generated method stub
		return mExpandableListView;
	}
}
