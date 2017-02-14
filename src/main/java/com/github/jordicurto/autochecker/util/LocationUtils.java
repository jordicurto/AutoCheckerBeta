package com.github.jordicurto.autochecker.util;

import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.location.Location;

public class LocationUtils {

	private static final double earthRadius = 6378137.0;
	private static final double c180 = 180.0;

	public static LatLngBounds getLocationBounds(WatchedLocation location) {

		double centerLatitude = location.getLatitude();
		double centerLongitude = location.getLongitude();
		float offset = location.getRadius();

		double offsetLat = offset / earthRadius;
		double offsetLon = offset / (earthRadius
				* Math.cos(Math.PI * centerLatitude / c180));

		double offsetLatDeg = ((offsetLat * c180) / Math.PI);
		double offsetLonDeg = ((offsetLon * c180) / Math.PI);

		LatLng southwest = new LatLng(centerLatitude - offsetLatDeg,
				centerLongitude - offsetLonDeg);
		LatLng northeast = new LatLng(centerLatitude + offsetLatDeg,
				centerLongitude + offsetLonDeg);

		return new LatLngBounds(southwest, northeast);

	}
	
	public static Location getLocationFromWatchedLocation(WatchedLocation wLocation) {
		Location location = new Location("");
		location.setLatitude(wLocation.getLatitude());
		location.setLongitude(wLocation.getLongitude());
		return location;
	}
}
