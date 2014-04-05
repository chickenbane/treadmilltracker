package com.googlejobapp.treadmilltracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EntryProvider extends ContentProvider {

	private SQLiteOpenHelper mSqliteHelper;

	@Override
	public boolean onCreate() {
		mSqliteHelper = Db.createSQLiteOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
		return Db.queryForEntryList(db);
	}

	private static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.com.googlejobapp.treadmilltracker.run";
	private static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.com.googlejobapp.treadmilltracker.run";

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
