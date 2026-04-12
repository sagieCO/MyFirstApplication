package com.sagie.myfirstapplication;

import com.sagie.myfirstapplication.models.GeoPoint;

public interface IGeocodeCallback {
    void onFinished(GeoPoint point);

    void onFailure(String errorMessage);
}
