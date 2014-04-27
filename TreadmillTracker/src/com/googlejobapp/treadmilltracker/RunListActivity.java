package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RunListActivity extends Activity {
	private final static String TAG = "RunListActivity";

	private RunListFragment mRunListFragment;
	private ActionMode mActionMode;
	private int mActionModeCheckedPos;
	private long mDeleteId;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run_list_activity);

		mRunListFragment = (RunListFragment) getFragmentManager()
				.findFragmentById(R.id.fragmentRunList);

		final ListView listView = mRunListFragment.getListView();
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				if (mActionMode != null) {
					return false;
				}

				mActionMode = RunListActivity.this
						.startActionMode(mActionModeCallback);
				listView.setItemChecked(position, true);
				mActionModeCheckedPos = position;
				mDeleteId = id;

				return true;
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				if (mActionMode != null) {
					mActionMode.finish();
				}
				listView.setItemChecked(position, false);
				mActionModeCheckedPos = position;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.run_list, menu);
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

	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
			final MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.run_list_actionmode, menu);
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
				Log.d(TAG, "Deleting rowId=" + mDeleteId);
				mRunListFragment.deleteRow(mDeleteId);
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {
			mRunListFragment.getListView().setItemChecked(
					mActionModeCheckedPos, false);
			mActionMode = null;
		}
	};

}
