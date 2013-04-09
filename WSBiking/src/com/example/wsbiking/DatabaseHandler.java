package com.example.wsbiking;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * All the database interactions will go in this Singleton handler for DB
 * 
 * @author Leon Dmello
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables

	/**
	 * instance.
	 */
	private static DatabaseHandler instance = null;
	private static SQLiteDatabase appWritableDb;

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Log tag for logging errors
	private static final String LOG_TAG = "DB Handler";

	// Database Name
	private static final String DATABASE_NAME = "routesManager";

	// Routes table name
	private static final String TABLE_ROUTES = "routes";
	// Route points table name
	private static final String TABLE_ROUTE_POINTS = "routePoints";

	// TODO: Need to include user ID in table as well
	// Routes Table Columns names
	private static final String ROUTEID = "routeID";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String AVGSPEED = "speed";
	private static final String DISTANCE = "distance";
	private static final String STARTTIME = "startTime";
	private static final String ENDTIME = "endTime";

	// Routes Table Column indexes
	private static final Integer ROUTEIDINDEX = 0;
	private static final Integer NAMEINDEX = 1;
	private static final Integer DESCRIPTIONINDEX = 2;
	private static final Integer AVGSPEEDINDEX = 3;
	private static final Integer DISTANCEINDEX = 4;
	private static final Integer STARTTIMEINDEX = 5;
	private static final Integer ENDTIMEINDEX = 6;

	// Route points Table Columns names
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";

	/**
	 * @return instance.
	 */
	public static DatabaseHandler getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHandler(context.getApplicationContext());
		}

		return instance;
	}

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Returns a writable database instance in order not to open and close many
	 * SQLiteDatabase objects simultaneously
	 * 
	 * @return a writable instance to SQLiteDatabase
	 */
	public SQLiteDatabase getMyWritableDatabase() {
		if ((appWritableDb == null) || (!appWritableDb.isOpen())) {
			appWritableDb = this.getWritableDatabase();
		}

		return appWritableDb;
	}

	@Override
	public void close() {
		super.close();
		if (appWritableDb != null) {
			appWritableDb.close();
			appWritableDb = null;
		}
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_ROUTE_TABLE = "CREATE TABLE " + TABLE_ROUTES + "("
				+ ROUTEID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME
				+ " TEXT," + DESCRIPTION + " TEXT," + AVGSPEED + " REAL,"
				+ DISTANCE + " REAL," + STARTTIME + " TEXT," + ENDTIME
				+ " TEXT); ";

		String CREATE_ROUTE_POINTS_TABLE = "CREATE TABLE " + TABLE_ROUTE_POINTS
				+ "(" + ROUTEID + " INTEGER," + LATITUDE + " REAL," + LONGITUDE
				+ " REAL," + "FOREIGN KEY(" + ROUTEID + ") REFERENCES "
				+ TABLE_ROUTES + "(" + ROUTEID + ")" + "); ";

		try {
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_ROUTE_POINTS_TABLE);
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to create tables: " + ex.getMessage());
		}
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String DROP_TABLES = "DROP TABLE IF EXISTS " + TABLE_ROUTE_POINTS
				+ "; " + "DROP TABLE IF EXISTS " + TABLE_ROUTES + "; ";
		// Drop older table if existed

		try {
			db.execSQL(DROP_TABLES);
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to drop tables: " + ex.getMessage());
		}

		// Create tables again
		onCreate(db);
	}

	/**
	 * Delete route if failure to insert route points
	 * 
	 * @param rowID
	 */
	public void deleteRoute(Integer rowID) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			if (db.delete(TABLE_ROUTES, ROUTEID + "=?",
					new String[] { String.valueOf(rowID) }) == 0)
				throw new Exception("Failed to delete Route entry");

		} catch (Exception ex) {
			Log.e(LOG_TAG,
					"Failed to delete route (Phantom route without route points): "
							+ ex.getMessage());
		}
	}

	// Create a new route entry
	public Integer addRoute(ArrayList<RoutePoint> routePoints, String name,
			String description, float distance, float avgSpeed,
			String startTime, String endTime) {

		Integer insertedRowID = 0;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(NAME, name);
			values.put(DESCRIPTION, description);
			values.put(DISTANCE, distance);
			values.put(AVGSPEED, avgSpeed);
			values.put(STARTTIME, startTime);
			values.put(ENDTIME, endTime);

			// Inserting Row
			insertedRowID = (int) db.insert(TABLE_ROUTES, null, values);

			if (insertedRowID == -1)
				throw new Exception("Failed to insert Route entry");

			if (!insertRoutePoints(routePoints, insertedRowID)) {
				deleteRoute(insertedRowID);
				insertedRowID = -1;
			}

		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to insert route values: " + ex.getMessage());
		}

		return insertedRowID;
	}

	/**
	 * Inserts the route points associated with a route into the database
	 * 
	 * @param routePoints
	 * @param insertedRowID
	 * @return
	 */
	private boolean insertRoutePoints(ArrayList<RoutePoint> routePoints,
			long insertedRowID) {

		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();

		boolean result = false;

		try {
			String sqlString = "Insert into " + TABLE_ROUTE_POINTS + " ("
					+ ROUTEID + ", " + LATITUDE + "," + LONGITUDE
					+ ") values(?,?,?)";

			SQLiteStatement sqlSTMT = db.compileStatement(sqlString);

			int routeSize = routePoints.size();

			for (int rowCounter = 0; rowCounter < routeSize; rowCounter++) {

				RoutePoint point = routePoints.get(rowCounter);

				sqlSTMT.bindDouble(1, insertedRowID);
				sqlSTMT.bindDouble(2, point.getLatitude());
				sqlSTMT.bindDouble(3, point.getLongitude());

				sqlSTMT.execute();
			}

			db.setTransactionSuccessful();
			result = true;

		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to insert route points: " + ex.getMessage());
		} finally {
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Returns an array list containing all routes saved
	 * 
	 * @return
	 */
	public ArrayList<Route> getRoutes() {

		// TODO: Need to include user ID in table as well
		Cursor cursor = null;
		ArrayList<Route> allRoutes = null;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			cursor = db.query(TABLE_ROUTES, new String[] { ROUTEID, NAME,
					DESCRIPTION, AVGSPEED, DISTANCE, STARTTIME, ENDTIME },
					null, null, null, null, null);

			if (cursor.moveToLast()) {

				allRoutes = new ArrayList<Route>();
				Integer routeID;
				String routeName, routeDesc, routeStart, routeEnd;
				;
				float routeSpeed, routeDistance;

				while (!cursor.isBeforeFirst()) {

					routeID = cursor.getInt(ROUTEIDINDEX);
					routeName = cursor.getString(NAMEINDEX);
					routeDesc = cursor.getString(DESCRIPTIONINDEX);
					routeSpeed = cursor.getFloat(AVGSPEEDINDEX);
					routeDistance = cursor.getFloat(DISTANCEINDEX);
					routeStart = cursor.getString(STARTTIMEINDEX);
					routeEnd = cursor.getString(ENDTIMEINDEX);

					allRoutes.add(new Route(routeID, routeName, routeDesc,
							routeSpeed, routeDistance, routeStart, routeEnd));

					cursor.moveToPrevious();
				}
			}

			cursor.close();

		} catch (Exception ex) {
			Log.e(LOG_TAG,
					"Failed to get all routes from route table: "
							+ ex.getMessage());
		}

		return allRoutes;
	}

	/**
	 * Returns a cursor with all the route points associated with a route ID
	 * 
	 * @param routeID
	 * @return
	 */
	public Cursor getRoutePoints(Integer routeID) {

		Cursor cursor = null;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			cursor = db.query(TABLE_ROUTE_POINTS, new String[] { LATITUDE,
					LONGITUDE }, ROUTEID + "= ?",
					new String[] { String.valueOf(routeID) }, null, null, null);

		} catch (Exception ex) {
			Log.e(LOG_TAG,
					"Failed to get all route points from points table table: "
							+ ex.getMessage());
		}

		return cursor;
	}

	//TODO: Need to include user ID in call as well
	/**
	 * Get's all Lat Long points from table
	 * 
	 * @return
	 */
	public Cursor getEveryLatLong() {
		Cursor cursor = null;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			cursor = db.query(TABLE_ROUTE_POINTS, new String[] { LATITUDE,
					LONGITUDE }, null, null, null, null, null);

		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to get all points from points table table: "
					+ ex.getMessage());
		}

		return cursor;
	}
}
