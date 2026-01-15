package com.sagie.myfirstapplication;

public interface IGeocodeCallback {
    void onFinished(GeoPoint point);

    void onFailure(String errorMessage);
}
