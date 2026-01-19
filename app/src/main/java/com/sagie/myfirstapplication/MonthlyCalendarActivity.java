package com.sagie.myfirstapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.Activities.BaseActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyCalendarActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CalendarAdapter adapter;
    private List<Day> days;
    private DatabaseReference eventsRef;
    private ValueEventListener eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_monthly_calendar);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        recyclerView = findViewById(R.id.calendarRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        days = new ArrayList<>();
        loadMonth();

        adapter = new CalendarAdapter(days);
        recyclerView.setAdapter(adapter);

        startListeningForEvents();
    }
    private void startListeningForEvents() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        eventsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");

        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ניקוי אירועים קודמים לפני עדכון
                for (Day d : days) d.dayEvents.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    MechinaEvent event = data.getValue(MechinaEvent.class);
                    if (event != null && event.date != null) {
                        try {
                            int eventDay = Integer.parseInt(event.date.split("/")[0]);
                            // מציאת היום הנכון ברשימה לפי המספר שלו
                            for (Day d : days) {
                                if (d.isCurrentMonth && d.dayNumber == eventDay) {
                                    d.dayEvents.add(event);
                                    break;
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyCalendarActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        };
        eventsRef.addValueEventListener(eventsListener);
    }

    private void loadMonth() {
        days.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // הורדנו את המשתנה firstDayOfWeek ואת הלולאה שהוסיפה ימים ריקים

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // החודש יתחיל תמיד מהתיבה הראשונה ב-RecyclerView
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(new Day(i, true));
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsRef != null && eventsListener != null) {
            eventsRef.removeEventListener(eventsListener);
        }
    }
}