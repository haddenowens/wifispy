package com.synthable.wifispy.services;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.models.AccessPoint;

public class WifiSpyService extends Service {
    public static final String BROADCAST_FOUND_AP = "com.synthable.wifispy.foundAccessPoint";

    private WifiManager mWifi;
    private LocationManager mLocationManager;
    private String mProvider;
    private AccessPointAdapter mDbAdapter;

    public Location mLocation = null;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = mWifi.getScanResults();
            int count = results.size();

            Double Lat = getLocation().getLatitude();
            Double Long = getLocation().getLongitude();

            Cursor c = null;
            AccessPoint ap = new AccessPoint();
            int position = 0;

            for (int x = 1; x <= count; x++) {
                String ssid = results.get(position).SSID;
                String bssid = results.get(position).BSSID;
                String capabilities = results.get(position).capabilities;
                int frequency = results.get(position).frequency;
                int dbm = results.get(position).level;

                c = mDbAdapter.findRowByBssid(bssid);
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
                    ap = mDbAdapter.getRow(c.getInt(AccessPointAdapter.ID_COLUMN));

                    /**
                     * Only update the settings if the current dBm is stronger than the last
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

            sendBroadcast(new Intent(BROADCAST_FOUND_AP));
        }
    };

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class ServiceBinder extends Binder {
        public WifiSpyService getService() {
            return WifiSpyService.this;
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            setLocation(location);
        }

        public void onProviderDisabled(String provider) {
            setLocation(null);
        }

        public void onProviderEnabled(String provider) {
            
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private final IBinder mBinder = new ServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mDbAdapter = new AccessPointAdapter(this);
        mDbAdapter.open();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "WifiSpy Service is starting...", Toast.LENGTH_SHORT).show();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        // criteria.setCostAllowed(true);
        // criteria.setPowerRequirement(Criteria.POWER_LOW);

        mProvider = mLocationManager.getBestProvider(criteria, true);
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            mLocationManager.requestLocationUpdates(mProvider, 2000, 10, mLocationListener);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, mLocationListener);
        }

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        if (!mWifi.isWifiEnabled()) {
            mWifi.setWifiEnabled(true);
        }

        mWifi.startScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationManager.removeUpdates(mLocationListener);
        Toast.makeText(this, "WifiSpy Service is stopping...",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public WifiManager getWifi() {
        return mWifi;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        if (mLocation == null) {
            mLocation = mLocationManager.getLastKnownLocation(mProvider);
        }
        return mLocation;
    }

    public LocationManager getGps() {
        return mLocationManager;
    }

    public static int getChannel(int frequency) {
        switch (frequency) {
            case 2412:
                return 1;
            case 2417:
                return 2;
            case 2422:
                return 3;
            case 2427:
                return 4;
            case 2432:
                return 5;
            case 2437:
                return 6;
            case 2442:
                return 7;
            case 2447:
                return 8;
            case 2452:
                return 9;
            case 2457:
                return 10;
            case 2462:
                return 11;
            case 2467:
                return 12;
            case 2472:
                return 13;
            case 2484:
                return 14;
            default:
                return 0;
        }
    }
}