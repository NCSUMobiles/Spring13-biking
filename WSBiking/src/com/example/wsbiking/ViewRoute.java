package com.example.wsbiking;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class ViewRoute extends FragmentActivity {

	private static final float DEFAULTZOOM = 14.3f;
	private static final float ROUTEWIDTH = 10.0f;
	private static final int LATITUDE = 0;
	private static final int LONGITUDE = 1;

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;
	private UiSettings mapUI;

	private float totalDistance, avgSpeed;
	private String startTime, endtime;
	private Integer routeID;

	private DatabaseHandler dbHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_route);
		dbHandler = DatabaseHandler.getInstance(this);
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_route, menu);
		return true;
	}

	@Override
	public void onStop() {
		dbHandler.close();
		super.onStop();
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this FragmentActivity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the FragmentActivity may not have been completely destroyed during this
	 * process (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
				populateDetails();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void populateDetails() {
		// TODO Auto-generated method stub
		TextView txtVwRouteSpeed = (TextView) findViewById(R.id.txtVwRouteSpeed);
		TextView txtVwRouteDistance = (TextView) findViewById(R.id.txtVwRouteDistance);
		TextView txtVwStartDateTime = (TextView) findViewById(R.id.txtVwStartDateTime);
		TextView txtVwEndDateTime = (TextView) findViewById(R.id.txtVwEndDateTime);
		
		txtVwRouteSpeed.setText(this.avgSpeed + " mph");
		txtVwRouteDistance.setText(this.totalDistance + " miles");
		txtVwStartDateTime.setText(this.startTime);
		txtVwEndDateTime.setText(this.endtime);
		
		SlidingDrawer infoDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
		infoDrawer.open();
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {

		mapUI = mMap.getUiSettings();
		mapUI.setZoomControlsEnabled(false);

		Intent viewRouteActivity = getIntent();

		this.routeID = Integer.valueOf(viewRouteActivity
				.getStringExtra("routeID"));
		this.totalDistance = Float.valueOf(viewRouteActivity
				.getStringExtra("totalDistance"));
		this.avgSpeed = Float.valueOf(viewRouteActivity
				.getStringExtra("avgSpeed"));
		this.startTime = viewRouteActivity.getStringExtra("startTime");
		this.endtime = viewRouteActivity.getStringExtra("endTime");

		Cursor resultSet = dbHandler.getRoutePoints(this.routeID);

		if (resultSet != null && resultSet.moveToFirst()) {
			PolylineOptions rectOptions = new PolylineOptions().color(
					Color.BLUE).width(ROUTEWIDTH);

			LatLng routeStart = new LatLng(resultSet.getDouble(LATITUDE),
					resultSet.getDouble(LONGITUDE));

			LatLng routeEnd = null;

			while (!resultSet.isAfterLast()) {
				rectOptions.add(routeEnd = new LatLng(resultSet
						.getDouble(LATITUDE), resultSet.getDouble(LONGITUDE)));

				resultSet.moveToNext();
			}

			mMap.addPolyline(rectOptions);

			mMap.addMarker(new MarkerOptions()
					.position(routeStart)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.cycling))
					.title(this.startTime));

			mMap.addMarker(new MarkerOptions()
					.position(routeEnd)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.finish))
					.title(this.endtime));

			moveCamera(routeStart, DEFAULTZOOM);

			resultSet.close();
		} else {
			// TODO: some error, no route points for route
		}
	}

	/**
	 * Move the camera to the location specified
	 * 
	 * @param newLocation
	 */
	private void moveCamera(LatLng newLocation, float zoomLevel) {
		CameraPosition camPos = new CameraPosition.Builder()
				.target(newLocation).zoom(zoomLevel).build();

		CameraUpdate cameraUpdate = CameraUpdateFactory
				.newCameraPosition(camPos);

		mMap.animateCamera(cameraUpdate);
	}
}
