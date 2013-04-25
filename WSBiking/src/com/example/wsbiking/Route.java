package com.example.wsbiking;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The route class to record the various parameters associated with a route
 * 
 * @author Leon Dmello
 * 
 */
public class Route implements Parcelable {
	private Integer ID;
	private String title, description, startTime, endTime, userID;
	private float speed, distance;
	private ArrayList<RoutePoint> points;

	public Route(ArrayList<RoutePoint> points, Integer routeID,
			String routeName, String routeDesc, float avgSpeed,
			float routeDistance, String routeStart, String routeEnd,
			String userID) {
		this.ID = routeID;
		this.title = routeName;
		this.description = routeDesc;
		this.speed = avgSpeed;
		this.distance = routeDistance;
		this.startTime = routeStart;
		this.endTime = routeEnd;
		this.userID = userID;

		if (points != null)
			this.points = points;
		else
			this.points = new ArrayList<RoutePoint>();
	}

	/**
	 * This will be used only by the MyCreator
	 * 
	 * @param source
	 */
	public Route(Parcel source) {
		/*
		 * Reconstruct from the Parcel
		 */
		ID = source.readInt();
		title = source.readString();
		description = source.readString();
		speed = source.readFloat();
		distance = source.readFloat();
		startTime = source.readString();
		endTime = source.readString();
		userID = source.readString();
	}

	public Integer getID() {
		return this.ID;
	}
	
	public void setID(Integer ID) {
		this.ID = ID;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public float getSpeed() {
		return this.speed;
	}

	public String getStartTime() {
		return this.startTime;
	}

	public String getEndTime() {
		return this.endTime;
	}

	public String getUserID() {
		return this.userID;
	}

	public float getDistance() {
		return this.distance;
	}

	public ArrayList<RoutePoint> getPoints() {
		return this.points;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeFloat(speed);
		dest.writeFloat(distance);
		dest.writeString(startTime);
		dest.writeString(endTime);
		dest.writeString(userID);
	}

	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		public Route createFromParcel(Parcel in) {
			return new Route(in);
		}

		public Route[] newArray(int size) {
			return new Route[size];
		}
	};
}
