package com.googlejobapp.treadmilltracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class RunListActivity extends Activity {
	private final static String TAG = "RunListActivity";

	private RunListFragment mRunListFragment;
	private ActionMode mActionMode;
	private int mCheckedPos;
	private long mDeleteId;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run_list_activity);

		mRunListFragment = (RunListFragment) getFragmentManager()
				.findFragmentById(R.id.fragmentRunList);

		final ExpandableListView listView = mRunListFragment
				.getExpandableListView();

		listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(final ExpandableListView parent,
					final View v, final int groupPosition,
					final int childPosition, final long id) {
				/*
				 * 1) Desired result: If the action mode is on and we get a
				 * click, turn off the action mode
				 * 
				 * 2) However, check if id for the click is the same as the
				 * deleteId - in that case, don't turn off action mode.
				 * 
				 * #2 is because once action mode is enabled, this callback will
				 * be immediately called, which would then immediately turn off
				 * action mode due to #1.
				 */
				Log.d(TAG, "child click gpos=" + groupPosition + " cpos="
						+ childPosition + " id=" + id + " mCheckedPos="
						+ mCheckedPos + " mDeleteId=" + mDeleteId);
				if (mActionMode != null && id != mDeleteId) {
					Log.d(TAG, "Disabling the ActionMode for child click.");
					mActionMode.finish();
				}
				return true;
			}
		});

		listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(final ExpandableListView parent,
					final View v, final int groupPosition, final long id) {
				Log.d(TAG, "group click gpos=" + groupPosition + " id=" + id
						+ " mCheckedPos=" + mCheckedPos + " mDeleteId="
						+ mDeleteId);
				if (mActionMode != null) {
					Log.d(TAG, "Disabling the ActionMode for group click");
					mActionMode.finish();
					return true;
				}
				return false;
			}
		});

		listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(final ContextMenu menu,
					final View v, final ContextMenuInfo menuInfo) {
				final ExpandableListView.ExpandableListContextMenuInfo elInfo = (ExpandableListContextMenuInfo) menuInfo;
				final int child = ExpandableListView
						.getPackedPositionChild(elInfo.packedPosition);
				final int group = ExpandableListView
						.getPackedPositionGroup(elInfo.packedPosition);
				final int type = ExpandableListView
						.getPackedPositionType(elInfo.packedPosition);
				Log.d(TAG, "onCreate packedPos=" + elInfo.packedPosition
						+ " group=" + group + " child=" + child + " type="
						+ type);

				if (mActionMode != null) {
					Log.d(TAG, "Disabling the ActionMode for long click");
					mActionMode.finish();
					return;
				}

				final long deleteId = mRunListFragment
						.getDeleteId(group, child);

				if (deleteId == -1) {
					// No menu for groups, only child
					return;
				}

				Log.d(TAG, "Enabling Action Mode");
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				mActionMode = RunListActivity.this
						.startActionMode(mActionModeCallback);
				mCheckedPos = listView
						.getFlatListPosition(elInfo.packedPosition);
				listView.setItemChecked(mCheckedPos, true);
				mDeleteId = deleteId;
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
			mRunListFragment.getExpandableListView().setItemChecked(
					mCheckedPos, false);
			mActionMode = null;
		}
	};

}
