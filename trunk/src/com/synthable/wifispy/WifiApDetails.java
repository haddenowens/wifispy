package com.synthable.wifispy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.services.WifiSpyService;

public class WifiApDetails extends ListActivity {
    static final int DIALOG_CONFIRM_DELETE = 0;
    static final int DIALOG_EXPORTING = 1;
    static final int DIALOG_DONE_EXPORTING = 2;

    static final int TOGGLE_SERVICE = 0;
    static final int MAP_ALL_AP = 1;
    static final int EXPORT_KML = 2;

    static final int VIEW_AP = 0;
    static final int DELETE_AP = 1;
    static final int DETAILS_AP = 2;

    private int SELECTED_AP_ID = 0;

    static final String WIFISPY_SERVICE_CLASS = "com.synthable.wifispy.WifiSpyService";

    public WifiSpyService mBoundService = null;
    private SimpleCursorAdapter mAccessPoints;
    private AccessPointAdapter mDbAdapter;
    private Cursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbAdapter = new AccessPointAdapter(this);
        mDbAdapter.open();

        setContentView(R.layout.home);

        mCursor = mDbAdapter.getAll();
        startManagingCursor(mCursor);

        String[] from = new String[] {
                AccessPointAdapter.KEY_ID, AccessPointAdapter.KEY_SSID,
                AccessPointAdapter.KEY_DBM, AccessPointAdapter.KEY_FREQUENCY,
                AccessPointAdapter.KEY_CAPABILITIES
        };
        int[] to = new int[] {
                R.id.id, R.id.ssid, R.id.bestDbm, R.id.channel, R.id.encryption
        };

        mAccessPoints = new SimpleCursorAdapter(WifiApDetails.this,
                R.layout.home_row, mCursor, from, to);
        mAccessPoints.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor,
                    int columnIndex) {
                switch (columnIndex) {
                    case 3:
                        TextView t = (TextView) view;
                        int dbm = cursor.getInt(cursor
                                .getColumnIndex(AccessPointAdapter.KEY_DBM));
                        t.setText(dbm + "dBm");
                        return true;
                    case 4:
                        TextView c = (TextView) view;
                        int channel = WifiSpyService
                                .getChannel(cursor
                                        .getInt(cursor
                                                .getColumnIndex(AccessPointAdapter.KEY_FREQUENCY)));
                        c.setText("Ch. " + channel);
                        return true;
                    default:
                        return false;
                }
            }
        });
        setListAdapter(mAccessPoints);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDbAdapter.close();
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
            default:
                return null;
        }
    }

    public void onClickListener(View target) {
        switch (target.getId()) {
            default:
                break;
        }
    }
}