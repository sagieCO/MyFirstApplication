package com.sagie.myfirstapplication;


public class MechinaEvent {
    public String eventId;     // מזהה ייחודי מ-Firebase
    public String mechinaName; // שם המכינה
    public String branch;      // שלוחה
    public String date;        // תאריך המיון (בפורמט DD/MM/YYYY)
    public String address;     // כתובת
    public double lat;         // קווי רוחב למפה
    public double lng;         // קווי אורך למפה

    // קונסטרקטור ריק - חובה עבור Firebase כדי להמיר JSON לאובייקט Java
    public MechinaEvent() {
    }

    // קונסטרקטור מלא ליצירת אירוע חדש בקוד
    public MechinaEvent(String eventId, String mechinaName, String branch, String date, String address, double lat, double lng) {
        this.eventId = eventId;
        this.mechinaName = mechinaName;
        this.branch = branch;
        this.date = date;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    // מתודות עזר (אופציונליות) לעדכון ידני של מיקום
    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}