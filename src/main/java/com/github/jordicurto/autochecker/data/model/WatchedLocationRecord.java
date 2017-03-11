package com.github.jordicurto.autochecker.data.model;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.io.Serializable;

public class WatchedLocationRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int id;
    private WatchedLocation location;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WatchedLocation getLocation() {
        return location;
    }

    public void setLocation(WatchedLocation location) {
        this.location = location;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public boolean isActive() {
        return checkIn != null && checkOut == null;
    }

}
