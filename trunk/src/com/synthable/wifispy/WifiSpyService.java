package com.synthable.wifispy;

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
    	WifiSpyService getService() {
            return WifiSpyService.this;
        }
    }

    private Location location = null;
	private int t = 2000; //300000;
	private int d = 10;

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
		Log.v("Service.onCreate()", "in");
		wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

		gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = gps.getBestProvider(criteria, true);

		gps.requestLocationUpdates(provider, getMilliseconds(), getMeters(), myLocationListener);
		location = gps.getLastKnownLocation(provider);

		Log.v("Service.onStart()", Double.toString(location.getLatitude()));
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);

		Log.v("Service.onRebind()", "in");
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Log.v("Service.onUnbind()", "in");

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

	public int getMilliseconds() {
		return t;
	}

	public int getMeters() {
		return d;
	}
}