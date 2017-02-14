package com.github.jordicurto.autochecker.data.model;

import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.Duration;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class WatchedLocationRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int id;
    private WatchedLocation location;
    private Date checkIn;
    private Date checkOut;

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

    public Date getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Date checkIn) {
        this.checkIn = checkIn;
    }

    public Date getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Date checkOut) {
        this.checkOut = checkOut;
    }

    public boolean isActive() {
        return checkIn != null && checkOut == null;
    }

    public Duration calculateDuration() {
        long checkDuration = 0;
        if (checkIn != null) {
            if (checkOut == null) {
                checkDuration = (System.currentTimeMillis() - checkIn.getTime());
            } else {
                checkDuration = (checkOut.getTime() - checkIn.getTime());
            }
        }
        return new Duration(checkDuration);
    }

    public String getCheckString() {
        return DateUtils.timeFormat.format(checkIn)
                + " - "
                + (checkOut != null ? DateUtils.timeFormat
                .format(checkOut) : "");
    }
}
