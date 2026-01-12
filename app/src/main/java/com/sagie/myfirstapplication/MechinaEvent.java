package com.sagie.myfirstapplication;

public class MechinaEvent {
    public String eventId;     // מזהה ייחודי מ-Firebase
    public String mechinaName; // שם המכינה
    public String branch;      // שלוחה
    public String date;        // תאריך המיון
    public String address;     // כתובת
    public double lat;         // קווי רוחב למפה
    public double lng;         // קווי אורך למפה

    // קונסטרקטור ריק - חובה עבור Firebase
    public MechinaEvent() {}

    public MechinaEvent(String eventId, String mechinaName, String branch, String date, String address, double lat, double lng) {
        this.eventId = eventId;
        this.mechinaName = mechinaName;
        this.branch = branch;
        this.date = date;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}