package com.example.wsbiking;

import java.util.Date;

/**
 * The route class to record teh various paraameters associated with a route
 * 
 * @author Leon Dmello
 * 
 */
public class Route {
	private Integer ID;
	private String title, description,startTime, endTime;
	private float speed, distance;

	public Route(Integer routeID, String routeName, String routeDesc,
			float avgSpeed, float routeDistance, String routeStart, String routeEnd) {
		this.ID = routeID;
		this.title = routeName;
		this.description = routeDesc;
		this.speed = avgSpeed;
		this.distance = routeDistance;
		this.startTime = routeStart;
		this.endTime = routeEnd;
	}

	public Integer getID() {
		return this.ID;
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

	public float getDistance() {
		return this.distance;
	}
}
