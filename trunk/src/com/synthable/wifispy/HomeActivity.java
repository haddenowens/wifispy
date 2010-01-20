package com.synthable.wifispy;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.models.AccessPoint;

public class HomeActivity extends ListActivity
{
	static final int DIALOG_CONFIRM_DELETE = 0;

	private int SERVICE_STATUS = 0;
	private int SELECTED_AP_ID = 0;
	
	static final int VIEW_AP = 0;
	static final int DELETE_AP = 1;

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
    				String capabilities = results.get(position).capabilities;
    				int frequency = results.get(position).frequency;
    				int dbm = results.get(position).level;
Log.v("onReceive()", ssid +" => "+ capabilities +" => "+ frequency +" => "+ dbm);

    				Cursor c = dbAdapter.findRowBySsid(ssid);
    				if(c.getCount() == 0) {
    					ap.setSsid(ssid);
    					ap.setCapabilities(capabilities);
    					ap.setFrequency(frequency);
    					ap.setDbm(dbm);

    					Log.v("New SSID: ", ssid);
        				dbAdapter.insert(ap);
    				} else {
    					/*ap = dbAdapter.getRow(c.getInt(AccessPointAdapter.ID_COLUMN));
    					if(ap.getDbm() < dbm) {
    						ap.setDbm(dbm);
    					}*/

    					//Log.v("Old SSID: ", ssid);
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

        String[] from = new String[] { AccessPointAdapter.KEY_ID, AccessPointAdapter.KEY_SSID, AccessPointAdapter.KEY_DBM }; 
		int[] to = new int[] { R.id.id, R.id.ssid, R.id.dbm };

		accessPoints = new SimpleCursorAdapter(
			HomeActivity.this,
			R.layout.home_row,
			cursor,
			from,
			to
		);
		accessPoints.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(columnIndex == 2) {
					TextView t = (TextView)view;
					int dbm = cursor.getInt(cursor.getColumnIndex(AccessPointAdapter.KEY_DBM));
					Log.v("setViewValue()", Integer.toString(dbm));
					t.setText(dbm +"dBm");

					return true;
				}
				return false;
			}
		});
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = (Cursor)getListAdapter().getItem(info.position);
		AccessPoint ap = dbAdapter.getRow(c.getInt(AccessPointAdapter.ID_COLUMN));

		menu.setHeaderTitle(ap.getSsid());
		menu.add(0, VIEW_AP, 0, "View");
		menu.add(0, DELETE_AP, 0, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();

		TextView t = (TextView)info.targetView.findViewById(R.id.ssid);
		String ssid = t.getText().toString();

		t = (TextView)info.targetView.findViewById(R.id.id);
		SELECTED_AP_ID = Integer.parseInt(t.getText().toString());
		
		switch (item.getItemId()) {
			case VIEW_AP:
				/*Intent intent = new Intent(
					Intent.ACTION_EDIT,
					Uri.parse("content://schedules/edit/"+ SELECTED_SCHEDULE_ID),
					ScheduleList.this,
					BackupScheduleAdd.class
				);
				intent.putExtra("location", LOCATION);
				startActivity(intent);*/
				return true;
			case DELETE_AP:
				showDialog(DIALOG_CONFIRM_DELETE);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	switch(id) {
    		case DIALOG_CONFIRM_DELETE:
    			builder.setCancelable(true)
					.setTitle("Confirm")
					.setMessage("Are you sure you want to delete that access point?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							dbAdapter.delete(SELECTED_AP_ID);
							
							/*BackupSchedule schedule = BackupScheduleAdapter.getRow(SELECTED_SCHEDULE_ID);

							AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
			            	Intent intent = new Intent(ScheduleList.this, BackupService.class);

			            	intent.setAction(BackupService.ACTION_BACKUP_SD);
			            	intent.setData(Uri.parse("content://schedules/"+ schedule.getId()));
			    			PendingIntent pIntent = PendingIntent.getService(ScheduleList.this, 0, intent, 0);

			    			Calendar cal = Calendar.getInstance();
			    			cal.set(
			    				cal.get(Calendar.YEAR),
			    				cal.get(Calendar.MONTH),
			    				cal.get(Calendar.DAY_OF_MONTH),
			    				schedule.getHour(),
			    				schedule.getMinute(),
			    				0
			    			);
			    			am.cancel(pIntent);

							BackupScheduleAdapter.delete(SELECTED_SCHEDULE_ID);
							c.requery();

							dialog.dismiss();*/

							cursor.requery();
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
    			return builder.create();
    		default:
    			return null;
    	}
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