package com.sagie.myfirstapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.IGeocodeCallback;
import com.sagie.myfirstapplication.models.GeoPoint;
import com.sagie.myfirstapplication.models.MechinaEvent;
import com.sagie.myfirstapplication.R;

import java.util.Iterator;
import java.util.List;

public class FullMapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference userEventsRef;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_full_map);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.full_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng israelCenter = new LatLng(31.4117, 35.0818);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israelCenter, 7.5f));

        mMap.getUiSettings().setZoomControlsEnabled(true);

        loadMarkersFromFirebase();

        enableUserLocation();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                LatLng position = marker.getPosition();
                openGoogleMapsNavigation(position.latitude, position.longitude);
            }
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            zoomToUserLocation();
        }
    }
    private void getCoordinatesFromAddress(String address, IGeocodeCallback callback) {
        Geocoder geocoder = new Geocoder(this);

        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(address + ", ישראל", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    runOnUiThread(() ->
                            callback.onFinished(new GeoPoint(a.getLatitude(), a.getLongitude())));
                }

                else {
                    runOnUiThread(() ->
                            callback.onFailure("לא נמצאה כתובת"));
                }

            }
            catch (Exception e) {
                runOnUiThread(() ->
                        callback.onFailure(e.getMessage()));

            }

        }).start();

    }
    private void loadHomeMarker() {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null)
            return;

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("address").get().addOnSuccessListener(snapshot -> {

                    String address = snapshot.getValue(String.class);
                    if (address == null || address.trim().isEmpty()) {
                        return;
                    }

                    getCoordinatesFromAddress(address, new IGeocodeCallback() {
                                @Override
                                public void onFinished(
                                        GeoPoint point
                                ) {

                                    LatLng home =
                                            new LatLng(
                                                    point.latitude,
                                                    point.longitude
                                            );

                                    mMap.addMarker(new MarkerOptions().position(home).title("הבית שלי").snippet(address).icon(
                                                            com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                                                    BitmapDescriptorFactory.HUE_BLUE
                                                            )
                                                    )
                                    );

                                }

                                @Override
                                public void onFailure(
                                        String errorMessage
                                ) {

                                }

                            }
                    );

                });

    }
    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        if (userLatLng.latitude>=29 && userLatLng.latitude<=34 && userLatLng.longitude>=34 && userLatLng.longitude<=36) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                        } else {
                            Toast.makeText(FullMapActivity.this, "מיקומך נמצא מחוץ לטווח התצוגה", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void loadMarkersFromFirebase() {
        if (userEventsRef == null) {
            return;
        }

        userEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                loadHomeMarker();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    DataSnapshot ds = iterator.next();
                    MechinaEvent event = ds.getValue(MechinaEvent.class);

                    if (event != null && event.getLat() != 0) {
                        LatLng pos = new LatLng(event.getLat(), event.getLng());
                        MarkerOptions options = new MarkerOptions();
                        options.position(pos);
                        options.title(event.getName());
                        options.snippet("שלוחה: " + event.getBranch() + "\nלחץ כאן לניווט ב-Google Maps");

                        mMap.addMarker(options);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FullMapActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGoogleMapsNavigation(double lat, double lng) {
        String uriString = "google.navigation:q=" + lat + "," + lng;
        Uri gmmIntentUri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "אפליקציית Google Maps אינה מותקנת", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "נדרשת הרשאת מיקום כדי להציג את המיקום שלך", Toast.LENGTH_SHORT).show();
            }
        }
    }
}