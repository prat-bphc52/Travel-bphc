package com.crux.pratd.travelbphc.model;

import java.util.Map;

/**
 * Created by pratd on 20-01-2018.
 */

public class TravelPlan {
    private Map<String, Object> travellers;
    private String source, dest, date, time, creator, space;

    public TravelPlan(){ }

    public TravelPlan(String source, String dest, String date, String time, String creator, String space, Map<String, Object> travellers) {
        this.source = source;
        this.dest = dest;
        this.date = date;
        this.time = time;
        this.creator = creator;
        this.space = space;
        this.travellers = travellers;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDest() {
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Map<String, Object> getTravellers() {
        return travellers;
    }

    public void setTravellers(Map<String, Object> travellers) {
        this.travellers = travellers;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }
}
