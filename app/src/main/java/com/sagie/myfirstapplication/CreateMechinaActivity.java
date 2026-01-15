package com.sagie.myfirstapplication;

import android.app.DatePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateMechinaActivity extends BaseActivity {

    // הגדרת משתנים לרכיבי המסך
    private EditText etMechinaName, etBranch, etAddress;
    private Button btnPickDate, btnSave;
    private String selectedDate = "";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // שימוש ב-BaseActivity כפי שהגדרת
        setupMenu();
        setContentLayout(R.layout.activity_create_mechina); // וודא שזה שם ה-XML שלך

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // טעינת המפה
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;
                }
            });
        }

        // אתחול רכיבים
        etMechinaName = findViewById(R.id.etMechinaName);
        etBranch = findViewById(R.id.etBranch);
        etAddress = findViewById(R.id.etAddress);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);

        // כפתור לבחירת תאריך
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateMechinaActivity.this.showDatePicker();
            }
        });

        // כפתור שמירה
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateMechinaActivity.this.saveEventToFirebase();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            btnPickDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveEventToFirebase() {
        // 1. בדיקה האם המשתמש בכלל מחובר (חובה כדי למנוע קריסה ושיוך שגוי)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "עליך להיות מחובר כדי להוסיף אירוע", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. קבלת ה-UID האמיתי של המשתמש המחובר כרגע
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 3. קליטת הנתונים מהשדות
        String name = etMechinaName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || selectedDate.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. הפניה לנתיב המדויק תחת ה-UID המחובר
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance()
                .getReference("users") // וודא שזה u קטנה כמו בתמונה שלך
                .child(uid)
                .child("my_events");

        // 5. יצירת מזהה ייחודי לאירוע (Push ID)
        String eventId = userEventsRef.push().getKey();

        // 6. יצירת האובייקט (שימוש במחלקה שבנינו)
        MechinaEvent newEvent = new MechinaEvent(eventId, name, branch, selectedDate, address, 0.0, 0.0);
        getCoordinatesFromAddress(address, new IGeocodeCallback() {
            @Override
            public void onFinished(GeoPoint point) {
                // כאן יש לך את האובייקט ביד!
                Log.d("Location", "Lat: " + point.latitude + " Lon: " + point.longitude);
                newEvent.setLat(point.latitude);
                newEvent.setLng(point.longitude);
                // מציגים על המפה
                showLocationOnMap(point);
            }

            private void showLocationOnMap(GeoPoint geoPoint) {
                if (mMap == null || geoPoint == null || !geoPoint.isValid()) {
                    return;
                }

                // המרת ה-GeoPoint שלנו לאובייקט של Google Maps
                LatLng location = new LatLng(geoPoint.latitude, geoPoint.longitude);

                // ניקוי סימונים קודמים אם יש
                mMap.clear();

                // הוספת נעץ
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("הכתובת שנמצאה"));

                // הזזת המצלמה למיקום עם זום (15 זה זום קרוב וטוב לרמת רחוב)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CreateMechinaActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 7. שמירה ל-Firebase
        if (eventId != null) {
            userEventsRef.child(eventId).setValue(newEvent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "המיון נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish(); // חזרה למסך הקודם
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void getCoordinatesFromAddress(String addressString, IGeocodeCallback callback) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                        // חוזרים ל-UI Thread כדי שהמשתמש יוכל לעדכן רכיבי מסך בבטחה
                        CreateMechinaActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFinished(geoPoint);
                            }
                        });
                    } else {
                        CreateMechinaActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure("כתובת לא נמצאה");
                            }
                        });
                    }
                } catch (IOException e) {
                    CreateMechinaActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("שגיאת רשת/חיבור");
                        }
                    });
                }
            }
        }).start();
    }
}