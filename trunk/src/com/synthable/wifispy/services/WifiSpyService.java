package com.synthable.wifispy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WifiSpyService extends Service {
    private WifiManager mWifi;
    private LocationManager mLocationManager;
    private String mProvider;

    public Location mLocation = null;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class ServiceBinder extends Binder {
        public WifiSpyService getService() {
            return WifiSpyService.this;
        }
    }

    private LocationListener myLocationListener = new LocationListener() {
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
        mWifi.startScan();
        return mBinder;
    }

    @Override
    public void onCreate() {
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "WifiSpy Service is starting...",
                Toast.LENGTH_SHORT).show();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        // criteria.setCostAllowed(true);
        // criteria.setPowerRequirement(Criteria.POWER_LOW);

        mProvider = mLocationManager.getBestProvider(criteria, true);

        mLocationManager.requestLocationUpdates(mProvider, 2000, 10,
                myLocationListener);

        if (!mWifi.isWifiEnabled()) {
            mWifi.setWifiEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "WifiSpy Service is stopping...",
                Toast.LENGTH_SHORT).show();
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