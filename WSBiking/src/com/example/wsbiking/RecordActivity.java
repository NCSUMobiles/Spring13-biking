package com.example.wsbiking;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wsbiking.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;

import com.example.yweather.StoreInfo;
import com.example.yweather.WeatherProcessing;
import com.example.yweather.YahooWeatherInfoListener;
import android.graphics.Typeface;
import android.graphics.Color;

/**
 * TODO: Improve code structure(Remove redundancy, error checking, try catch,
 * optimization etc.)
 * 
 * @author Leon Dmello The main route recording activity. Tracks the route
 *         points when the user has started recording a route.
 * 
 */
public class RecordActivity extends FragmentActivity implements YahooWeatherInfoListener{

	/**
	 * Constants used in this file
	 */
	private static final float DEFAULTZOOM = 14.3f;
	private static final float METER_THRESHOLD = 160.934f;
	private static final float METERS_TO_MILES = 1609.34f;
	private static final CharSequence INITIAL_DISTANCE = "0 meters";
	private static final float ROUTEWIDTH = 10.0f;
	private static final int ROUTECOLOR = 0x7F0000FF;
	private static final String LOG_TAB = "Record activity";

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	private UiSettings mapUI;
	private Marker myLocationMarker = null, startMarker = null,
			endMarker = null;
	SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	private LocationManager locManager;
	private LocationListener locationListener, myLocationListener;
	private boolean myLocationListening = false, recordingListening = false;

	private boolean gps_enabled = false;

	private ArrayList<RoutePoint> routePoints;

	private float totalDistance;
	private Location lastLocation;
	private Date startTime;

	private DatabaseHandler dbHandler;
	private Resources resourceHandler;

