package com.synthable.wifispy;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.synthable.wifispy.adapters.AccessPointAdapter;
import com.synthable.wifispy.models.AccessPoint;

public class WifiMap extends MapActivity {
    public static final int DIALOG_LOADING = 0;

    public static final String ACTION_VIEW_SINGLE = "com.synthable.wifispy.action.view_single";
    public static final String ACTION_VIEW_ALL = "com.synthable.wifispy.action.view_all";
    public static final String ACTION_VIEW_RADIUS = "com.synthable.wifispy.action.view_radius";

    MapView mMapView;
    List<Overlay> mMapOverlays;
    Drawable mDrawable;
    WifiApItemizedOverlay mItemizedOverlay;
    MapController mController;
    AccessPointAdapter mDbAdapter;
    AccessPoint mAccessPoint;
    Cursor mCursor;
    MyLocationOverlay mLocationOverlay;
    ProgressDialog mProgressDialog;

    class LoadWifiAp extends AsyncTask<Void, Integer, Void> {
        Cursor c;
        WifiApItemizedOverlay iOverlay;
        List<Overlay> mOverlays;
        AccessPoint ap;
        int count = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            c = mCursor;
            c.moveToFirst();

            mOverlays = mMapOverlays;
            iOverlay = mItemizedOverlay;

            // showDialog(DIALOG_LOADING);
        }

        protected Void doInBackground(Void... nothing) {
            count = c.getCount();
            int columnIndex = c.getColumnIndex(AccessPointAdapter.KEY_ID);
            int num = 0;

            c.moveToFirst();
            for (int x = 0; x != count; x++) {
                ap = mDbAdapter.getRow(c.getInt(columnIndex));
                Double lat = ap.getLat() * 1E6;
                Double lng = ap.getLong() * 1E6;

                GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
                OverlayItem overlayitem = new OverlayItem(point,
                        "Access Point", "This is a snippit...");

                iOverlay.addOverlay(overlayitem);

                // publishProgress(x);

                c.moveToNext();
            }
            mOverlays.add(iOverlay);
            c.close();

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            // progressDialog.setProgress((progress[0] / count) * 100);

            super.onProgressUpdate((progress[0] / count) * 100);
        }

        protected void onPostExecute() {
            // removeDialog(DIALOG_LOADING);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mDbAdapter = new AccessPointAdapter(this);
        mDbAdapter.open();

        setContentView(R.layout.map);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_OVER);
        mController = (MapController) mMapView.getController();
        mLocationOverlay = new MyLocationOverlay(WifiMap.this, mMapView);

        mMapOverlays = mMapView.getOverlays();
        mDrawable = getResources().getDrawable(android.R.drawable.star_on); // R.drawable.marker);
        mItemizedOverlay = new WifiApItemizedOverlay(mDrawable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent i = getIntent();
        String action = i.getAction();

        mMapOverlays.add(mLocationOverlay);
        mLocationOverlay.enableCompass();
        mLocationOverlay.enableMyLocation();

        if (action.equals(ACTION_VIEW_SINGLE)) {
            mAccessPoint = mDbAdapter.getRow(i.getIntExtra("id", 0));

            Double lat = mAccessPoint.getLat() * 1E6;
            Double lng = mAccessPoint.getLong() * 1E6;

            GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
            OverlayItem overlayitem = new OverlayItem(point, "Access Point",
                    "This is a snippit...\nSecond line of snippit...\nThird!");

            mItemizedOverlay.addOverlay(overlayitem);
            mMapOverlays.add(mItemizedOverlay);

            mController.setCenter(point);
            mController.setZoom(20);
        } else if (action.equals(ACTION_VIEW_ALL)) {

            mCursor = mDbAdapter.getAll();

            /*
             * int count = c.getCount(); int columnIndex =
             * c.getColumnIndex(AccessPointAdapter.KEY_ID);
             * 
             * c.moveToFirst(); for(int x = 0;x != count;x++) { ap =
             * dbAdapter.getRow(c.getInt(columnIndex)); Double lat = ap.getLat()
             * * 1E6; Double lng = ap.getLong() * 1E6;
             * 
             * GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
             * OverlayItem overlayitem = new OverlayItem(point, "Access Point",
             * "This is a snippit...");
             * 
             * itemizedOverlay.addOverlay(overlayitem);
             * 
             * c.moveToNext(); } mapOverlays.add(itemizedOverlay); c.close();
             */

            new LoadWifiAp().execute();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_LOADING:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage("Loading...");
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}