package com.example.wsbiking;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.StoreInfo;
import com.example.weather.WeatherAdapter;
import com.example.weather.WeatherProcessing;
import com.example.weather.WeatherInfoListener;
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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * TODO: Improve code structure(Remove redundancy, error checking, try catch,
 * optimization etc.)
 * 
 * @author Leon Dmello The main route recording activity. Tracks the route
 *         points when the user has started recording a route.
 * 
 */
public class RecordActivity extends FragmentActivity implements
		WeatherInfoListener {

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
	private static final int SECS = 5;

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

	private EditText LoggedUser;
	private Session session;

	// Handler to Hide Weather popup after some seconds
	final Handler handler = new Handler();
	final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(runnable);
			displayWeather();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		dbHandler = DatabaseHandler.getInstance(this);
		resourceHandler = getResources();

		setUpMapIfNeeded();

		// LoggedUser = (EditText) findViewById(R.id.LoggedUser);
		// if (Main.isLogin) {
		// LoggedUser.setText(Main.logged_user);
		// } else {
		// session = Session.getActiveSession();
		// setName(session);
		// }
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
		case R.id.logout_item:
			if (Main.isLogin) {
				Main.isLogin = false;
				SharedPreferences sp = getSharedPreferences("logindetails", 0);
				SharedPreferences.Editor spedit = sp.edit();
				spedit.clear();
				spedit.commit();
			}
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
				Log.i("face", "in isClosed");
				session.closeAndClearTokenInformation();
			}
			Intent intent1 = new Intent(this, Main.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent1);
			break;
		case R.id.weather:
			displayWeather();
			break;
		case R.id.heatmap:
			Intent heatmapIntent = new Intent(this, Heatmap.class);
			startActivity(heatmapIntent);
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

	/* Weather functions START */

	@Override
	public void gotWeatherInfo(StoreInfo storeInfo) {

		ArrayList<StoreInfo> weather = new ArrayList<StoreInfo>();
		if (storeInfo != null) {

			handler.removeCallbacks(runnable);

			for (int i = 0; i < 5; i++) {
				StoreInfo temp = new StoreInfo();
				temp.setCondition(storeInfo.getCondition(i), 0);
				temp.setTemp(storeInfo.getTemp(i), 0);
				temp.setImg(storeInfo.getImg(i), 0);
				temp.setTime(storeInfo.getTime(i), 0);
				weather.add(temp);
			}

			WeatherAdapter adapter = new WeatherAdapter(this,
					R.layout.singleweather, weather);
			ListView weatherList = (ListView) findViewById(R.id.weatherList);
			weatherList.setAdapter(adapter);

			ProgressBar loadingWeather = (ProgressBar) findViewById(R.id.progressBarWeather);
			loadingWeather.setVisibility(View.INVISIBLE);

			weatherList.setVisibility(View.VISIBLE);

			handler.postDelayed(runnable, SECS * 1000);
		} else {

		}

	}

	public void displayWeather() {
		ListView weatherList = (ListView) findViewById(R.id.weatherList);
		ProgressBar loadingWeather = (ProgressBar) findViewById(R.id.progressBarWeather);

		if (loadingWeather.getVisibility() == View.INVISIBLE) {
			if (weatherList.isShown()) {
				weatherList.setVisibility(View.INVISIBLE);		
			} else {
				String latlong[] = { "35.7719", "-78.6389" };

				loadingWeather.setVisibility(View.VISIBLE);
				WeatherProcessing weatherProcessor = new WeatherProcessing();
				weatherProcessor.queryYahooWeather(getApplicationContext(),
						latlong, this);
			}
		}
	}

	/* Weather functions END */

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
			// My location marker already exists, just move it

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
		} else if (myLocationListening && currentLocation != null) {
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

			startTime = new Date();

			ListView weatherList = (ListView) findViewById(R.id.weatherList);
			Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
			TextView distance = (TextView) findViewById(R.id.tripDistance);
			ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
			ImageView btnMyLoc = (ImageView) findViewById(R.id.imgVwMyLoc);

			mapUI.setCompassEnabled(false);
			mMap.clear();

			// Remove my location listener
			if (myLocationListening) {
				locManager.removeUpdates(myLocationListener);
				myLocationListening = false;
			}

			// Initialize all route recording parameters properly
			recordingListening = true;
			totalDistance = 0;
			myLocationMarker = null;

			// Get last known location and move camera
			Location lastKnown = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (lastKnown != null) {

				LatLng lastKnownLatLng = new LatLng(lastKnown.getLatitude(),
						lastKnown.getLongitude());

				startMarker = mMap.addMarker(new MarkerOptions()
						.position(lastKnownLatLng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.cycling))
						.title(dateFormatter.format(startTime)));

				lastLocation = new Location(lastKnown);

				routePoints.add(new RoutePoint(lastLocation.getLatitude(),
						lastLocation.getLongitude()));

				plotMyLocationMarker(DEFAULTZOOM, DEFAULTZOOM, lastKnown);

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

			distance.setText(INITIAL_DISTANCE);
			timer.setVisibility(View.VISIBLE);
			distance.setVisibility(View.VISIBLE);
			btnStart.setVisibility(View.INVISIBLE);
			btnStop.setVisibility(View.VISIBLE);
			btnMyLoc.setVisibility(View.INVISIBLE);
			weatherList.setVisibility(View.INVISIBLE);
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

			if (startMarker != null) {
				startMarker.remove();
				startMarker = null;
			}

			showToast(resourceHandler
					.getString(R.string.message_single_route_point));
		} else {

			Date endTime = new Date();

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

			startActivity(intent);
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

		Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
		TextView distance = (TextView) findViewById(R.id.tripDistance);
		ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
		ImageView btnStart = (ImageView) findViewById(R.id.start_button);
		ImageView btnMyLoc = (ImageView) findViewById(R.id.imgVwMyLoc);

		mapUI.setCompassEnabled(true);
		recordingListening = false;
		locManager.removeUpdates(locationListener);

		timer.stop();
		timer.setVisibility(View.INVISIBLE);
		btnStop.setVisibility(View.INVISIBLE);
		btnStart.setVisibility(View.VISIBLE);
		btnMyLoc.setVisibility(View.VISIBLE);
		distance.setVisibility(View.INVISIBLE);
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

	private void setName(final Session session) {
		// Make an API call to get user data and define a
		// new callback to handle the response.
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// If the response is successful
						if (session == Session.getActiveSession()) {
							if (user != null) {

								Log.i("pratik",
										"username " + user.getId()
												+ user.getFirstName() + " "
												+ user.getLastName());
								LoggedUser.setText(user.getId());
							}
						}
						if (response.getError() != null) {
							// Handle errors, will do so later.
						}
					}

				});
		request.executeAsync();
	}
}
