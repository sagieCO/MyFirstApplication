package com.sagie.myfirstapplication.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.models.GeoPoint;
import com.sagie.myfirstapplication.IGeocodeCallback;
import com.sagie.myfirstapplication.models.MechinaEvent;
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
    private Button btnPickDate, btnSave, btnManualEntry, btnPickTime;
    private Spinner spMechinot, spBranches, spColorPicker;

    private String selectedDate = "";
    private String selectedTime = "";
    private String selectedColor = "#3F51B5"; // ברירת מחדל
    private GoogleMap mMap;
    private List<MechinaEvent> allMechinotEvents = new ArrayList<>();

    // נתוני צבעים קבועים
    private final String[] colorNames = {"כחול", "ירוק", "אדום", "כתום"};
    private final String[] colorHex = {"#3F51B5", "#4CAF50", "#F44336", "#FF9800"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_create_mechina);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initViews();
        initMap();
        setupSpinners();
        setupListeners();
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    private void initViews() {
        etMechinaName = findViewById(R.id.etMechinaName);
        etBranch = findViewById(R.id.etBranch);
        etAddress = findViewById(R.id.etAddress);
        btnManualEntry = findViewById(R.id.btnManualEntry);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        spMechinot = findViewById(R.id.spMechinot);
        spBranches = findViewById(R.id.spBranches);
        spColorPicker = findViewById(R.id.spColorPicker);

        // הגדרת כיווניות עברית
        etMechinaName.setTextDirection(View.TEXT_DIRECTION_RTL);
        etBranch.setTextDirection(View.TEXT_DIRECTION_RTL);
        etAddress.setTextDirection(View.TEXT_DIRECTION_RTL);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> mMap = googleMap);
        }
    }

    private void setupSpinners() {
        // 1. הגדרת Spinner צבעים
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spColorPicker.setAdapter(colorAdapter);

        // 2. טעינת רשימת המכינות
        loadAllMechinotData();
    }

    private void loadAllMechinotData() {
        allMechinotEvents.clear();

        // טעינה מה-JSON
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString != null) {
                JSONObject root = new JSONObject(jsonString);
                JSONArray mechinotArray = root.getJSONArray("mechinot");
                for (int i = 0; i < mechinotArray.length(); i++) {
                    JSONObject obj = mechinotArray.getJSONObject(i);
                    MechinaEvent m = new MechinaEvent();
                    m.setName(obj.getString("name"));
                    allMechinotEvents.add(m);
                }
            }
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Error loading JSON", e);
        }

        // טעינה מה-Firebase (הוספה לרשימה הקיימת)
        FBRef.mechinotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int jsonSize = getJSONMechinotCount();
                if (allMechinotEvents.size() > jsonSize) {
                    allMechinotEvents.subList(jsonSize, allMechinotEvents.size()).clear();
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent manualMechina = ds.getValue(MechinaEvent.class);
                    if (manualMechina != null) {
                        allMechinotEvents.add(manualMechina);
                    }
                }
                updateMainSpinnerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", error.getMessage());
            }
        });
    }

    private void updateMainSpinnerAdapter() {
        List<String> displayNames = new ArrayList<>();
        for (MechinaEvent m : allMechinotEvents) {
            displayNames.add(m.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMechinot.setAdapter(adapter);
    }

    private void setupListeners() {
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveEventToFirebase());
        btnManualEntry.setOnClickListener(v -> showManualEntryDialog());

        spColorPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedColor = colorHex[position];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spMechinot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadBranchesForMechina(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spBranches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUIFromSelectedBranch(spMechinot.getSelectedItemPosition(), position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadBranchesForMechina(int mechinaPos) {
        if (mechinaPos >= allMechinotEvents.size()) return;

        int jsonSize = getJSONMechinotCount();
        List<String> branchNames = new ArrayList<>();

        if (mechinaPos < jsonSize) {
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
            MechinaEvent selectedMechina = allMechinotEvents.get(mechinaPos);
            branchNames.add(selectedMechina.getBranch() != null ? selectedMechina.getBranch() : "שלוחה ראשית");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBranches.setAdapter(adapter);
    }

    private void updateUIFromSelectedBranch(int mechinaPos, int branchPos) {
        int jsonSize = getJSONMechinotCount();
        if (mechinaPos < jsonSize) {
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
            MechinaEvent selected = allMechinotEvents.get(mechinaPos);
            etMechinaName.setText(selected.getName());
            etBranch.setText(selected.getBranch());
            etAddress.setText(selected.getAddress());
            showLocationOnMap(new GeoPoint(selected.getLat(), selected.getLng()));
        }
    }

    private void saveEventToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = etMechinaName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל הפרטים ובחר תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        getCoordinatesFromAddress(address, new IGeocodeCallback() {
            @Override
            public void onFinished(GeoPoint point) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");
                String id = ref.push().getKey();

                // יצירת האירוע עם השדה החדש של הצבע (11 פרמטרים)
                MechinaEvent event = new MechinaEvent(id, name, branch, selectedDate, selectedTime,
                        address, point.latitude, point.longitude, "", "", selectedColor);

                if (id != null) {
                    ref.child(id).setValue(event).addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateMechinaActivity.this, "האירוע נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
            @Override
            public void onFailure(String err) {
                Toast.makeText(CreateMechinaActivity.this, "שגיאת מיקום: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- פונקציות עזר קיימות ---

    private void showLocationOnMap(GeoPoint geoPoint) {
        if (mMap == null || geoPoint == null || !geoPoint.isValid()) return;
        LatLng location = new LatLng(geoPoint.latitude, geoPoint.longitude);
        mMap.clear();
        String title = etMechinaName.getText().toString() + " - " + etBranch.getText().toString();
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
    }

    public String loadJSONFromAsset() {
        try {
            InputStream is = getAssets().open("mechinot.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private int getJSONMechinotCount() {
        try {
            JSONObject root = new JSONObject(loadJSONFromAsset());
            return root.getJSONArray("mechinot").length();
        } catch (Exception e) { return 0; }
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            btnPickTime.setText("שעה: " + selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate = d + "/" + (m + 1) + "/" + y;
            btnPickDate.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
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
        layout.addView(inputName);

        final EditText inputBranch = new EditText(this);
        inputBranch.setHint("שלוחה");
        layout.addView(inputBranch);

        final EditText inputAddress = new EditText(this);
        inputAddress.setHint("כתובת (רחוב מספר, עיר)");
        layout.addView(inputAddress);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("הוספת מכינה ידנית")
                .setView(layout)
                .setPositiveButton("אישור", (dialogInterface, i) -> {
                    String name = inputName.getText().toString().trim();
                    String branch = inputBranch.getText().toString().trim();
                    String address = inputAddress.getText().toString().trim();

                    if (name.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "חובה למלא שם וכתובת", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getCoordinatesFromAddress(address, new IGeocodeCallback() {
                        @Override
                        public void onFinished(GeoPoint point) {
                            saveManualMechinaToRepo(name, branch, address, point);
                        }
                        @Override
                        public void onFailure(String errorMessage) {
                            saveManualMechinaToRepo(name, branch, address, new GeoPoint(0,0));
                        }
                    });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void saveManualMechinaToRepo(String name, String branch, String address, GeoPoint point) {
        DatabaseReference newMechinaRef = FBRef.mechinotRef.push();
        String uniqueKey = newMechinaRef.getKey();
        // שמירת מכינה במאגר הכללי (ללא תאריך, אך עם צבע ברירת מחדל אם נדרש)
        MechinaEvent event = new MechinaEvent(uniqueKey, name, branch, "", "", address, point.latitude, point.longitude, "", "", selectedColor);
        if (uniqueKey != null) {
            newMechinaRef.setValue(event).addOnSuccessListener(aVoid ->
                    Toast.makeText(CreateMechinaActivity.this, "המכינה נוספה למאגר!", Toast.LENGTH_SHORT).show());
        }
    }
}