package com.googlejobapp.treadmilltracker;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class Db {

	private Db() {

	}

	// Entry -> Run
	// TODO add runDate
	public static final class Entry implements BaseColumns {
		public static final String TABLE_NAME = "treadmill";
		public static final String COLUMN_NAME_DURATION_MINS = "duration";
		public static final String COLUMN_NAME_START_TIME = "start";
		public static final String COLUMN_NAME_DISTANCE_MILES = "distance";
	}

	private static final String SQL_CREATE_ENTRY = "CREATE TABLE "
			+ Entry.TABLE_NAME + " (" + Entry._ID + " INTEGER PRIMARY KEY,"
			+ Entry.COLUMN_NAME_DURATION_MINS + " INTEGER,"
			+ Entry.COLUMN_NAME_START_TIME + " INTEGER,"
			+ Entry.COLUMN_NAME_DISTANCE_MILES + " TEXT);";

	public static ContentValues createContentValues(int duration,
			String distance, long startTime) {
		ContentValues values = new ContentValues();
		values.put(Entry.COLUMN_NAME_DURATION_MINS, duration);
		values.put(Entry.COLUMN_NAME_START_TIME, startTime);
		values.put(Entry.COLUMN_NAME_DISTANCE_MILES, distance);
		return values;
	}

	private static final String SQL_SORT_ORDER = Entry._ID + " ASC";
	public static final String[] QUERY_COLUMNS = { Entry._ID,
			Entry.COLUMN_NAME_DISTANCE_MILES };

	public static Cursor queryForEntryList(SQLiteDatabase db) {

		Cursor cursor = db.query(Entry.TABLE_NAME, QUERY_COLUMNS, null, null,
				null, null, SQL_SORT_ORDER);
		return cursor;
	}

	public static SQLiteOpenHelper createSQLiteOpenHelper(Context context) {
		return new DbHelper(context);
	}

	private static class DbHelper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "TreadmillTracker.db";

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Db.SQL_CREATE_ENTRY);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}

	}

	private static class SqliteCursorLoader extends AsyncTaskLoader<Cursor> {
		private final SQLiteOpenHelper mSqliteHelper;
		private Cursor mCursor;

		public SqliteCursorLoader(Context context) {
			super(context);
			mSqliteHelper = Db.createSQLiteOpenHelper(context);
		}

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
			return Db.queryForEntryList(db);
		}

		@Override
		public void deliverResult(Cursor cursor) {
			if (isReset()) {
				// An async query came in while the loader is stopped
				if (cursor != null) {
					cursor.close();
				}
				return;
			}
			Cursor oldCursor = mCursor;
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
		public void onCanceled(Cursor cursor) {
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

	public static Loader<Cursor> createLoader(Context context) {
		return new SqliteCursorLoader(context);
	}
}
