package com.crux.pratd.travelbphc;

import java.util.Map;

/**
 * Created by pratd on 20-01-2018.
 */

public class TravelPlan {
    private Map<String, Object> travellers;
    private String source, dest, date, time, creator, space;

    public TravelPlan() {
    }

    TravelPlan(String source, String dest, String date, String time, String creator, String space, Map<String, Object> travellers) {
        this.source = source;
        this.dest = dest;
        this.date = date;
        this.time = time;
        this.creator = creator;
        this.space = space;
        this.travellers = travellers;
    }

    String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    Map<String, Object> getTravellers() {
        return travellers;
    }

    public void setTravellers(Map<String, Object> travellers) {
        this.travellers = travellers;
    }
}
