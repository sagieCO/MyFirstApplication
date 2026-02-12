package com.sagie.myfirstapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthlyCalendarActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CalendarAdapter adapter;
    private List<Day> days;
    private DatabaseReference eventsRef;
    private ValueEventListener eventsListener;
    private TextView tvMonthYear;
    private Calendar selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_monthly_calendar);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        recyclerView = findViewById(R.id.calendarRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        selectedMonth = Calendar.getInstance();
        days = new ArrayList<>();
        adapter = new CalendarAdapter(days);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnPreviousMonth).setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, -1);
            updateUI();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, 1);
            updateUI();
        });

        updateUI();
    }

    private void updateUI() {
        // שינוי שם החודש לעברית באמצעות SimpleDateFormat עם Locale עברי
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("he", "IL"));
        String monthName = sdf.format(selectedMonth.getTime());
        tvMonthYear.setText(monthName);

        loadMonth();
        startListeningForEvents();
    }

    private void loadMonth() {
        days.clear();
        Calendar calendar = (Calendar) selectedMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // לקיחת התאריך של היום האמיתי להשוואה
        Calendar today = Calendar.getInstance();

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(new Day(0, false));
        }

        for (int i = 1; i <= daysInMonth; i++) {
            Day day = new Day(i, true);

            // בדיקה: האם זה אותו יום, חודש ושנה כמו היום?
            if (today.get(Calendar.DAY_OF_MONTH) == i &&
                    today.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH) &&
                    today.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR)) {
                day.isToday = true;
            }

            days.add(day);
        }
        adapter.notifyDataSetChanged();
    }

    private void startListeningForEvents() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        if (eventsRef != null && eventsListener != null) {
            eventsRef.removeEventListener(eventsListener);
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("my_events");

        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Day d : days) d.dayEvents.clear();

                int currentMonthInt = selectedMonth.get(Calendar.MONTH) + 1;
                int currentYearInt = selectedMonth.get(Calendar.YEAR);

                for (DataSnapshot data : snapshot.getChildren()) {
                    MechinaEvent event = data.getValue(MechinaEvent.class);
                    if (event != null && event.date != null) {
                        try {
                            String[] parts = event.date.split("/");
                            int eventDay = Integer.parseInt(parts[0]);
                            int eventMonth = Integer.parseInt(parts[1]);
                            int eventYear = Integer.parseInt(parts[2]);

                            if (eventMonth == currentMonthInt && eventYear == currentYearInt) {
                                for (Day d : days) {
                                    if (d.isCurrentMonth && d.dayNumber == eventDay) {
                                        d.dayEvents.add(event);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyCalendarActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        };
        eventsRef.addValueEventListener(eventsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsRef != null && eventsListener != null) {
            eventsRef.removeEventListener(eventsListener);
        }
    }
}