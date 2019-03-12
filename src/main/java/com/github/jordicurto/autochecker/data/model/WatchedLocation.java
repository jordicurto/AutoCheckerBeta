package com.github.jordicurto.autochecker.data.model;

import java.io.Serializable;

public class WatchedLocation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int OUTSIDE_LOCATION = 0;
	public static final int INSIDE_LOCATION = 1;
	public static final int FORCED_OUTSIDE_LOCATION = 2;

	private int id;
	private String name;
	private double longitude;
	private double latitude;
	private float radius;
	private int status;

	public WatchedLocation() {
		
	}
	
	public WatchedLocation(String name, double longitude, double latitude, float radius) {
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
		this.radius = radius;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public String toString() {
		return "(" + name + ") Lat: " + latitude + ", Long: " + longitude + ", Radius: " + radius;
	}

	public boolean isInside() {
		return status == INSIDE_LOCATION;
	}

	public boolean isForcedOutside() {
		return status == FORCED_OUTSIDE_LOCATION;
	}
}
