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
    private Button btnPickDate, btnSave;
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

    private void setupSpinner() {
        List<String> mechinaNames = new ArrayList<>();
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString == null) return;

            JSONObject root = new JSONObject(jsonString);
            JSONArray mechinotArray = root.getJSONArray("mechinot");

            for (int i = 0; i < mechinotArray.length(); i++) {
                mechinaNames.add(mechinotArray.getJSONObject(i).getString("name"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, mechinaNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMechinot.setAdapter(adapter);

        } catch (Exception e) {
            Log.e("JSON_ERROR", "Error loading mechinot", e);
        }

        findViewById(R.id.btnManualEntry).setOnClickListener(v -> showManualEntryDialog());
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

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveEventToFirebase());
    }

    private void loadBranchesForMechina(int mechinaPos) {
        try {
            JSONObject root = new JSONObject(loadJSONFromAsset());
            JSONArray branchesArray = root.getJSONArray("mechinot")
                    .getJSONObject(mechinaPos)
                    .getJSONArray("branches");

            List<String> branchNames = new ArrayList<>();
            for (int i = 0; i < branchesArray.length(); i++) {
                branchNames.add(branchesArray.getJSONObject(i).getString("branchName"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, branchNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spBranches.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUIFromSelectedBranch(int mechinaPos, int branchPos) {
        try {
            String jsonString = loadJSONFromAsset();
            JSONObject root = new JSONObject(jsonString);
            JSONArray mechinotArray = root.getJSONArray("mechinot");

            JSONObject mechina = mechinotArray.getJSONObject(mechinaPos);
            JSONObject branch = mechina.getJSONArray("branches").getJSONObject(branchPos);

            String address = branch.getString("location");
            String bName = branch.getString("branchName");
            String mName = mechina.getString("name");

            etAddress.setText(address);
            etBranch.setText(bName);
            etMechinaName.setText(mName);

            double lat = branch.getDouble("lat");
            double lng = branch.getDouble("lng");

            showLocationOnMap(new GeoPoint(lat, lng));

        } catch (Exception e) {
            Log.e("MAP_UPDATE", "Error updating map", e);
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

    /**
     * פונקציה לשמירת מכינה ידנית תחת ענף mechinot נפרד
     */
    private void saveManualEventToFirebase(String name, String branch, String address, GeoPoint point) {
        String uniqueKey = FBRef.mechinotRef.getKey();

        // יצירת אובייקט המכינה
        MechinaEvent event = new MechinaEvent(uniqueKey, name, branch, selectedDate, address, point.latitude, point.longitude);

        if (uniqueKey != null) {
            FBRef.mechinotRef.child(uniqueKey).setValue(event)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateMechinaActivity.this, "המכינה נוספה לקטגוריית mechinot בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateMechinaActivity.this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}