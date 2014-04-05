package com.googlejobapp.treadmilltracker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RunSqlite {

	private RunSqlite() {

	}

	private static final String SQL_CREATE_ENTRY = "CREATE TABLE "
			+ TreadmillTracker.Run.TABLE_NAME + " (" + TreadmillTracker.Run._ID
			+ " INTEGER PRIMARY KEY,"
			+ TreadmillTracker.Run.COLUMN_NAME_DURATION_MINS + " INTEGER,"
			+ TreadmillTracker.Run.COLUMN_NAME_START_TIME + " INTEGER,"
			+ TreadmillTracker.Run.COLUMN_NAME_DISTANCE_MILES + " TEXT);";

	private static final String SQL_SORT_ORDER = TreadmillTracker.Run._ID
			+ " ASC";
	public static final String[] QUERY_COLUMNS = { TreadmillTracker.Run._ID,
			TreadmillTracker.Run.COLUMN_NAME_DISTANCE_MILES };

	public static Cursor queryForEntryList(final SQLiteDatabase db) {

		final Cursor cursor = db.query(TreadmillTracker.Run.TABLE_NAME,
				QUERY_COLUMNS, null, null, null, null, SQL_SORT_ORDER);
		return cursor;
	}

	public static SQLiteOpenHelper createSQLiteOpenHelper(final Context context) {
		return new SqliteRunHelper(context);
	}

	private static class SqliteRunHelper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "TreadmillTracker.db";

		public SqliteRunHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(RunSqlite.SQL_CREATE_ENTRY);

		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			// TODO Auto-generated method stub

		}

	}

	private static class SqliteCursorLoader extends AsyncTaskLoader<Cursor> {
		private final SQLiteOpenHelper mSqliteHelper;
		private Cursor mCursor;

		public SqliteCursorLoader(final Context context) {
			super(context);
			mSqliteHelper = RunSqlite.createSQLiteOpenHelper(context);
		}

		@Override
		public Cursor loadInBackground() {
			final SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
			return RunSqlite.queryForEntryList(db);
		}

		@Override
		public void deliverResult(final Cursor cursor) {
			if (isReset()) {
				// An async query came in while the loader is stopped
				if (cursor != null) {
					cursor.close();
				}
				return;
			}
			final Cursor oldCursor = mCursor;
			mCursor = cursor;

			if (isStarted()) {
				super.deliverResult(cursor);
			}

			if (oldCursor != null && oldCursor != cursor
					&& !oldCursor.isClosed()) {
				oldCursor.close();
			}
		}

		// Starts an asynchronous load of the contacts list data. When the
		// result is ready the callbacks will be called on the UI thread.
		// If a previous load has been completed and is still valid the
		// result may be passed to the callbacks immediately. //
		// Must be called from the UI thread

		@Override
		protected void onStartLoading() {
			if (mCursor != null) {
				deliverResult(mCursor);
			}
			if (takeContentChanged() || mCursor == null) {
				forceLoad();
			}
		}

		@Override
		protected void onStopLoading() {
			// Attempt to cancel the
			// current load task if possible.
			cancelLoad();
		}

		@Override
		public void onCanceled(final Cursor cursor) {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}

		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped onStopLoading();

			if (mCursor != null && !mCursor.isClosed()) {
				mCursor.close();
			}
			mCursor = null;
		}

	}

	public static Loader<Cursor> createLoader(final Context context) {
		return new SqliteCursorLoader(context);
	}
}
