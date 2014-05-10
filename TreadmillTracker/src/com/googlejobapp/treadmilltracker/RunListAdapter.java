package com.googlejobapp.treadmilltracker;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class RunListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "RunListAdapter";

	private RunDataCursor mCursor;
	private final LayoutInflater mInflater;

	public RunListAdapter(final Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getGroupCount() {
		if (mCursor == null) {
			return 0;
		}
		return mCursor.getSortedWeekList().size();
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		final String weekKey = getGroup(groupPosition);
		return mCursor.getWeekRunData(weekKey).size();
	}

	@Override
	public String getGroup(final int groupPosition) {
		final List<String> sortedWeekList = mCursor.getSortedWeekList();
		final int index = sortedWeekList.size() - 1 - groupPosition;

		return sortedWeekList.get(index);

	}

	@Override
	public RunData getChild(final int groupPosition, final int childPosition) {
		final String week = getGroup(groupPosition);
		return mCursor.getWeekRunData(week).get(childPosition);
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return Long.parseLong(getGroup(groupPosition));
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return getChild(groupPosition, childPosition).getId();
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			final View convertView, final ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newView(parent);
		} else {
			v = convertView;
		}

		if (mCursor == null) {
			Log.e(TAG, "cursor invalid");
			throw new IllegalStateException(
					"this should only be called when the cursor is valid");
		}

		final String weekKey = getGroup(groupPosition);
		final RunAggregate week = mCursor.getAggregateWeek(weekKey);

		if (week == null) {
			throw new IllegalStateException("Don't know how to get groupPos="
					+ groupPosition);
		}

		final RunListRow row = (RunListRow) v.getTag();

		row.tvMinutes.setText("" + week.getAvgMinutes());
		row.tvMiles.setText(week.getAvgMiles());
		row.tvPace.setText(week.getPace());
		row.tvMph.setText(week.getMph());

		final String dateText = week.getAggregrateMinutes() + " | "
				+ week.getAggregateMiles() + " (" + week.getRuns() + ")";
		row.tvDate.setText(dateText);
		return v;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			final boolean isLastChild, final View convertView,
			final ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newView(parent);
		} else {
			v = convertView;
		}

		if (mCursor == null) {
			throw new IllegalStateException(
					"this should only be called when the cursor is valid");
		}

		final RunData runData = getChild(groupPosition, childPosition);

		if (runData == null) {
			throw new IllegalStateException("Don't know how to get groupPos="
					+ groupPosition + " childPos=" + childPosition);
		}

		final RunListRow row = (RunListRow) v.getTag();

		row.tvMinutes.setText("" + runData.getMinutes());
		row.tvMiles.setText(runData.getMilesFormatted());
		row.tvPace.setText(runData.getPace());
		row.tvMph.setText(runData.getMph());

		final String date = DateUtils.formatDateTime(null,
				runData.getStartTime(), DateUtils.FORMAT_SHOW_DATE);
		row.tvDate.setText(date);
		return v;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition,
			final int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void setRunDataCursor(final RunDataCursor cursor) {
		mCursor = cursor;
	}

	private View newView(final ViewGroup parent) {
		final View view = mInflater.inflate(R.layout.run_list_row, parent,
				false);
		final RunListRow row = new RunListRow();
		row.tvMinutes = (TextView) view.findViewById(R.id.textViewMinutes);
		row.tvMiles = (TextView) view.findViewById(R.id.textViewMiles);
		row.tvPace = (TextView) view.findViewById(R.id.textViewPace);
		row.tvMph = (TextView) view.findViewById(R.id.textViewMph);
		row.tvDate = (TextView) view.findViewById(R.id.textViewDate);
		view.setTag(row);
		return view;
	}

	private static class RunListRow {
		TextView tvMinutes;
		TextView tvMiles;
		TextView tvPace;
		TextView tvMph;
		TextView tvDate;
	}
}
