package com.example.wsbiking;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

		if (routeTitle.length() == 0)
			routeTitle = this.startTime.toString() + " "
					+ this.endtime.toString();

		if (dbHandler
				.addRoute(this.routePoints, routeTitle, routeDesc,
						this.totalDistance, this.avgSpeed, this.startTime,
						this.endtime) > 0) {

			Toast toast = Toast.makeText(getApplicationContext(),
					"Route Saved", Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Route couldn't be saved", Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}

		this.setResult(Activity.RESULT_OK, null);

		dbHandler.close();
		super.onStop();
	}

	private void InitializeElements() {
		Intent recordActivity = getIntent();

		this.routePoints = recordActivity.getExtras().getParcelableArrayList(
				"routePoints");
		this.totalDistance = recordActivity.getFloatExtra("totalDistance", 0);
		this.avgSpeed = recordActivity.getFloatExtra("avgSpeed", 0);
		this.startTime = recordActivity.getStringExtra("startTime");
		this.endtime = recordActivity.getStringExtra("endTime");

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
