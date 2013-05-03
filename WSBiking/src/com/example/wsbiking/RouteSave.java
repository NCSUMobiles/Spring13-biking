package com.example.wsbiking;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author Leon Dmello This the activity which gets fired when route recording
 *         is stopped It allows to discard or save a route with the facility to
 *         provide a title and description for the route.
 * 
 */
public class RouteSave extends Activity {
	private static final int SECS = 10;
	private ArrayList<RoutePoint> routePoints;
	private float totalDistance, avgSpeed;
	private String startTime, endtime;
	private String weatherInfo;
	private DatabaseHandler dbHandler = null;

	// Handler to Hide after some seconds
	final Handler handler = new Handler();
	final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_save);
		dbHandler = DatabaseHandler.getInstance(this);
		InitializeElements();
	}

	@Override
	public void onStop() {

		EditText edtTitle = (EditText) findViewById(R.id.edtTxTitle);
		EditText edtDesc = (EditText) findViewById(R.id.edtTxtDesc);

		String routeTitle = edtTitle.getText().toString();
		String routeDesc = edtDesc.getText().toString();
		
		HttpHandler httphandle = new HttpHandler();

		if (routeTitle.length() == 0)
			routeTitle = this.startTime.toString() + " "
					+ this.endtime.toString();

		Route route = new Route(this.routePoints, null, routeTitle, routeDesc,
				this.avgSpeed, this.totalDistance, this.startTime,
				this.endtime, Main.logged_user);

		int routeID = dbHandler.addRoute(route);
		
	    route.setID(routeID);
	   
		httphandle.doInBackground(this.weatherInfo);

		// TODO: Server call to save route to server and also send all pending
		// routes that aren't synced

		if (routeID > 0) {
			showToast("Route Saved");
		} else {
			showToast("Route couldn't be saved");
		}

		dbHandler.close();
		super.onStop();
	}

	public void closeDialog(View saveButton) {
		this.finish();
	}

	private void InitializeElements() {
		Intent recordActivity = getIntent();

		this.routePoints = recordActivity.getExtras().getParcelableArrayList(
				"routePoints");
		this.totalDistance = recordActivity.getFloatExtra("totalDistance", 0);
		this.avgSpeed = recordActivity.getFloatExtra("avgSpeed", 0);
		this.startTime = recordActivity.getStringExtra("startTime");
		this.endtime = recordActivity.getStringExtra("endTime");
		this.weatherInfo = recordActivity.getStringExtra("weatherInfo");

		EditText edtTitle = (EditText) findViewById(R.id.edtTxTitle);
		EditText edtDesc = (EditText) findViewById(R.id.edtTxtDesc);

		edtTitle.addTextChangedListener(new textChangedListener());
		edtDesc.addTextChangedListener(new textChangedListener());

		handler.postDelayed(runnable, SECS * 1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_save, menu);
		return true;
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

	private class textChangedListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, SECS * 1000);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}
	}
}
