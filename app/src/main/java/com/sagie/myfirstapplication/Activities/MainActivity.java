package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.ExploreAdapter;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.models.MechinaEvent;
import com.sagie.myfirstapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mAuth;

    private String currentRegionFilter = null; // "צפון", "דרום", "מרכז" או null
    private List<MechinaEvent> filteredExploreList = new ArrayList<>();
    private List<MechinaEvent> originalMechinotList = new ArrayList<>(); // רשימת המקור של כל המכינות מה־JSON
    private DatabaseReference eventsRef;
    private ValueEventListener eventsListener;

    private DatabaseReference userNameRef;
    private ValueEventListener userNameListener;

    private ValueEventListener manualMechinotListener;

    private TextView tvNextMechinaName, tvNextDate, tv_welcome;
    private Button btnShowAllStations;

    private List<MechinaEvent> allExploreList = new ArrayList<>();
    private ExploreAdapter exploreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mAuth = FirebaseAuth.getInstance();

        // קישור UI
        tvNextMechinaName = findViewById(R.id.tv_next_mechina_name);
        tvNextDate = findViewById(R.id.tv_next_date);
        btnShowAllStations = findViewById(R.id.btnShowAllStations);
        ImageButton btnGoToCalendarIcon = findViewById(R.id.btnGoToCalendarIcon);
        tv_welcome = findViewById(R.id.tv_welcome);

        // טעינת נתוני המשתמש (שם) בהאזנה רציפה
        loadHeaderData();

        setupRecyclerView();
        loadCombinedData();
        setupFilters();
        btnGoToCalendarIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MonthlyCalendarActivity.class)));
        btnShowAllStations.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MonthlyCalendarActivity.class)));

        if (mAuth.getCurrentUser() != null) {
            startListeningForNextEvent();
        }
    }

    private void loadHeaderData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            tv_welcome.setText("שלום, אורח");
            return;
        }

        String uid = user.getUid();

        // הגדרת הרפרנס לשם המשתמש
        userNameRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("name");

        // הגדרת המאזין הרציף (Real-time)
        userNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        // עדכון הברכה במסך הראשי
                        tv_welcome.setText("היי " + name + ", מה התוכניות?");
                        // עדכון השם בתפריט הצד (navName הוגדר ב-BaseActivity)
                        if (navName != null) {
                            navName.setText("שלום, " + name);
                        }
                    }
                } else {
                    tv_welcome.setText("היי, מה התוכניות?");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בעדכון השם", Toast.LENGTH_SHORT).show();
            }
        };

        // הפעלת ההאזנה
        userNameRef.addValueEventListener(userNameListener);
    }

    private void setupRecyclerView() {
        RecyclerView rvExplore = findViewById(R.id.rv_mechinot_list);
        rvExplore.setLayoutManager(new LinearLayoutManager(this));
        rvExplore.setNestedScrollingEnabled(false);

        exploreAdapter = new ExploreAdapter(allExploreList, event -> {
            showMechinaDialog(event);
        });        rvExplore.setAdapter(exploreAdapter);
    }

    private void loadCombinedData() {
        allExploreList.clear();
        originalMechinotList.clear();

        List<MechinaEvent> fromJson = getMechinotFromJson();
        allExploreList.addAll(fromJson);
        originalMechinotList.addAll(fromJson); // שמירה של המקור

        manualMechinotListener = FBRef.mechinotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allExploreList.clear();
                allExploreList.addAll(originalMechinotList); // נטען קודם את המקור

                for (DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent manualMechina = ds.getValue(MechinaEvent.class);
                    if (manualMechina != null) {
                        allExploreList.add(manualMechina);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת נתונים מהענן", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void applyFilters() {

        // אם אין פילטר → מציגים הכל
        if (currentRegionFilter == null) {
            exploreAdapter.updateList(new ArrayList<>(allExploreList));
            return;
        }

        filteredExploreList.clear();

        for (MechinaEvent event : allExploreList) {

            // אם זה Firebase ואין לו region → תמיד להציג
            if (event.region == null||event.region.trim().isEmpty()) {
                filteredExploreList.add(event);
                continue;
            }

            // ניקוי רווחים (מאוד חשוב!)
            String eventRegion = event.region.trim();
            String filterRegion = currentRegionFilter.trim();

            if (eventRegion.equals(filterRegion)) {
                filteredExploreList.add(event);
            }
        }

        exploreAdapter.updateList(filteredExploreList);
    }

    private void setupFilters() {
        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        int count = chipGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View chipView = chipGroup.getChildAt(i);

            if (chipView instanceof Chip) {
                chipView.setOnClickListener(v -> {
                    Chip chip = (Chip) v;
                    String text = chip.getText().toString();

                    if (text.equals("צפון") || text.equals("דרום")) {
                        if (text.equals(currentRegionFilter)) {
                            currentRegionFilter = null;
                        } else {
                            currentRegionFilter = text;
                        }
                    }

                    else if (text.equals("הכל")) {
                        currentRegionFilter = null;
                    }

                    else if (text.equals("פופולריות")) {
                        currentRegionFilter = null; // בעתיד תעשה מיון
                    }

                    applyFilters();
                });
            }
        }
    }

    private List<MechinaEvent> getMechinotFromJson() {
        List<MechinaEvent> list = new ArrayList<>();
        String jsonString = getJsonFromAssets();
        if (jsonString == null) return list;

        try {
            JSONObject mainObject = new JSONObject(jsonString);
            JSONArray mechinotArray = mainObject.getJSONArray("mechinot");

            for (int i = 0; i < mechinotArray.length(); i++) {
                JSONObject mechinaJson = mechinotArray.getJSONObject(i);
                String name = mechinaJson.getString("name");
                JSONArray branches = mechinaJson.getJSONArray("branches");

                for (int j = 0; j < branches.length(); j++) {
                    JSONObject branchObj = branches.getJSONObject(j);
                    MechinaEvent event = new MechinaEvent();
                    event.mechinaName = name;
                    event.branch = branchObj.getString("branchName");
                    event.address = branchObj.getString("location");
                    event.lat = branchObj.getDouble("lat");
                    event.lng = branchObj.getDouble("lng");

                    // <-- כאן אנחנו מאתחלים את השדות לפילטר
                    event.region = branchObj.getString("region");
                    event.religiousType = branchObj.getString("religiousType");

                    list.add(event);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getJsonFromAssets() {
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

    private void startListeningForNextEvent() {
        String uid = mAuth.getCurrentUser().getUid();
        eventsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");

        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date today = new Date();

                    MechinaEvent closestEvent = null;
                    Date closestDate = null;

                    for (DataSnapshot data : snapshot.getChildren()) {
                        MechinaEvent event = data.getValue(MechinaEvent.class);

                        if (event != null && event.date != null) {
                            try {
                                Date eventDate = sdf.parse(event.date);

                                if (eventDate != null && eventDate.after(today)) {

                                    if (closestDate == null || eventDate.before(closestDate)) {
                                        closestDate = eventDate;
                                        closestEvent = event;
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (closestEvent != null) {
                        tvNextMechinaName.setText(closestEvent.mechinaName);
                        tvNextDate.setText(closestEvent.date);
                    } else {
                        tvNextMechinaName.setText("אין אירועים עתידיים");
                        tvNextDate.setText("--/--/----");
                    }
                } else {
                    tvNextMechinaName.setText("אין מכינות רשומות");
                    tvNextDate.setText("לחץ על גילוי כדי להתחיל");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        eventsRef.addValueEventListener(eventsListener);
    }
    private void showMechinaDialog(MechinaEvent mechinaEvent) {
        // יצירת דיאלוג
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mechina_details, null);
        builder.setView(dialogView);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // קישור רכיבים
        TextView tvMechinaName = dialogView.findViewById(R.id.tvMechinaNameDialog);
        TextView tvBranchInfo = dialogView.findViewById(R.id.tvBranchInfoDialog);
        TextView tvReviews = dialogView.findViewById(R.id.tvReviewsDialog);
        Button btnAddReview = dialogView.findViewById(R.id.btnAddReview);
        Button btnViewAllReviews = dialogView.findViewById(R.id.btnViewAllReviews);

        // מילוי הנתונים
        tvMechinaName.setText(mechinaEvent.mechinaName + " - " + mechinaEvent.branch);
        tvBranchInfo.setText("סוג דתיות: " + mechinaEvent.religiousType + "\nאזור: " + mechinaEvent.region);
        tvReviews.setText("ביקורות קצרות: \n- ביקורת 1\n- ביקורת 2\n- ביקורת 3");

        // כפתור הוספת ביקורת
        btnAddReview.setOnClickListener(v -> {
            dialog.dismiss();
            // כאן תוכל לפתוח Activity או Dialog נוסף להוספת ביקורת
            Toast.makeText(this, "הוספת ביקורת...", Toast.LENGTH_SHORT).show();
        });

        // כפתור צפייה בכל הביקורות
        btnViewAllReviews.setOnClickListener(v -> {
            dialog.dismiss();
            // כאן תוכל לפתוח Activity חדש שמציג את כל הביקורות
            Toast.makeText(this, "צפייה בכל הביקורות...", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // הסרת כל המאזינים למניעת Memory Leaks
        if (eventsRef != null && eventsListener != null) {
            eventsRef.removeEventListener(eventsListener);
        }
        if (FBRef.mechinotRef != null && manualMechinotListener != null) {
            FBRef.mechinotRef.removeEventListener(manualMechinotListener);
        }
        if (userNameRef != null && userNameListener != null) {
            userNameRef.removeEventListener(userNameListener);
        }
    }

}