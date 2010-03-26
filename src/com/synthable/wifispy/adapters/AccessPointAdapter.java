package com.synthable.wifispy.adapters;

import com.synthable.wifispy.models.AccessPoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class AccessPointAdapter
{
	private static final int DATABASE_VERSION = 6;

	private static final String DATABASE_NAME = "WifiSpy.db";
	private static final String DATABASE_TABLE = "access_points";

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

	private SQLiteDatabase mDb;
	private AccessPointDbHelper mDbHelper;

	private final Context mContext;

	public AccessPointAdapter(Context _context) {
		mContext = _context;
		mDbHelper = new AccessPointDbHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public AccessPointAdapter open() throws SQLException {
		try {
			mDb = mDbHelper.getWritableDatabase();
		} catch(SQLiteException e) {
			mDb = mDbHelper.getReadableDatabase();
		}
		return this;
	}

	public void close() {
		mDb.close();
	}

	public long insert(AccessPoint ap) {
		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_SSID, ap.getSsid());
		contentValues.put(KEY_BSSID, ap.getBssid());
		contentValues.put(KEY_CAPABILITIES, ap.getCapabilities());
		contentValues.put(KEY_FREQUENCY, ap.getFrequency());
		contentValues.put(KEY_DBM, ap.getDbm());
		contentValues.put(KEY_LAT, ap.getLat());
		contentValues.put(KEY_LONG, ap.getLong());

		return mDb.insert(DATABASE_TABLE, null, contentValues);
	}

	public int delete(int id) {
		return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null);
	}

	public Cursor getAll() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_SSID, KEY_BSSID, KEY_DBM, KEY_FREQUENCY, KEY_CAPABILITIES, KEY_LONG, KEY_LAT }, null, null, null, null, null);
	}

	public Cursor findRowBySsid(String ssid) {
		String where = KEY_SSID +" = '"+ ssid +"'";
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_SSID }, where, null, null, null, null);
	}
	
	public Cursor findRowByBssid(String bssid) {
		String where = KEY_BSSID +" = '"+ bssid +"'";
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_BSSID }, where, null, null, null, null);
	}

	public AccessPoint getRow(int id) {
		AccessPoint ap = new AccessPoint();

		String where = KEY_ID +" = "+ id;
		Cursor c = mDb.query(DATABASE_TABLE, null, where, null, null, null, null);
		c.moveToFirst();

		ap.setNew(false);
		ap.setId(c.getInt(ID_COLUMN));
		ap.setSsid(c.getString(SSID_COLUMN));
		ap.setBssid(c.getString(BSSID_COLUMN));
		ap.setCapabilities(c.getString(CAPABILITIES_COLUMN));
		ap.setFrequency(c.getInt(FREQUENCY_COLUMN));
		ap.setDbm(c.getInt(DBM_COLUMN));
		ap.setLat(c.getDouble(LAT_COLUMN));
		ap.setLong(c.getDouble(LONG_COLUMN));

		return ap;
	}

	public int update(AccessPoint ap) {
		String where = KEY_ID + "=" + ap.getId();

		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_ID, ap.getId());
		contentValues.put(KEY_SSID, ap.getSsid());
		contentValues.put(KEY_BSSID, ap.getBssid());
		contentValues.put(KEY_CAPABILITIES, ap.getCapabilities());
		contentValues.put(KEY_FREQUENCY, ap.getFrequency());
		contentValues.put(KEY_DBM, ap.getDbm());
		contentValues.put(KEY_LAT, ap.getLat());
		contentValues.put(KEY_LONG, ap.getLong());

		return mDb.update(DATABASE_TABLE, contentValues, where, null);
	}


	private static class AccessPointDbHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE =
			"CREATE TABLE "+ DATABASE_TABLE +"("
				+ KEY_ID +" integer primary key autoincrement,"
				+ KEY_SSID +" varchar(128) not null,"
				+ KEY_BSSID +" varchar(128) not null,"
				+ KEY_CAPABILITIES +" varchar(128) not null,"
				+ KEY_FREQUENCY +" integer not null,"
				+ KEY_DBM +" integer not null default 0,"
				+ KEY_LAT +" double not null default 0,"
				+ KEY_LONG +" double not null default 0"
			+");";

		public AccessPointDbHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		/**
		 * Called when no database exists in disk and the helper class needs to create a new one.
		 */
		@Override
		public void onCreate(SQLiteDatabase _db) {
			Log.v("database_create", DATABASE_CREATE);
			_db.execSQL(DATABASE_CREATE);
		}

		/**
		 *  Called when there is a database version mismatch meaning that the version of the
		 *  database on disk needs to be upgraded to the current version.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
			Log.v("TaskDBAdapter", "Upgrading from version "+ _oldVersion +" to "+ _newVersion +", which will destroy all old data");

			/**
			 *  Upgrade the existing database to conform to the new version.
			 *  Multiple previous versions can be handled by comparing _oldVersion and _newVersion values.
			 *  The simplest case is to drop the old table and create a new one.
			 */
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

			onCreate(_db);
		}
	}

	public static String getEncryptionMethods(String capabilities) {
		boolean wpa = false;
		String cyphers = "";

		if(capabilities.contains("WPA")) {
			wpa = true;
			cyphers = "WPA";
		}

		if(capabilities.contains("WPA2")) {
			/*if(wpa == true) {
				cyphers = "WPA/WPA2";
			} else {
				cyphers = "WPA2";
			}*/
			cyphers += "\nWPA2";
		}

		if(capabilities.contains("[WEP]")) {
			cyphers += "\nWEP";
		}

		return cyphers;
	}
}