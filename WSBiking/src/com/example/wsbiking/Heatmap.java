package com.example.wsbiking;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

public class Heatmap extends Activity {

	private DatabaseHandler dbHandler;
	private static final int LATITUDE = 0;
	private static final int LONGITUDE = 1;
	private static final String MAP_URL = "file:///android_asset/heatmap.html";
	private static final String GOOGLE_MAP_POINT = "new google.maps.LatLng(%s,%s),";
	private static final String LOG_TAB = "Heatmap";

	private WebView webView;

	@Override
	public void onStop() {
		dbHandler.close();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heatmap, menu);
		return true;
	}

	@Override
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heatmap);
		dbHandler = DatabaseHandler.getInstance(this);
		setupWebView();
	}

	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {
		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
		webView.loadUrl(MAP_URL);
	}

	/**
	 * JavaScript interface to talk with the web view
	 * 
	 * @author Leon Dmello
	 * 
	 */
	public class JavaScriptInterface {
		private Context appContext;

		public JavaScriptInterface(Context context) {
			this.appContext = context;
		}

		// TODO: Pass user ID to get current user's lat long points
		public String getPointsData() {
			StringBuilder pointsString = new StringBuilder("[");

			try {
				Cursor pointsCursor = dbHandler
						.getEveryLatLong(Main.logged_user);

				if (pointsCursor != null) {

					if (pointsCursor.moveToFirst()) {
						while (!pointsCursor.isAfterLast()) {
							pointsString.append(String.format(GOOGLE_MAP_POINT,
									pointsCursor.getDouble(LATITUDE),
									pointsCursor.getDouble(LONGITUDE)));

							pointsCursor.moveToNext();

						}
					}

					pointsCursor.close();
				}
			} catch (Exception ex) {
				Log.e(LOG_TAB, ex.getMessage());
				pointsString.append("ERROR_");
			}

			if(pointsString.length() > GOOGLE_MAP_POINT.length())
				return pointsString.substring(0, pointsString.length() - 1) + "]";
			else
				return "NODATA";
		}

		public void errorInPoints() {
			showToast("Unable to plot heatmap of route points");
		}
		
		public void noRoutePoints() {
			showToast("No route points to plot heatmap");
		}
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
}
