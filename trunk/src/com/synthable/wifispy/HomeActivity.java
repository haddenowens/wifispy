package com.synthable.wifispy;

import java.util.List;

import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.models.AccessPoint;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

public class HomeActivity extends ListActivity
{
	private int SERVICE_STATUS = 0;

    private WifiSpyService mBoundService = null;
    private SimpleCursorAdapter accessPoints;
    private AccessPointAdapter dbAdapter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(mBoundService != null) {
    			List<ScanResult> results = mBoundService.getWifi().getScanResults();
    			int count = results.size();

    			AccessPoint ap = new AccessPoint();
    			int position = 0;
    			for(int x = 1;x <= count;x++) 
    			{
    				String ssid = results.get(position).SSID;

    				Cursor c = dbAdapter.findRowBySsid(ssid);
    				if(c.getCount() == 0) {
    					ap.setSsid(ssid);
        				Log.v("New SSID: ", ssid);
        				dbAdapter.insert(ap);
    				} else {
    					Log.v("Old SSID: ", ssid);
    					//dbAdapter.update(ap);
    				}

        			position++;
    			}

    			cursor.requery();
			}
		}
    };

    private Button button;
    private Cursor cursor;

	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((WifiSpyService.ServiceBinder)service).getService();
            Log.v("onServiceConnected()", "in");
        }

        public void onServiceDisconnected(ComponentName className) {
        	Log.v("onServiceDisconnected()", "in");
            mBoundService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        dbAdapter = new AccessPointAdapter(this);
        dbAdapter.open();

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(receiver, filter);

        setContentView(R.layout.home);

        cursor = dbAdapter.getAll();
        startManagingCursor(cursor);

        String[] from = new String[] { AccessPointAdapter.KEY_SSID}; 
		int[] to = new int[] { R.id.TextView01 };

		accessPoints = new SimpleCursorAdapter(
			HomeActivity.this,
			R.layout.home_row,
			cursor,
			from,
			to
		);
		/*accessPoints.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(columnIndex == 2) {
					CheckBox cb = (CheckBox)view;
					int activated = cursor.getColumnIndexOrThrow(BackupScheduleAdapter.KEY_ACTIVE);
					if(cursor.getInt(activated) == 0) {
						cb.setChecked(false);
					} else {
						cb.setChecked(true);
					}
					return true;
				}
				return false;
			}
		});*/
		setListAdapter(accessPoints);
		registerForContextMenu(getListView());
    }

    @Override
	protected void onStart()
    {
		super.onStart();

		if(SERVICE_STATUS == 0) {
			if(mBoundService == null) {
				startService(new Intent(HomeActivity.this, WifiSpyService.class));
			}
			bindService(new Intent(HomeActivity.this, WifiSpyService.class), mConnection, Context.BIND_AUTO_CREATE);
			//button.setText(R.string.running);
			SERVICE_STATUS = 1;
		} else {
			stopService(new Intent(HomeActivity.this, WifiSpyService.class));
			unbindService(mConnection);
			//button.setText(R.string.stopped);
			SERVICE_STATUS = 0;
		}

		/*button = (Button)findViewById(R.id.control_button);
		if(mBoundService == null) {
			button.setText(R.string.stopped);
		} else {
			button.setText(R.string.running);
		}*/
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		unregisterReceiver(receiver);
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		Log.v("onNewIntent()", intent.getStringExtra("aaa"));
	}

	public void onClickListener(View target)
	{
    	switch(target.getId()) {
    		case R.id.control_button:
    			if(SERVICE_STATUS == 0) {
    				if(mBoundService == null) {
    					startService(new Intent(HomeActivity.this, WifiSpyService.class));
    				}
    				bindService(new Intent(HomeActivity.this, WifiSpyService.class), mConnection, Context.BIND_AUTO_CREATE);
    				//button.setText(R.string.running);
    				SERVICE_STATUS = 1;
    			} else {
    				stopService(new Intent(HomeActivity.this, WifiSpyService.class));
    				unbindService(mConnection);
    				//button.setText(R.string.stopped);
    				SERVICE_STATUS = 0;
    			}
    			break;
    		default:
    			break;
    	}
    }

	
}