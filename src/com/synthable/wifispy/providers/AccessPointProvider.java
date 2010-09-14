package com.synthable.wifispy.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.util.Log;

public class AccessPointProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://com.synthable.wifispy.providers.accesspointprovider");

	private static final String DATABASE_NAME = "WifiSpy.db";
    private static final int DATABASE_VERSION = 10; // 10 signals start of ContentProvider

	public static final class AccessPoints {
		public static final Uri CONTENT_URI = Uri.parse("content://com.synthable.wifispy.providers.accesspointprovider/accesspoints");

		public static final String DATABASE_TABLE = "access_points";

	    public static final String KEY_ID = "_id";
	    public static final int ID_COLUMN = 0;

	    public static final String KEY_SSID = "ssid";
	    public static final int SSID_COLUMN = 1;

	    public static final String KEY_BSSID = "bssid";
	    public static final int BSSID_COLUMN = 2;

	    public static final String KEY_CAPABILITIES = "capabilities";
	    public static final int CAPABILITIES_COLUMN = 3;

	    public static final String KEY_FREQUENCY = "frequency";
	    public static final int FREQUENCY_COLUMN = 4;

	    public static final String KEY_DBM = "dbm";
	    public static final int DBM_COLUMN = 5;

	    public static final String KEY_LAT = "latitude";
	    public static final int LAT_COLUMN = 6;

	    public static final String KEY_LONG = "longitude";
	    public static final int LONG_COLUMN = 7;

	    public static final String KEY_ALT = "altitude";
	    public static final int ALT_COLUMN = 8;

	    private static final String SCHEMA = "CREATE TABLE IF NOT EXISTS "
            + DATABASE_TABLE + "("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_SSID + " varchar(128) not null,"
            + KEY_BSSID + " varchar(128) not null,"
            + KEY_CAPABILITIES + " varchar(128) not null,"
            + KEY_FREQUENCY + " integer not null,"
            + KEY_DBM + " integer not null default 0,"
            + KEY_LAT + " double not null default 0,"
            + KEY_LONG + " double not null default 0,"
            + KEY_ALT + " double not null default 0"
            +");";
	}

	public static final class Profiles {
		public static final Uri CONTENT_URI = Uri.parse("content://com.synthable.wifispy.providers.accesspointprovider/profiles");

		public static final String DATABASE_TABLE = "profiles";

	    public static final String KEY_ID = "_id";
	    public static final int ID_COLUMN = 0;

	    public static final String KEY_NAME = "name";
	    public static final int NAME_COLUMN = 1;

	    private static final String SCHEMA = "CREATE TABLE IF NOT EXISTS "
            + DATABASE_TABLE + "("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_NAME + " varchar(128) not null"
            +");";
	}

	public static final class AccessPointProfile {
		public static final Uri CONTENT_URI = Uri.parse("content://com.synthable.wifispy.providers.accesspointprovider/accesspointprofile");

		public static final String DATABASE_TABLE = "accesspoint_profile";

	    public static final String KEY_ID = "_id";
	    public static final int ID_COLUMN = 0;

	    public static final String KEY_APID = "apid";
	    public static final int APID_COLUMN = 1;

	    public static final String KEY_PID = "pid";
	    public static final int PID_COLUMN = 2;

	    private static final String SCHEMA = "CREATE TABLE IF NOT EXISTS "
            + DATABASE_TABLE + "("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_APID + " integer not null,"
            + KEY_PID + " integer not null"
            +");";
	}

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
        //sURIMatcher.addURI("accesspoints", "/all", 0);
        sURIMatcher.addURI("accesspoints", "/#", AccessPoints.ID_COLUMN);
        sURIMatcher.addURI("accesspoints", "/ssid", AccessPoints.SSID_COLUMN);
        sURIMatcher.addURI("accesspoints", "/bssid", AccessPoints.BSSID_COLUMN);

        sURIMatcher.addURI("profiles", "/#", Profiles.ID_COLUMN);
        sURIMatcher.addURI("profiles", "/name", Profiles.NAME_COLUMN);

        sURIMatcher.addURI("accesspointprofile", "/pid/#", AccessPointProfile.PID_COLUMN);
        sURIMatcher.addURI("accesspointprofile", "/apid/#", AccessPointProfile.APID_COLUMN);
    }

    private SQLiteDatabase mDb;
    private DbHelper mDbHelper;

    private static class DbHelper extends SQLiteOpenHelper {
        
        public DbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        /**
         * Called when no database exists in disk and the helper class needs to
         * create a new one.
         */
        @Override
        public void onCreate(SQLiteDatabase _db) {
            Log.v("Creating AccessPoints table: ", AccessPoints.DATABASE_TABLE);
            _db.execSQL(AccessPoints.SCHEMA);

            Log.v("Creating Profiles table: ", Profiles.DATABASE_TABLE);
            _db.execSQL(Profiles.SCHEMA);

            Log.v("Creating AccessPointProfile table: ", AccessPointProfile.DATABASE_TABLE);
            _db.execSQL(AccessPointProfile.SCHEMA);
        }

        /**
         * Called when there is a database version mismatch meaning that the
         * version of the database on disk needs to be upgraded to the current
         * version.
         */
        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            Log.v("AccessPointsProvider", "Upgrading from version " + _oldVersion + " to " + _newVersion);

            if(_oldVersion > 10) {
            	_db.execSQL("ALTER TABLE "+ AccessPoints.DATABASE_TABLE +" ADD COLUMN "+ AccessPoints.KEY_ALT + " double not null default 0");
            }

            onCreate(_db);
        }
    }

	@Override
	public boolean onCreate() {
		mDbHelper = new DbHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		mDb = mDbHelper.getWritableDatabase();

		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Put your code here
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Put your code here
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		/*
		int match = sURIMatcher.match(uri);
		switch(match) {
			case STORES:
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				long id = db.insert(DATABASE_TABLE, null, values);
				if(id > 0) {
					Uri storeUri = ContentUris.withAppendedId(CONTENT_URI, id);
					getContext().getContentResolver().notifyChange(storeUri, null);
					return storeUri;
				} 

				throw new SQLException("Failed to insert row into " + uri);
		}
		*/

		return null;
	}

	/**
	 * @see android.content.ContentProvider#query(Uri,String[],String,String[],String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Put your code here
		return null;
	}

	/**
	 * @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Put your code here
		return 0;
	}
}