	/**
	 * Call back from save route activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
			break;
		case Activity.RESULT_CANCELED:

			mMap.clear();
			myLocationMarker = null;
			
			Location lastKnown = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if(lastKnown != null)
				plotMyLocationMarker(DEFAULTZOOM, DEFAULTZOOM, lastKnown);
			else
				plotMyLocation(null);

			break;

		default:
			break;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		dbHandler = DatabaseHandler.getInstance(this);
		resourceHandler = getResources();
		setUpMapIfNeeded();
		TextView tv = (TextView) findViewById(R.id.textViewWeatherInfo);
	        tv.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_routes:
			Intent intent = new Intent(this, ViewRoutes.class);
			startActivity(intent);

			break;
		default:
			break;
		}

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
			}
		}
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
		routePoints = new ArrayList<RoutePoint>();

		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				locationChanged(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
				if (provider.equals(LocationManager.GPS_PROVIDER)) {
					initializePreRecording(true);
					routePoints.clear();
					disableRecordingRoute();
				}
			}
		};

		// Define a listener that responds to users location updates to plot his
		// location once
		myLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				trackMyLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			Log.e(LOG_TAB, ex.getMessage());
		}

		if (!gps_enabled) {
			disableRecordingRoute();
		}

		plotMyLocation(null);
	}

	/**
	 * Show users current location on map
	 */
	public void plotMyLocation(View view) {
		if (!recordingListening && !myLocationListening) {

			myLocationListening = true;

			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, myLocationListener);
		}
	}

	/**
	 * Location listener to track users current location
	 * 
	 * @param location
	 */
	public void trackMyLocation(Location currentLocation) {

		if (recordingListening) {
			myLocationListening = false;
			locManager.removeUpdates(myLocationListener);
		}

		if (myLocationListening && currentLocation != null) {

			myLocationListening = false;
			locManager.removeUpdates(myLocationListener);

			// Previous GPS location is already present
			plotMyLocationMarker(DEFAULTZOOM, DEFAULTZOOM, currentLocation);
		}
	}

	/**
	 * Location changed listener to store route points
	 * 
	 * @param currentLocation
	 */
	public void locationChanged(Location currentLocation) {
		if (currentLocation != null) {

			// Previous GPS location is already present
			plotMyLocationMarker(DEFAULTZOOM, mMap.getCameraPosition().zoom,
					currentLocation);

			if (lastLocation != null) {
				totalDistance += currentLocation.distanceTo(lastLocation);

				PolylineOptions rectOptions = new PolylineOptions()
						.add(new LatLng(lastLocation.getLatitude(),
								lastLocation.getLongitude()))
						.add(new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude()))
						.color(ROUTECOLOR).width(ROUTEWIDTH);

				mMap.addPolyline(rectOptions);

				TextView distance = (TextView) findViewById(R.id.tripDistance);
				distance.setText(formatTotalDistance());

			} else {
				startMarker = mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(currentLocation.getLatitude(),
										currentLocation.getLongitude()))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.cycling))
						.title(dateFormatter.format(startTime)));

				// Timer will start after first start point
				Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
				timer.start();
				timer.setBase(SystemClock.elapsedRealtime());
			}

			lastLocation = new Location(currentLocation);

			routePoints.add(new RoutePoint(currentLocation.getLatitude(),
					currentLocation.getLongitude()));
		}
	}

	/**
	 * Start recording user route
	 * 
	 * @param btnStart
	 */
	public void startRecording(View btnStart) {

		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			Log.e(LOG_TAB, ex.getMessage());
		}

		if (gps_enabled) {

			mapUI.setCompassEnabled(false);
			mMap.clear();

			if (myLocationMarker != null)
				myLocationMarker = null;

			startTime = new Date();
			Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
			TextView distance = (TextView) findViewById(R.id.tripDistance);
			ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
			ImageView btnMyLoc = (ImageView) findViewById(R.id.imgVwMyLoc);

			if (myLocationListening) {
				locManager.removeUpdates(myLocationListener);
				myLocationListening = false;
			}

			recordingListening = true;

			// Get last known location and move camera
			Location lastKnown = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (lastKnown != null) {

				LatLng lastKnownLatLng = new LatLng(lastKnown.getLatitude(),
						lastKnown.getLongitude());

				myLocationMarker = mMap.addMarker(new MarkerOptions()
						.anchor(0.5f, 0.5f)
						.position(lastKnownLatLng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.mylocation)));

				startMarker = mMap.addMarker(new MarkerOptions()
						.position(lastKnownLatLng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.cycling))
						.title(dateFormatter.format(startTime)));

				lastLocation = new Location(lastKnown);

				routePoints.add(new RoutePoint(lastLocation.getLatitude(),
						lastLocation.getLongitude()));

				moveCamera(lastKnown, DEFAULTZOOM);

				// Timer will only start after first start point
				timer.start();
				timer.setBase(SystemClock.elapsedRealtime());
			} else {
				lastLocation = null;
				showToast("Timer will start after detecting first GPS location");
			}

			// Register location listener to get periodic updates
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					resourceHandler.getInteger(R.integer.time_interval),
					resourceHandler.getInteger(R.integer.min_distance),
					locationListener);

			// TODO: Initialize all route recording parameters properly
			totalDistance = 0;

			timer.setVisibility(View.VISIBLE);

			distance.setText(INITIAL_DISTANCE);
			distance.setVisibility(View.VISIBLE);

			btnStart.setVisibility(View.INVISIBLE);
			btnStop.setVisibility(View.VISIBLE);
			btnMyLoc.setVisibility(View.INVISIBLE);
		} else {
			showToast("Please enable GPS to record");
		}
	}

	/**
	 * Stop recording the route
	 * 
	 * @param btnStop
	 */
	public void stopRecording(View btnStop) {

		initializePreRecording(false);

		if (routePoints.size() <= 1) {

			startMarker.remove();

			Toast toast = Toast.makeText(getApplicationContext(),
					resourceHandler
							.getString(R.string.message_single_route_point),
					Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();

		} else {

			Date endTime = new Date();
			SimpleDateFormat dateFormatter = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");

			endMarker = mMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(lastLocation.getLatitude(), lastLocation
									.getLongitude()))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.finish))
					.title(dateFormatter.format(endTime)));

			Intent intent = new Intent(this, RouteSave.class);
			intent.putParcelableArrayListExtra("routePoints", routePoints);
			intent.putExtra("totalDistance", Float
					.parseFloat(new DecimalFormat("#.##")
							.format(convertDistanceToMiles())));
			intent.putExtra("startTime", dateFormatter.format(startTime));
			intent.putExtra("endTime", dateFormatter.format(endTime));
			intent.putExtra("avgSpeed", calculateSpeed(endTime));

			startActivityForResult(intent, 1);
		}

		routePoints.clear();
	}

	/**
	 * Re-initialize to start recording state
	 * 
	 * @param providerDisabled
	 */
	private void initializePreRecording(Boolean providerDisabled) {

		if (providerDisabled)
			mMap.clear();

		mapUI.setCompassEnabled(true);

		Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
		timer.stop();
		timer.setVisibility(View.INVISIBLE);

		TextView distance = (TextView) findViewById(R.id.tripDistance);
		distance.setVisibility(View.INVISIBLE);

		recordingListening = false;
		locManager.removeUpdates(locationListener);

		ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
		btnStop.setVisibility(View.INVISIBLE);

		ImageView btnStart = (ImageView) findViewById(R.id.start_button);
		btnStart.setVisibility(View.VISIBLE);

		ImageView btnMyLoc = (ImageView) findViewById(R.id.imgVwMyLoc);
		btnMyLoc.setVisibility(View.VISIBLE);
	}

	/**
	 * Plots or arranges pin of my location marker
	 * 
	 * @param nullZoom
	 * @param notNullZoom
	 * @param currentLocation
	 */
	private void plotMyLocationMarker(float nullZoom, float notNullZoom,
			Location currentLocation) {
		if (myLocationMarker != null) {
			myLocationMarker.setPosition(new LatLng(currentLocation
					.getLatitude(), currentLocation.getLongitude()));
			moveCamera(currentLocation, notNullZoom);
		} else {
			myLocationMarker = mMap.addMarker(new MarkerOptions()
					.anchor(0.5f, 0.5f)
					.position(
							new LatLng(currentLocation.getLatitude(),
									currentLocation.getLongitude()))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.mylocation)));
			moveCamera(currentLocation, nullZoom);
		}
	}

	/**
	 * Returns average speed in terms of miler per hour
	 * 
	 * @param endTime
	 * @return
	 */
	private float calculateSpeed(Date endTime) {

		float distanceInMiles = convertDistanceToMiles();
		long difference = endTime.getTime() - startTime.getTime();
		float timeInHours = (float) difference / (float) (1000 * 60 * 60);

		return Float.parseFloat(new DecimalFormat("#.##")
				.format(distanceInMiles / timeInHours));
	}

	/**
	 * Move the camera to the location specified
	 * 
	 * @param newLocation
	 */
	private void moveCamera(Location newLocation, float zoomLevel) {
		CameraPosition camPos = new CameraPosition.Builder()
				.target(new LatLng(newLocation.getLatitude(), newLocation
						.getLongitude())).zoom(zoomLevel).build();

		CameraUpdate cameraUpdate = CameraUpdateFactory
				.newCameraPosition(camPos);

		mMap.animateCamera(cameraUpdate);
	}

	/**
	 * generic method to display toast
	 * 
	 * @param message
	 */
	private void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);

		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	/**
	 * Format distance in decimal meters to rounded meters or miles *
	 * 
	 * @return
	 */
	private String formatTotalDistance() {
		if (totalDistance <= METER_THRESHOLD) {
			return String.valueOf(Math.round(totalDistance) + " meters");
		} else {
			return String.valueOf(Float.parseFloat(new DecimalFormat("#.##")
					.format(convertDistanceToMiles())) + " miles");
		}
	}

	/**
	 * Converts the meters distance into miles
	 * 
	 * @return
	 */
	private float convertDistanceToMiles() {
		float tempDistance = totalDistance, calculatedDistance;
		calculatedDistance = (float) Math.floor(tempDistance / METERS_TO_MILES);
		tempDistance %= METERS_TO_MILES;
		calculatedDistance += tempDistance / METERS_TO_MILES;
		return calculatedDistance;
	}

	/**
	 * Redirect user to enable GPS if disabled
	 */
	private void disableRecordingRoute() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage("GPS is disabled in your device. Enable it?")
				.setCancelable(false)
				.setPositiveButton("Enable GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showToast("Please enable GPS to record");
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

		@Override
	public void gotWeatherInfo(StoreInfo storeInfo) {

        if(storeInfo != null) {     
        	TextView tv = (TextView) findViewById(R.id.textViewWeatherInfo);         	
        	tv.setTextColor(Color.WHITE);
        	Typeface tf = Typeface.createFromAsset(getAssets(),"font/Days.otf");
        	tv.setTypeface(tf);        
             
			tv.setText( storeInfo.getCity() + ", "
					+ storeInfo.getCountry() + "\n\n"
					+ "Current Weather: " + storeInfo.getTemperature() + " F" + "\n"
					+ "Weather Condition : " + storeInfo.getmCurrentText() + "\t\t\t"
					+ "Humidity: " + storeInfo.getHumidity()					
					 );		
        } 
        
	}

	public void displayWeather(View v){
		TextView tv = (TextView) findViewById(R.id.textViewWeatherInfo);
    	if(tv.isShown()){
    		tv.setVisibility(View.INVISIBLE);
    	}
    	else{
    		String latlong[]={"35.7719", "-78.6389"};  
    		tv.setVisibility(View.VISIBLE);
    		tv.setText("Loading..");
    		WeatherProcessing weatherProcessor = new WeatherProcessing();
    		weatherProcessor.queryYahooWeather(getApplicationContext(), latlong, this);   
    	}
	}
}
