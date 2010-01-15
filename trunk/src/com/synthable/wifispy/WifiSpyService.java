package com.synthable.wifispy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class WifiSpyService extends Service
{
	private Context context;
	private WifiManager wifi;

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
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		Log.v("Service.onStart()", "in");
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

	public void setContext(Context _context)
	{
		context = _context;
	}

	public void sendIntent()
	{
		Intent i = new Intent(WifiSpyService.this, HomeActivity.class);
		i.putExtra("aaa", "Testing!");
		i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//context.startActivity(i);
	}

	public WifiManager getWifi() {
		return wifi;
	}
}