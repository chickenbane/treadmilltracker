package com.googlejobapp.treadmilltracker;

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

	private ExpandableListView mExpandableListView;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.run_list_fragment, container,
				false);
		mExpandableListView = (ExpandableListView) v
				.findViewById(R.id.expandableRunList);
		return v;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new RunListAdapter(getActivity());
		getLoaderManager().initLoader(0, null, this);

		mExpandableListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mSqliteHelper = RunDao.getInstance(getActivity());

		final View header = getActivity().getLayoutInflater().inflate(
				R.layout.run_list_row, null);
		header.setBackgroundColor(getResources().getColor(R.color.header_color));

		final TextView tvMin = (TextView) header
				.findViewById(R.id.textViewMinutes);
		tvMin.setText(R.string.label_duration);

		final TextView tvMiles = (TextView) header
				.findViewById(R.id.textViewMiles);
		tvMiles.setText(R.string.label_distance);

		final TextView tvPace = (TextView) header
				.findViewById(R.id.textViewPace);
		tvPace.setText(R.string.label_pace);

		final TextView tvMph = (TextView) header.findViewById(R.id.textViewMph);
		tvMph.setText(R.string.label_mph);

		final TextView tvDate = (TextView) header
				.findViewById(R.id.textViewDate);
		tvDate.setText(R.string.label_start_date);

		mExpandableListView.addHeaderView(header, null, false);

		mExpandableListView.setAdapter(mAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return RunDao.createLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mAdapter.swapCursor((RunDataCursor) data);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
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
		if (deleteId != -1) {
			new DeleteRunTask().execute(deleteId);
		}
	}

	public ExpandableListView getExpandableListView() {
		return mExpandableListView;
	}

	public long getDeleteId(final int groupPos, final int childPos) {
		if (childPos == -1) {
			return -1;
		}
		return mAdapter.getChildId(groupPos, childPos);
	}
}
