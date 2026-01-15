package com.sagie.myfirstapplication;

public class GeoPoint {
    public double latitude;
    public double longitude;

    public GeoPoint(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public boolean isValid() {
        //  בודק שהקואורדינטות לא 0
        return latitude != 0.0 && longitude != 0.0;
    }
}