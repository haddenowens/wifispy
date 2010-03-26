package com.synthable.wifispy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.adapters.AccessPointListAdapter;
import com.synthable.wifispy.models.AccessPoint;
import com.synthable.wifispy.services.WifiSpyService;

public class HomeActivity extends ListActivity {
    static final int DIALOG_CONFIRM_DELETE = 0;
    static final int DIALOG_EXPORTING = 1;
    static final int DIALOG_DONE_EXPORTING = 2;
    static final int DIALOG_GPS_DISABLED = 3;

    static final int TOGGLE_SERVICE = 0;
    static final int MAP_ALL_AP = 1;
    static final int EXPORT_KML = 2;
    static final int DETAILS_ALL = 3;

    static final int VIEW_AP = 0;
    static final int DELETE_AP = 1;
    static final int DETAILS_AP = 2;

    private int SERVICE_STATUS = 0;
    private int SELECTED_AP_ID = 0;

    static final String WIFISPY_SERVICE_CLASS = "com.synthable.wifispy.WifiSpyService";

    private WifiSpyService mBoundService = null;
    private Location mLocation;
    private LocationManager mLocationManager;
    private SimpleCursorAdapter mAccessPoints;
    private AccessPointAdapter mDbAdapter;
    private Cursor mCursor;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBoundService != null) {
                List<ScanResult> results = mBoundService.getWifi()
                        .getScanResults();
                int count = results.size();

                Double Lat = mBoundService.getLocation().getLatitude();
                Double Long = mBoundService.getLocation().getLongitude();

                AccessPoint ap = new AccessPoint();
                int position = 0;

                for (int x = 1; x <= count; x++) {
                    String ssid = results.get(position).SSID;
                    String bssid = results.get(position).BSSID;
                    String capabilities = results.get(position).capabilities;
                    int frequency = results.get(position).frequency;
                    int dbm = results.get(position).level;

                    Cursor c = mDbAdapter.findRowByBssid(bssid);
                    if (c.getCount() == 0) {
                        ap.setSsid(ssid);
                        ap.setBssid(bssid);
                        ap.setCapabilities(capabilities);
                        ap.setFrequency(frequency);
                        ap.setDbm(dbm);
                        ap.setLat(Lat);
                        ap.setLong(Long);

                        mDbAdapter.insert(ap);
                    } else {
                        c.moveToFirst();
                        ap = mDbAdapter.getRow(c
                                .getInt(AccessPointAdapter.ID_COLUMN));

                        /**
                         * <0 if current dbm is weaker than last logged dbm >0
                         * if current dbm is stronger than last logged dbm
                         */
                        if (WifiManager.compareSignalLevel(dbm, ap.getDbm()) > 0) {
                            ap.setDbm(dbm);
                            ap.setLat(Lat);
                            ap.setLong(Long);
                            mDbAdapter.update(ap);
                        }
                    }

                    position++;
                }

                mCursor.requery();
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((WifiSpyService.ServiceBinder) service)
                    .getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    class KmlExport extends AsyncTask<Void, Void, Void> {
        Cursor c;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            c = mCursor;
            c.moveToFirst();

            showDialog(DIALOG_EXPORTING);
        }

        @Override
        protected Void doInBackground(Void... nothing) {
            try {

                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root, "/wifispy/");
                dir.mkdirs();

                File file = new File(root, "/wifispy/"
                        + System.currentTimeMillis() + ".kml");
                FileWriter writer = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(writer);

                out
                        .write("<?xml version='1.0' encoding='UTF-8'?><kml xmlns='http://www.opengis.net/kml/2.2'><Document>");

                while (!c.isLast()) {
                    out.write("<Placemark>");
                    out
                            .write("<name><![CDATA["
                                    + c
                                            .getString(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_SSID))
                                    + "]]></name>");
                    out.write("<description><![CDATA[");
                    out
                            .write("<b>BSSID</b>: "
                                    + c
                                            .getString(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_BSSID))
                                    + "<br>");
                    out
                            .write("<b>Best dBm</b>: "
                                    + c
                                            .getInt(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_DBM))
                                    + "<br>");
                    out
                            .write("<b>Encryption Capabilities</b>: "
                                    + c
                                            .getString(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_CAPABILITIES))
                                    + "<br>");
                    out.write("]]></description>");
                    out
                            .write("<Point><coordinates>"
                                    + c
                                            .getFloat(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_LONG))
                                    + ","
                                    + c
                                            .getFloat(c
                                                    .getColumnIndex(AccessPointAdapter.KEY_LAT))
                                    + ",0</coordinates></Point>");
                    out.write("</Placemark>");

                    c.moveToNext();
                }
                out.write("</Document></kml>");
                out.close();

            } catch (IOException e) {
                Log.v("onCreate()", "Could not write file " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            dismissDialog(DIALOG_EXPORTING);
            showDialog(DIALOG_DONE_EXPORTING);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbAdapter = new AccessPointAdapter(this);
        mDbAdapter.open();

        mCursor = mDbAdapter.getAll();
        startManagingCursor(mCursor);

        String[] from = new String[] {};
        int[] to = new int[] {};

        mAccessPoints = new AccessPointListAdapter(HomeActivity.this,
                R.layout.home_row, mCursor, from, to);
        setContentView(R.layout.home);
        setListAdapter(mAccessPoints);
        registerForContextMenu(getListView());

        /**
         * Loop through the currently running service on the device and check
         * them against the WifiSpyService class.
         */
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am
                .getRunningServices(100); // Hopefully there are less than 100
        // services running

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (rsi.service.getClassName().equals(WIFISPY_SERVICE_CLASS)) {
                SERVICE_STATUS = 1;
            }
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Unregister the BroadcastReceiver if the Activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        mDbAdapter.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Integer id = (Integer) getListAdapter().getItem(info.position);
        AccessPoint ap = mDbAdapter.getRow((int) id);

        menu.setHeaderTitle(ap.getSsid());
        menu.add(0, DETAILS_AP, 0, "Details");
        menu.add(0, VIEW_AP, 1, "Map It");
        menu.add(0, DELETE_AP, 2, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        /**
         * Get the Access Point id from the hidden text view
         */
        TextView t = (TextView) info.targetView.findViewById(R.id.id);
        SELECTED_AP_ID = Integer.parseInt(t.getText().toString());

        switch (item.getItemId()) {
            case DETAILS_AP:
                return true;
            case VIEW_AP:
                Intent i = new Intent(this, WifiMap.class);
                i.setAction(WifiMap.ACTION_VIEW_SINGLE);
                i.putExtra("id", SELECTED_AP_ID);
                startActivity(i);
                return true;
            case DELETE_AP:
                showDialog(DIALOG_CONFIRM_DELETE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (SERVICE_STATUS == 1) {
            menu.add(0, TOGGLE_SERVICE, 0, "Stop Scanning").setIcon(
                    android.R.drawable.ic_menu_search);
        } else {
            menu.add(0, TOGGLE_SERVICE, 0, "Start Scanning").setIcon(
                    android.R.drawable.ic_menu_search);
        }

        menu.add(0, MAP_ALL_AP, 1, "Map All AP").setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(0, EXPORT_KML, 2, "Google Earth Export").setIcon(
                android.R.drawable.ic_menu_save);
        menu.add(0, DETAILS_ALL, 3, "AP Details");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TOGGLE_SERVICE:
                if (SERVICE_STATUS == 0) {
                    if (mLocationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                        showDialog(DIALOG_GPS_DISABLED);
                    } else {
                        startService();
                    }
                } else {
                    unbindService(mConnection);
                    stopService(new Intent(HomeActivity.this,
                            WifiSpyService.class));
                    mBoundService = null;
                    SERVICE_STATUS = 0;
                }
                return true;
            case MAP_ALL_AP:
                Intent i = new Intent(this, WifiMap.class);
                i.setAction(WifiMap.ACTION_VIEW_ALL);
                startActivity(i);
                return true;
            case EXPORT_KML:
                new KmlExport().execute();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                builder.setCancelable(true).setTitle("Confirm").setMessage(
                        "Are you sure you want to delete that access point?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        mDbAdapter.delete(SELECTED_AP_ID);
                                        mCursor.requery();
                                    }
                                }).setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.dismiss();
                                    }
                                });
                return builder.create();
            case DIALOG_EXPORTING:
                ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Exporting...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                return progressDialog;
            case DIALOG_DONE_EXPORTING:
                builder
                        .setCancelable(true)
                        .setTitle("Done exporting")
                        .setMessage(
                                "Done exporting your Access Point selections to a Google Earth KML file on your SD Card.")
                        .setNeutralButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.dismiss();
                                    }
                                });
                return builder.create();
            case DIALOG_GPS_DISABLED:
                builder
                        .setCancelable(false)
                        .setTitle("GPS signal not Found")
                        .setMessage(
                                "GPS is not enabled, and accuracy may be effected.")
                        .setNeutralButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        startService();
                                        dialog.dismiss();
                                    }
                                });
                /*
                 * .setPositiveButton("Yes", new
                 * DialogInterface.OnClickListener() { public void
                 * onClick(DialogInterface dialog, int which) { startService(new
                 * Intent(HomeActivity.this, WifiSpyService.class));
                 * bindService(new Intent(HomeActivity.this,
                 * WifiSpyService.class), mConnection,
                 * Context.BIND_AUTO_CREATE); SERVICE_STATUS = 1; } })
                 * .setNegativeButton("No", new
                 * DialogInterface.OnClickListener() { public void
                 * onClick(DialogInterface dialog, int which) {
                 * dialog.dismiss(); } })
                 */
                return builder.create();
            default:
                return null;
        }
    }

    private void startService() {
        startService(new Intent(HomeActivity.this, WifiSpyService.class));
        bindService(new Intent(HomeActivity.this, WifiSpyService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        SERVICE_STATUS = 1;
    }

    public void onClickListener(View target) {
        switch (target.getId()) {
            default:
                break;
        }
    }
}