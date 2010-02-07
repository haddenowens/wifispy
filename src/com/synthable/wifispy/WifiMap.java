package com.synthable.wifispy;

import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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

public class WifiMap extends MapActivity
{
	public static final String ACTION_VIEW_SINGLE = "com.synthable.wifispy.action.view_single";
	public static final String ACTION_VIEW_ALL = "com.synthable.wifispy.action.view_all";
	public static final String ACTION_VIEW_RADIUS = "com.synthable.wifispy.action.view_radius";

	MapView mapView;
	List<Overlay> mapOverlays;
	Drawable drawable;
	WifiApItemizedOverlay itemizedOverlay;
	MapController mController;
	AccessPointAdapter dbAdapter;
	AccessPoint ap;
	MyLocationOverlay mLocationOverlay;

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		dbAdapter = new AccessPointAdapter(this);
        dbAdapter.open();

		setContentView(R.layout.map);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_OVER);
		mController = (MapController) mapView.getController();
		mLocationOverlay = new MyLocationOverlay(WifiMap.this, mapView);

		mapOverlays = mapView.getOverlays();
		drawable = getResources().getDrawable(android.R.drawable.star_on);  //R.drawable.marker);
		itemizedOverlay = new WifiApItemizedOverlay(drawable);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		Intent i = getIntent();
        String action = i.getAction();

        mapOverlays.add(mLocationOverlay);
        mLocationOverlay.enableCompass();
        mLocationOverlay.enableMyLocation();

		if(action.equals(ACTION_VIEW_SINGLE)) {
			ap = dbAdapter.getRow(i.getIntExtra("id", 0));

			Double lat = ap.getLat() * 1E6;
	        Double lng = ap.getLong() * 1E6;

			GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
			OverlayItem overlayitem = new OverlayItem(point, "Access Point", "This is a snippit...\nSecond line of snippit...\nThird!");

			itemizedOverlay.addOverlay(overlayitem);
			mapOverlays.add(itemizedOverlay);

			mController.setCenter(point);
			mController.setZoom(20);
		} else if(action.equals(ACTION_VIEW_ALL)) {

			Cursor c = dbAdapter.getAll();
			int count = c.getCount();
			int columnIndex = c.getColumnIndex(AccessPointAdapter.KEY_ID);

			c.moveToFirst();
			for(int x = 0;x != count;x++) {
				ap = dbAdapter.getRow(c.getInt(columnIndex));
				Double lat = ap.getLat() * 1E6;
		        Double lng = ap.getLong() * 1E6;

				GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
				OverlayItem overlayitem = new OverlayItem(point, "Access Point", "This is a snippit...");

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);

				c.moveToNext();
			}
			c.close();
		}
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}