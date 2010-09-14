package com.synthable.wifispy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.synthable.wifispy.R;
import com.synthable.wifispy.services.WifiSpyService;

public class AccessPointListAdapter extends SimpleCursorAdapter {
    Cursor mCursor;
    Context mContext;
    CheckBox mCheckBox;
    TextView mSettingsTitle;
    TextView mSettingsDesc;
    TextView mCountdownText;

    public AccessPointListAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        mCursor = c;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);

        return mCursor
                .getInt(mCursor.getColumnIndex(AccessPointAdapter.KEY_ID));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout row;

        if (convertView == null) {
            row = new RelativeLayout(mContext);

            LayoutInflater vi;
            vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(R.layout.accesspointlist_row, row, true);
        } else {
            row = (RelativeLayout) convertView;
        }

        mCursor.moveToPosition(position);
        int dbm = mCursor.getInt(mCursor
                .getColumnIndex(AccessPointAdapter.KEY_DBM));
        ImageView image = (ImageView) row
                .findViewById(R.id.signalStrengthImage);

        int level = R.drawable.level_1;
        switch (WifiManager.calculateSignalLevel(dbm, 10)) {
            case 1:
                level = R.drawable.level_1;
                break;
            case 2:
                level = R.drawable.level_1;
                break;
            case 3:
                level = R.drawable.level_2;
                break;
            case 4:
                level = R.drawable.level_2;
                break;
            case 5:
                level = R.drawable.level_3;
                break;
            case 6:
                level = R.drawable.level_3;
                break;
            case 7:
                level = R.drawable.level_4;
                break;
            case 8:
                level = R.drawable.level_4;
                break;
            case 9:
                level = R.drawable.level_5;
                break;
            case 10:
                level = R.drawable.level_5;
                break;
            default:
                break;
        }
        image.setImageDrawable(mContext.getResources().getDrawable(level));

        TextView t = (TextView) row.findViewById(R.id.bestDbm);
        t.setText(dbm + "dBm");

        t = (TextView) row.findViewById(R.id.id);
        t.setText(String.valueOf((mCursor.getInt(mCursor
                .getColumnIndex(AccessPointAdapter.KEY_ID)))));

        TextView c = (TextView) row.findViewById(R.id.channel);
        int channel = WifiSpyService.getChannel(mCursor.getInt(mCursor
                .getColumnIndex(AccessPointAdapter.KEY_FREQUENCY)));
        c.setText("Ch. " + channel);

        TextView ssid = (TextView) row.findViewById(R.id.ssid);
        ssid.setText(mCursor.getString(mCursor
                .getColumnIndex(AccessPointAdapter.KEY_SSID)));

        TextView encryption = (TextView) row.findViewById(R.id.encryption);
        encryption.setText(AccessPointAdapter.getEncryptionMethods(mCursor
                .getString(mCursor
                        .getColumnIndex(AccessPointAdapter.KEY_CAPABILITIES))));

        return row;
    }
}