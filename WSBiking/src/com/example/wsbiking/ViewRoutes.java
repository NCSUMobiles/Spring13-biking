package com.example.wsbiking;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Show all routes recorded by user, show no data message otherwise
 * 
 * @author Leon Dmello
 * 
 */
public class ViewRoutes extends Activity {

	// Log tag for logging errors
	private static final String LOG_TAG = "View Routes Activity";

	private DatabaseHandler dbHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_routes);
		dbHandler = DatabaseHandler.getInstance(this);
		populateList();
	}

	/**
	 * Populates the routes list
	 */
	private void populateList() {
		ArrayList<Route> routes = dbHandler.getRoutes();

		try {
			if (routes != null) {
				RouteAdapter adapter = new RouteAdapter(this,
						R.layout.singleroute, routes);

				ListView routesListView = (ListView) findViewById(R.id.routesList);
				routesListView.setAdapter(adapter);
				routesListView.setVisibility(View.VISIBLE);
			} else {
				RelativeLayout nodata = (RelativeLayout) findViewById(R.id.noDataHolder);
				nodata.setVisibility(View.VISIBLE);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
			showToast("Unable to display routes");
			RelativeLayout nodata = (RelativeLayout) findViewById(R.id.noDataHolder);
			nodata.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_routes, menu);
		return true;
	}

	/**
	 * Launch view route activity o plot route on map and show route details
	 * 
	 * @param showButton
	 */
	public void showRouteOnMap(View showButton) {

		try {
			View rowContainer = (View) showButton.getParent().getParent();

			TextView txtVwRouteID = (TextView) rowContainer
					.findViewById(R.id.txtVwRouteID);
			TextView txtVwSpeed = (TextView) rowContainer
					.findViewById(R.id.txtVwSpeed);
			TextView txtVwDistance = (TextView) rowContainer
					.findViewById(R.id.txtVwDistance);
			TextView txtVwStartTime = (TextView) rowContainer
					.findViewById(R.id.txtVwStartTime);
			TextView txtVwEndTime = (TextView) rowContainer
					.findViewById(R.id.txtVwEndTime);

			Intent intent = new Intent(this, ViewRoute.class);
			intent.putExtra("routeID", txtVwRouteID.getText());
			intent.putExtra("totalDistance", txtVwDistance.getText());
			intent.putExtra("avgSpeed", txtVwSpeed.getText());
			intent.putExtra("startTime", txtVwStartTime.getText());
			intent.putExtra("endTime", txtVwEndTime.getText());

			startActivity(intent);
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
			showToast("Unable to show route");
		}
	}

	/**
	 * generic method to display toast
	 * 
	 * @param message
	 */
	public void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);

		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
}
