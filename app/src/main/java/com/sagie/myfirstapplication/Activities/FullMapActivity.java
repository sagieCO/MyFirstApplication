package com.sagie.myfirstapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.MechinaEvent;
import com.sagie.myfirstapplication.R;

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

        // הגדרת כיווניות RTL (דרישת הנדסת אנוש)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // אתחול שירות המיקום (חובה עבור סעיף 9 במחוון)
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 1. הגדרת מרכז ישראל (קו רוחב וקו אורך שנותנים מבט מאוזן על המדינה)
        LatLng israelCenter = new LatLng(31.4117, 35.0818);

        // 2. פתיחת המפה בזום 7.5 - זה הזום האידיאלי לראות את כל ישראל ברוב המסכים
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israelCenter, 7.5f));

        // 3. הגדרות UI
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // 4. בדיקת הרשאות והפעלת הנקודה הכחולה (דרישה 9 ו-5 במחוון)
        enableUserLocation();

        // 5. הגדרת מאזין לניווט
        mMap.setOnInfoWindowClickListener(marker -> {
            openGoogleMapsNavigation(marker.getPosition().latitude, marker.getPosition().longitude);
        });

        loadMarkersFromFirebase();
    }

    private void enableUserLocation() {
        // בדיקת הרשאות זמן-ריצה (דרישת חובה סעיף 5 במחוון)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // הצגת המיקום הנוכחי של המשתמש על המפה
            mMap.setMyLocationEnabled(true);
            zoomToUserLocation();
        }
    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // התמקדות במיקום המשתמש בישראל
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                }
            });
        }
    }

    private void loadMarkersFromFirebase() {
        if (userEventsRef == null) return;
        userEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent event = ds.getValue(MechinaEvent.class);
                    if (event != null && event.getLat() != 0) {
                        LatLng pos = new LatLng(event.getLat(), event.getLng());
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(event.getName())
                                .snippet("שלוחה: " + event.getBranch() + "\nלחץ כאן לניווט ב-Google Maps"));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FullMapActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * פונקציה לפתיחת ניווט ב-Google Maps בלבד
     */
    private void openGoogleMapsNavigation(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // הגדרה שזה יפתח רק את אפליקציית גוגל מפות
        mapIntent.setPackage("com.google.android.apps.maps");

        // בדיקה שהאפליקציה קיימת במכשיר למניעת קריסה (טיפול בשגיאות - סעיף 10)
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