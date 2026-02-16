package com.sagie.myfirstapplication;

public class MechinaEvent {
    public String eventId;
    public String mechinaName; // אנחנו שומרים על השם הזה כפי שביקשת
    public String branch;
    public String date;
    public String address;
    public double lat;
    public double lng;

    public MechinaEvent() {
    }

    public MechinaEvent(String eventId, String mechinaName, String branch, String date, String address, double lat, double lng) {
        this.eventId = eventId;
        this.mechinaName = mechinaName;
        this.branch = branch;
        this.date = date;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    // --- אלו הפונקציות שחסרו לך כדי שה-Activity יעבוד ---

    // כשמישהו קורא ל-getName, הוא יקבל את mechinaName
    public String getName() {
        return mechinaName;
    }
    public double getLat(){
        return this.lat;
    }

    public double getLng() {
        return this.lng;
    }

    // כשמישהו קורא ל-setName, זה יעדכן את mechinaName
    public void setName(String name) {
        this.mechinaName = name;
    }

    // שאר ה-Setters למיקום (כבר היו לך, השארתי אותם)
    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    // מומלץ להוסיף גם Getters לשאר השדות כדי שפיירבייס ימשוך אותם בקלות
    public String getBranch() { return branch; }
    public String getAddress() { return address; }
}