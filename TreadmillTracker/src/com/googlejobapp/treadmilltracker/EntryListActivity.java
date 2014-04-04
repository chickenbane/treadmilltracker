package com.googlejobapp.treadmilltracker;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

public class EntryListActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = "EntryListActivity";

	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry_list);

		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		progressBar.setIndeterminate(true);
		getListView().setEmptyView(progressBar);

		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, null, Db.QUERY_COLUMNS,
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);

		// SQLiteOpenHelper sqliteHelper = Db
		// .createSQLiteOpenHelper(getApplicationContext());
		// SQLiteDatabase db = sqliteHelper.getReadableDatabase();
		// Cursor cursor = Db.queryForEntryList(db);

		// startManagingCursor(cursor);
		//
		// ListAdapter adapter = new
		// SimpleCursorAdapter(getApplicationContext(),
		// android.R.layout.simple_list_item_2, cursor, Db.QUERY_COLUMNS,
		// new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		//
		// setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry_list, menu);
		return true;
	}

	public void clickAdd(View view) {
		startActivity(new Intent(this, AddEntryActivity.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.v(TAG, "creating loader");
		return Db.createLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}
