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
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_NAME = "WifiSpy.db";
	private static final String DATABASE_TABLE = "access_points";

	public static final String KEY_ID = "_id";
	public static final int ID_COLUMN = 0;

	public static final String KEY_SSID = "ssid";
	public static final int SSID_COLUMN = 1;

	public static final String KEY_CAPABILITIES = "capabilities";
	public static final int CAPABILITIES_COLUMN = 2;

	public static final String KEY_FREQUENCY = "frequency";
	public static final int FREQUENCY_COLUMN = 3;

	public static final String KEY_DBM = "dbm";
	public static final int DBM_COLUMN = 4;

	private SQLiteDatabase db;
	private AccessPointDbHelper dbHelper;

	private final Context context;

	public AccessPointAdapter(Context _context)
	{
		context = _context;
		dbHelper = new AccessPointDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public AccessPointAdapter open() throws SQLException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch(SQLiteException e) {
			db = dbHelper.getReadableDatabase();
		}
		return this;
	}

	public void close() {
		db.close();
	}

	public long insert(AccessPoint ap)
	{
		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_SSID, ap.getSsid());
		contentValues.put(KEY_CAPABILITIES, ap.getCapabilities());
		contentValues.put(KEY_FREQUENCY, ap.getFrequency());
		contentValues.put(KEY_DBM, ap.getDbm());

		Log.v("insetr()", Integer.toString(ap.getDbm()));

		return db.insert(DATABASE_TABLE, null, contentValues);
	}

	public int delete(int id) {
		return db.delete(DATABASE_TABLE, KEY_ID + "=" + id, null);
	}

	/*public Cursor getAllFromLocation(int location) {
		String where = KEY_LOCATION +" = "+ location;
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_ACTIVE }, where, null, null, null, null);
	}*/

	public Cursor getAll() {
		Cursor c = db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_SSID, KEY_DBM }, null, null, null, null, null);
		//c.moveToFirst();
		//Log.v("getAll()", c.getString(c.getColumnIndex(KEY_DBM)));

		//return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_SSID, KEY_DBM }, null, null, null, null, null);
		return c;
	}

	public Cursor findRowBySsid(String ssid) {
		String where = KEY_SSID +" = '"+ ssid +"'";
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_SSID }, where, null, null, null, null);
	}

	public AccessPoint getRow(int id)
	{
		AccessPoint ap = new AccessPoint();

		String where = KEY_ID +" = "+ id;
		Cursor c = db.query(DATABASE_TABLE, null, where, null, null, null, null);
		c.moveToFirst();

		ap.setNew(false);
		ap.setId(c.getInt(ID_COLUMN));
		ap.setSsid(c.getString(SSID_COLUMN));
		ap.setCapabilities(c.getString(CAPABILITIES_COLUMN));
		ap.setFrequency(c.getInt(FREQUENCY_COLUMN));
		ap.setDbm(c.getInt(DBM_COLUMN));

		Log.v("getRow().1", Integer.toString(c.getInt(DBM_COLUMN)));

		return ap;
	}

	public int update(AccessPoint ap) {
		String where = KEY_ID + "=" + ap.getId();

		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_ID, ap.getId());
		contentValues.put(KEY_SSID, ap.getSsid());
		contentValues.put(KEY_CAPABILITIES, ap.getCapabilities());
		contentValues.put(KEY_FREQUENCY, ap.getFrequency());
		contentValues.put(KEY_DBM, ap.getDbm());

		return db.update(DATABASE_TABLE, contentValues, where, null);
	}


	private static class AccessPointDbHelper extends SQLiteOpenHelper
	{
		// SQL Statement to create a new database.
		private static final String DATABASE_CREATE =
			"CREATE TABLE "+ DATABASE_TABLE +"("
				+ KEY_ID +" integer primary key autoincrement,"
				+ KEY_SSID +" varchar(128) not null,"
				+ KEY_CAPABILITIES +" varchar(128) not null,"
				+ KEY_FREQUENCY +" integer not null,"
				+ KEY_DBM +" integer not null"
			+");";

		public AccessPointDbHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
			Log.v("database_create", DATABASE_CREATE);
		}

		/**
		 * Called when no database exists in disk and the helper class needs to create a new one.
		 */
		@Override
		public void onCreate(SQLiteDatabase _db)
		{
			_db.execSQL(DATABASE_CREATE);
		}

		/**
		 *  Called when there is a database version mismatch meaning that the version of the
		 *  database on disk needs to be upgraded to the current version.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
		{
			// Log the version upgrade.
			Log.v("TaskDBAdapter", "Upgrading from version " + _oldVersion
					+ " to " + _newVersion
					+ ", which will destroy all old data");

			/**
			 *  Upgrade the existing database to conform to the new version.
			 *  Multiple previous versions can be handled by comparing _oldVersion and _newVersion values.
			 *  The simplest case is to drop the old table and create a new one.
			 */
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

			// Create a new one.
			onCreate(_db);
		}
	}
}