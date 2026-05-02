package com.sagie.myfirstapplication.models;

public class MechinaEvent {
    public String eventId;
    public String mechinaName;
    public String branch;
    public String date;
    public String time;
    public String address;
    public double lat;
    public double lng;
    public String region;        // השדה שהיה חסר
    public String religiousType; // השדה שהיה חסר
    public  String eventColor;


    // בנאי ריק חובה עבור Firebase
    public MechinaEvent() {
    }

    // בנאי מלא - שים לב לסדר הפרמטרים!
    public MechinaEvent(String eventId, String mechinaName, String branch, String date, String time, String address, double lat, double lng, String region, String religiousType,String eventColor) {
        this.eventId = eventId;
        this.mechinaName = mechinaName;
        this.branch = branch;
        this.date = date;
        this.time = time;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.region = region;
        this.religiousType = religiousType;
        this.eventColor=eventColor;
    }

    // Getters
    public String getName() { return mechinaName; }
    public void setName(String name) { this.mechinaName = name; }
    public String getBranch() { return branch; }
    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getEventColor() { return eventColor != null ? eventColor : "#3F51B5"; }
    public void setEventColor(String eventColor) { this.eventColor = eventColor; }
    public String getTime() { return time; }
    public String getRegion() { return region; }
    public String getReligiousType() { return religiousType; }
}