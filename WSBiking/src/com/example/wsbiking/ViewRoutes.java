package com.example.wsbiking;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ViewRoutes extends Activity {

	private DatabaseHandler dbHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_routes);
		dbHandler = DatabaseHandler.getInstance(this);
		populateList();
	}

	private void populateList() {
		// TODO Auto-generated method stub
		ArrayList<Route> routes = dbHandler.getRoutes();

		try {
			if (routes != null) {
				RouteAdapter adapter = new RouteAdapter(this,
						R.layout.singleroute, routes);

				ListView routesListView = (ListView) findViewById(R.id.routesList);

				routesListView.setAdapter(adapter);
			}
		} catch (Exception ex) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_routes, menu);
		return true;
	}

	public void showRouteOnMap(View showButton) {
		TextView txtVwRouteID;
		TextView txtVwSpeed;
		TextView txtVwDistance;
		TextView txtVwStartTime;
		TextView txtVwEndTime;

		View rowContainer = (View) showButton.getParent().getParent();

		txtVwRouteID = (TextView) rowContainer.findViewById(R.id.txtVwRouteID);
		txtVwSpeed = (TextView) rowContainer.findViewById(R.id.txtVwSpeed);
		txtVwDistance = (TextView) rowContainer
				.findViewById(R.id.txtVwDistance);
		txtVwStartTime = (TextView) rowContainer.findViewById(R.id.txtVwStartTime);
		txtVwEndTime = (TextView) rowContainer.findViewById(R.id.txtVwEndTime);

		Intent intent = new Intent(this, ViewRoute.class);
		intent.putExtra("routeID", txtVwRouteID.getText());
		intent.putExtra("totalDistance", txtVwDistance.getText());
		intent.putExtra("avgSpeed", txtVwSpeed.getText());
		intent.putExtra("startTime", txtVwStartTime.getText());
		intent.putExtra("endTime", txtVwEndTime.getText());
		
		startActivity(intent);
	}
}
