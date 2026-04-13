package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private String currentRegionFilter = null;

    private List<MechinaEvent> allExploreList = new ArrayList<>();
    private List<MechinaEvent> originalMechinotList = new ArrayList<>();
    private List<MechinaEvent> filteredExploreList = new ArrayList<>();

    private ExploreAdapter exploreAdapter;
    private DatabaseReference eventsRef, userNameRef;
    private ValueEventListener eventsListener, userNameListener, manualMechinotListener;

    private TextView tvNextMechinaName, tvNextDate, tv_welcome;
    private Button btnShowAllStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_main);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mAuth = FirebaseAuth.getInstance();

        initView();
        loadHeaderData();
        setupRecyclerView();
        loadCombinedData();
        setupFilters();

        if (mAuth.getCurrentUser() != null) {
            startListeningForNextEvent();
        }
    }

    private void initView() {
        tvNextMechinaName = findViewById(R.id.tv_next_mechina_name);
        tvNextDate = findViewById(R.id.tv_next_date);
        btnShowAllStations = findViewById(R.id.btnShowAllStations);
        tv_welcome = findViewById(R.id.tv_welcome);
        ImageButton btnGoToCalendarIcon = findViewById(R.id.btnGoToCalendarIcon);

        // שינוי כאן: btnOpenChat לא נמצא פה, הוא ב-Adapter!

        btnGoToCalendarIcon.setOnClickListener(v -> startActivity(new Intent(this, MonthlyCalendarActivity.class)));
        btnShowAllStations.setOnClickListener(v -> startActivity(new Intent(this, MonthlyCalendarActivity.class)));
    }

    private void setupRecyclerView() {
        RecyclerView rvExplore = findViewById(R.id.rv_mechinot_list);
        rvExplore.setLayoutManager(new LinearLayoutManager(this));
        rvExplore.setNestedScrollingEnabled(false);

        // האדפטר כבר מטפל בלחיצות על הצ'אט בפנים
        exploreAdapter = new ExploreAdapter(allExploreList, this::showMechinaDialog);
        rvExplore.setAdapter(exploreAdapter);
    }

    private void loadHeaderData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            tv_welcome.setText("שלום, אורח");
            return;
        }
        userNameRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("name");
        userNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) {
                    tv_welcome.setText("היי " + name + ", מה התוכניות?");
                    if (navName != null) navName.setText("שלום, " + name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userNameRef.addValueEventListener(userNameListener);
    }

    private void loadCombinedData() {
        originalMechinotList = getMechinotFromJson();
        manualMechinotListener = FBRef.mechinotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MechinaEvent> combined = new ArrayList<>(originalMechinotList);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MechinaEvent manualMechina = ds.getValue(MechinaEvent.class);
                    if (manualMechina != null) combined.add(manualMechina);
                }
                allExploreList.clear();
                allExploreList.addAll(combined);
                applyFilters();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyFilters() {
        if (currentRegionFilter == null || currentRegionFilter.equals("הכל")) {
            exploreAdapter.updateList(new ArrayList<>(allExploreList));
            return;
        }
        filteredExploreList.clear();
        for (MechinaEvent event : allExploreList) {
            if (event.region != null && event.region.trim().equals(currentRegionFilter.trim())) {
                filteredExploreList.add(event);
            } else if (event.region == null || event.region.isEmpty()) {
                filteredExploreList.add(event);
            }
        }
        exploreAdapter.updateList(filteredExploreList);
    }

    private void setupFilters() {
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentRegionFilter = null;
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                String text = chip.getText().toString();
                currentRegionFilter = text.equals("הכל") ? null : text;
            }
            applyFilters();
        });
    }

    private List<MechinaEvent> getMechinotFromJson() {
        List<MechinaEvent> list = new ArrayList<>();
        try {
            InputStream is = getAssets().open("mechinot.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject mainObject = new JSONObject(json);
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
                    event.region = branchObj.optString("region", "");
                    event.religiousType = branchObj.optString("religiousType", "");
                    list.add(event);
                }
            }
        } catch (Exception e) { Log.e("JSON", "Error", e); }
        return list;
    }

    private void startListeningForNextEvent() {
        String uid = mAuth.getCurrentUser().getUid();
        eventsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");
        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date today = new Date();
                    MechinaEvent closest = null;
                    Date minDate = null;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MechinaEvent e = ds.getValue(MechinaEvent.class);
                        if (e != null && e.date != null) {
                            try {
                                Date d = sdf.parse(e.date);
                                if (d != null && d.after(today)) {
                                    if (minDate == null || d.before(minDate)) {
                                        minDate = d;
                                        closest = e;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    if (closest != null) {
                        tvNextMechinaName.setText(closest.mechinaName);
                        tvNextDate.setText(closest.date);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        eventsRef.addValueEventListener(eventsListener);
    }

    private void showMechinaDialog(MechinaEvent mechinaEvent) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mechina_details, null);
        builder.setView(dialogView);
        TextView tvMechinaName = dialogView.findViewById(R.id.tvMechinaNameDialog);
        TextView tvBranchInfo = dialogView.findViewById(R.id.tvBranchInfoDialog);
        tvMechinaName.setText(mechinaEvent.mechinaName + " - " + mechinaEvent.branch);
        tvBranchInfo.setText("סוג: " + mechinaEvent.religiousType + "\nאזור: " + mechinaEvent.region);
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsRef != null && eventsListener != null) eventsRef.removeEventListener(eventsListener);
        if (FBRef.mechinotRef != null && manualMechinotListener != null) FBRef.mechinotRef.removeEventListener(manualMechinotListener);
        if (userNameRef != null && userNameListener != null) userNameRef.removeEventListener(userNameListener);
    }
}