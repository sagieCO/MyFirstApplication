package com.sagie.myfirstapplication.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_full_map);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

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

        // הגדרת מרכז ישראל (קו רוחב וקו אורך מרכזיים)
        LatLng israelCenter = new LatLng(31.4117, 35.0818);

        // זום ברמה 7.5 מציג את רוב המדינה מהצפון עד הדרום במבט רחוק
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israelCenter, 7.5f));

        // הוספת פקדי זום לשימוש ידני של המשתמש
        mMap.getUiSettings().setZoomControlsEnabled(true);

        loadMarkersFromFirebase();
    }

    private void loadMarkersFromFirebase() {
        if (userEventsRef == null) return;

        userEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear(); // מנקה את המפה כדי שלא יהיו כפילויות בעדכון נתונים

                for (DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent event = ds.getValue(MechinaEvent.class);
                    if (event != null && event.getLat() != 0) {
                        LatLng pos = new LatLng(event.getLat(), event.getLng());

                        // הוספת הסמן ללא הזזת המצלמה
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(event.getName())
                                .snippet("שלוחה: " + event.getBranch()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FullMapActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}