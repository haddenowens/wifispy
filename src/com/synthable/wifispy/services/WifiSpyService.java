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

public class WifiSpyService extends Service
{
	private WifiManager wifi;
	private LocationManager gps;

	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class ServiceBinder extends Binder {
    	public WifiSpyService getService() {
            return WifiSpyService.this;
        }
    }

    private Location location = null;

	private LocationListener myLocationListener = new LocationListener()
	{
		public void onLocationChanged(Location location) {
			Log.v("LocationListener()", "onLocationChanged()");
			setLocation(location);
		}

		public void onProviderDisabled(String provider){
			Log.v("LocationListener()", "onProviderDisabled()");
			setLocation(null);
		}

		public void onProviderEnabled(String provider){ }
		public void onStatusChanged(String provider, int status, Bundle extras){ }
	};

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new ServiceBinder();

	@Override
	public IBinder onBind(Intent intent)
	{
		wifi.startScan();
		return mBinder;
	}

	@Override
	public void onCreate()
	{
//Log.v("Service.onCreate()", "in");
		wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

		gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		Toast.makeText(this, "WifiSpy Service is starting...", Toast.LENGTH_SHORT).show();

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = gps.getBestProvider(criteria, true);

		gps.requestLocationUpdates(provider, 2000, 10, myLocationListener);
		location = gps.getLastKnownLocation(provider);

		if(!wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		Toast.makeText(this, "WifiSpy Service is stopping...", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);

//Log.v("Service.onRebind()", "in");
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
//Log.v("Service.onUnbind()", "in");

		return super.onUnbind(intent);
	}

	public WifiManager getWifi() {
		return wifi;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public static int getChannel(int frequency)
	{
		switch(frequency) {
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