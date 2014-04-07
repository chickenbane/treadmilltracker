package com.googlejobapp.treadmilltracker;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RunDao {

	private RunDao() {

	}

	private static final String SQL_CREATE_ENTRY = "CREATE TABLE "
			+ TreadmillTracker.Run.TABLE_NAME + " (" + TreadmillTracker.Run._ID
			+ " INTEGER PRIMARY KEY,"
			+ TreadmillTracker.Run.COLUMN_NAME_DURATION_MINS + " INTEGER,"
			+ TreadmillTracker.Run.COLUMN_NAME_START_TIME + " INTEGER,"
			+ TreadmillTracker.Run.COLUMN_NAME_DISTANCE_MILES + " TEXT);";

	private static final String SQL_DROP_ENTRY = "DROP TABLE IF EXISTS "
			+ TreadmillTracker.Run.TABLE_NAME;

	private static final String SQL_SORT_ORDER = TreadmillTracker.Run.COLUMN_NAME_START_TIME
			+ " DESC";
	public static final String[] QUERY_COLUMNS = {
			TreadmillTracker.Run.COLUMN_NAME_DISTANCE_MILES, // 0
			TreadmillTracker.Run.COLUMN_NAME_START_TIME, // 1
			TreadmillTracker.Run.COLUMN_NAME_DURATION_MINS, // 2
			TreadmillTracker.Run._ID, // 3
	};

	public static final int QUERY_COLUMN_DISTANCE_MILES = 0;
	public static final int QUERY_COLUMN_START_TIME = 1;
	public static final int QUERY_COLUMN_DURATION_MINS = 2;
	public static final int QUERY_COLUMN_ID = 3;

	public static Cursor queryForEntryList(final SQLiteDatabase db) {

		final Cursor cursor = db.query(TreadmillTracker.Run.TABLE_NAME,
				QUERY_COLUMNS, null, null, null, null, SQL_SORT_ORDER);
		return cursor;
	}

	public static RunData queryForRun(final SQLiteDatabase db, final long rowId) {
		final String where = TreadmillTracker.Run._ID + " = ?";
		final String[] wwhereArgs = new String[] { String.valueOf(rowId) };
		final Cursor cursor = db.query(TreadmillTracker.Run.TABLE_NAME,
				QUERY_COLUMNS, where, wwhereArgs, null, null, null);

		long startTime = 0;
		int minutes = 0;
		String distance = "";
		if (cursor.moveToFirst()) {
			startTime = cursor.getLong(QUERY_COLUMN_START_TIME);
			distance = cursor.getString(QUERY_COLUMN_DISTANCE_MILES);
			minutes = cursor.getInt(QUERY_COLUMN_DURATION_MINS);
		}
		cursor.close();
		return new RunData(startTime, minutes, distance);
	}

	public static long insertRun(final SQLiteDatabase db,
			final ContentValues contentValues) {
		return db.insert(TreadmillTracker.Run.TABLE_NAME, null, contentValues);
	}

	public static void deleteRun(final SQLiteDatabase db, final long rowId) {
		final String where = TreadmillTracker.Run._ID + " = ?";
		final String[] whereArgs = new String[] { String.valueOf(rowId) };
		db.delete(TreadmillTracker.Run.TABLE_NAME, where, whereArgs);
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
			db.execSQL(SQL_CREATE_ENTRY);

		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			db.execSQL(SQL_DROP_ENTRY);

			onCreate(db);
		}

	}

	private static class SqliteCursorLoader extends AsyncTaskLoader<Cursor> {
		private final SQLiteOpenHelper mSqliteHelper;
		private Cursor mCursor;

		public SqliteCursorLoader(final Context context) {
			super(context);
			mSqliteHelper = createSQLiteOpenHelper(context);
		}

		@Override
		public Cursor loadInBackground() {
			final SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
			return queryForEntryList(db);
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
