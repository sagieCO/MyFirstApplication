package com.sagie.myfirstapplication.Activities;

import android.app.DatePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.GeoPoint;
import com.sagie.myfirstapplication.IGeocodeCallback;
import com.sagie.myfirstapplication.MechinaEvent;
import com.sagie.myfirstapplication.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateMechinaActivity extends BaseActivity {

    private EditText etMechinaName, etBranch, etAddress;
    private Button btnPickDate, btnSave,btnDialog;
    private String selectedDate = "";
    private GoogleMap mMap;
    private Spinner spMechinot, spBranches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_create_mechina);

        // הגדרת כיווניות RTL לכל המסך
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initViews();
        initMap();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        etMechinaName = findViewById(R.id.etMechinaName);
        etBranch = findViewById(R.id.etBranch);
        etAddress = findViewById(R.id.etAddress);
        btnDialog = findViewById(R.id.btnManualEntry);
        // הגדרת כיווניות עברית לשדות הראשיים
        etMechinaName.setTextDirection(View.TEXT_DIRECTION_RTL);
        etBranch.setTextDirection(View.TEXT_DIRECTION_RTL);
        etAddress.setTextDirection(View.TEXT_DIRECTION_RTL);

        spMechinot = findViewById(R.id.spMechinot);
        spBranches = findViewById(R.id.spBranches);

        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> mMap = googleMap);
        }
    }

    private List<MechinaEvent> allMechinotEvents = new ArrayList<>();
    private void setupSpinner() {
        allMechinotEvents.clear(); // ניקוי הרשימה לפני טעינה

        // 1. שלב ראשון: טעינה מה-JSON
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString != null) {
                JSONObject root = new JSONObject(jsonString);
                JSONArray mechinotArray = root.getJSONArray("mechinot");

                for (int i = 0; i < mechinotArray.length(); i++) {
                    JSONObject obj = mechinotArray.getJSONObject(i);
                    // יוצרים אובייקט זמני עבור ה-Spinner (שם בלבד)
                    MechinaEvent m = new MechinaEvent();
                    m.setName(obj.getString("name"));
                    // כאן ב-JSON יש מערך של שלוחות, אז נשמור רק את השם לצורך ה-Spinner הראשי
                    allMechinotEvents.add(m);
                }
            }
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Error loading JSON", e);
        }

        // 2. שלב שני: טעינה מה-Firebase (המכינות הידניות)
        loadManualMechinotFromFirebase();
    }
    private void loadManualMechinotFromFirebase() {
        // מאזין לענף mechinot ב-Firebase
        FBRef.mechinotRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                // כדי למנוע כפילויות בכל פעם שהנתונים משתנים, ננקה את מה שהוספנו מה-Firebase
                // (הדרך הפשוטה ביותר היא לרענן את כל הרשימה מחדש או לבצע ניהול רשימות)

                // נשמור רק את אלו מה-JSON כבסיס
                int jsonSize = getJSONMechinotCount();
                if (allMechinotEvents.size() > jsonSize) {
                    allMechinotEvents.subList(jsonSize, allMechinotEvents.size()).clear();
                }

                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent manualMechina = ds.getValue(MechinaEvent.class);
                    if (manualMechina != null) {
                        allMechinotEvents.add(manualMechina);
                    }
                }

                // עדכון ה-Adapter של ה-Spinner
                updateMainSpinnerAdapter();
            }
            private void updateMainSpinnerAdapter() {
                List<String> displayNames = new ArrayList<>();
                for (MechinaEvent m : allMechinotEvents) {
                    displayNames.add(m.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateMechinaActivity.this, android.R.layout.simple_spinner_item, displayNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spMechinot.setAdapter(adapter);
            }
            private int getJSONMechinotCount() {
                try {
                    JSONObject root = new JSONObject(loadJSONFromAsset());
                    return root.getJSONArray("mechinot").length();
                } catch (Exception e) { return 0; }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("FIREBASE_ERROR", error.getMessage());
            }
        });
    }

    private void setupListeners() {
        spMechinot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadBranchesForMechina(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spBranches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUIFromSelectedBranch(spMechinot.getSelectedItemPosition(), position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualEntryDialog();
            }
        });

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveEventToFirebase());
    }

    private void loadBranchesForMechina(int mechinaPos) {
        if (mechinaPos >= allMechinotEvents.size()) return;

        int jsonSize = getJSONMechinotCount();
        List<String> branchNames = new ArrayList<>();

        if (mechinaPos < jsonSize) {
            // מקרה א': המכינה מגיעה מה-JSON - טוענים את כל השלוחות שלה מהקובץ
            try {
                JSONObject root = new JSONObject(loadJSONFromAsset());
                JSONArray branchesArray = root.getJSONArray("mechinot")
                        .getJSONObject(mechinaPos)
                        .getJSONArray("branches");

                for (int i = 0; i < branchesArray.length(); i++) {
                    branchNames.add(branchesArray.getJSONObject(i).getString("branchName"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            // מקרה ב': המכינה מ-Firebase - היא בעצמה מייצגת שלוחה ספציפית
            MechinaEvent selectedMechina = allMechinotEvents.get(mechinaPos);
            String branchName = selectedMechina.getBranch();

            if (branchName == null || branchName.isEmpty()) {
                branchNames.add("שלוחה ראשית"); // ברירת מחדל אם לא הוזן שם שלוחה
            } else {
                branchNames.add(branchName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, branchNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBranches.setAdapter(adapter);
    }
    private void updateUIFromSelectedBranch(int mechinaPos, int branchPos) {
        int jsonSize = getJSONMechinotCount();

        if (mechinaPos < jsonSize) {
            // נתונים מה-JSON (הקוד הקיים שלך)
            try {
                JSONObject root = new JSONObject(loadJSONFromAsset());
                JSONObject mechinaObj = root.getJSONArray("mechinot").getJSONObject(mechinaPos);
                JSONObject branchObj = mechinaObj.getJSONArray("branches").getJSONObject(branchPos);

                etMechinaName.setText(mechinaObj.getString("name"));
                etBranch.setText(branchObj.getString("branchName"));
                etAddress.setText(branchObj.getString("location"));

                showLocationOnMap(new GeoPoint(branchObj.getDouble("lat"), branchObj.getDouble("lng")));
            } catch (Exception e) { Log.e("MAP_UPDATE", "Error", e); }
        } else {
            // נתונים מ-Firebase
            MechinaEvent selected = allMechinotEvents.get(mechinaPos);
            etMechinaName.setText(selected.getName());
            etBranch.setText(selected.getBranch());
            etAddress.setText(selected.getAddress());

            showLocationOnMap(new GeoPoint(selected.getLat(), selected.getLng()));
        }
    }
    private int getJSONMechinotCount() {
        try {
            String json = loadJSONFromAsset();
            if (json == null) return 0;
            JSONObject root = new JSONObject(json);
            return root.getJSONArray("mechinot").length();
        } catch (Exception e) {
            return 0;
        }
    }
    private void showLocationOnMap(GeoPoint geoPoint) {
        if (mMap == null || geoPoint == null || !geoPoint.isValid()) return;
        LatLng location = new LatLng(geoPoint.latitude, geoPoint.longitude);
        mMap.clear();

        String title = etMechinaName.getText().toString() + " - " + etBranch.getText().toString();
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("mechinot.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            btnPickDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEventToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = etMechinaName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || selectedDate.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        getCoordinatesFromAddress(address, new IGeocodeCallback() {
            @Override
            public void onFinished(GeoPoint point) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");
                String id = ref.push().getKey();
                MechinaEvent event = new MechinaEvent(id, name, branch, selectedDate, address, point.latitude, point.longitude);
                ref.child(id).setValue(event).addOnSuccessListener(aVoid -> finish());
            }
            @Override
            public void onFailure(String err) { Toast.makeText(CreateMechinaActivity.this, err, Toast.LENGTH_SHORT).show(); }
        });
    }

    private void getCoordinatesFromAddress(String addressString, IGeocodeCallback callback) {
        Geocoder geocoder = new Geocoder(this, new Locale("he"));
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString + ", ישראל", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address loc = addresses.get(0);
                    runOnUiThread(() -> callback.onFinished(new GeoPoint(loc.getLatitude(), loc.getLongitude())));
                } else {
                    runOnUiThread(() -> callback.onFailure("כתובת לא נמצאה"));
                }
            } catch (IOException e) {
                runOnUiThread(() -> callback.onFailure("שגיאת רשת"));
            }
        }).start();
    }

    private void showManualEntryDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        final EditText inputName = new EditText(this);
        inputName.setHint("שם המכינה");
        inputName.setTextDirection(View.TEXT_DIRECTION_RTL);
        inputName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        layout.addView(inputName);

        final EditText inputBranch = new EditText(this);
        inputBranch.setHint("שלוחה");
        inputBranch.setTextDirection(View.TEXT_DIRECTION_RTL);
        inputBranch.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        layout.addView(inputBranch);

        final EditText inputAddress = new EditText(this);
        inputAddress.setHint("כתובת (רחוב מספר, עיר)");
        inputAddress.setTextDirection(View.TEXT_DIRECTION_RTL);
        inputAddress.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        layout.addView(inputAddress);

        final Button btnDateInDialog = new Button(this);
        btnDateInDialog.setText("בחר תאריך למכינה");
        btnDateInDialog.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = day + "/" + (month + 1) + "/" + year;
                btnDateInDialog.setText("תאריך: " + selectedDate);
                btnPickDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnDateInDialog);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("הוספת מכינה ידנית")
                .setView(layout)
                .setPositiveButton("אישור", (dialogInterface, i) -> {
                    String name = inputName.getText().toString().trim();
                    String branch = inputBranch.getText().toString().trim();
                    String address = inputAddress.getText().toString().trim();

                    if (selectedDate.isEmpty()) {
                        Toast.makeText(this, "אנא בחר תאריך", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (name.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "חובה למלא שם וכתובת", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    etMechinaName.setText(name);
                    etBranch.setText(branch);
                    etAddress.setText(address);

                    getCoordinatesFromAddress(address, new IGeocodeCallback() {
                        @Override
                        public void onFinished(GeoPoint point) {
                            showLocationOnMap(point);
                            saveManualEventToFirebase(name, branch, address, point);
                        }
                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(CreateMechinaActivity.this, "לא מצאנו את הכתובת, נשמר ללא מיקום", Toast.LENGTH_SHORT).show();
                            saveManualEventToFirebase(name, branch, address, new GeoPoint(0,0));
                        }
                    });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }


    private void saveManualEventToFirebase(String name, String branch, String address, GeoPoint point) {
        DatabaseReference newMechinaRef = FBRef.mechinotRef.push();
        String uniqueKey = newMechinaRef.getKey();

        MechinaEvent event = new MechinaEvent(uniqueKey, name, branch, "", address, point.latitude, point.longitude);

        if (uniqueKey != null) {
            newMechinaRef.setValue(event)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateMechinaActivity.this, "המכינה נוספה למאגר הכללי בהצלחה!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateMechinaActivity.this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}