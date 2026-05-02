package com.sagie.myfirstapplication.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.sagie.myfirstapplication.CalendarAdapter;
import com.sagie.myfirstapplication.R;
import com.sagie.myfirstapplication.models.AlarmReciever;
import com.sagie.myfirstapplication.models.Day;
import com.sagie.myfirstapplication.models.MechinaEvent;

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
    @Override
    protected boolean requiresAuthentication() {
        return true;
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
                // ניקוי האירועים הקיימים ברשימת הימים של לוח השנה
                for (Day d : days) d.dayEvents.clear();

                int currentMonthInt = selectedMonth.get(Calendar.MONTH) + 1;
                int currentYearInt = selectedMonth.get(Calendar.YEAR);

                for (DataSnapshot data : snapshot.getChildren()) {
                    MechinaEvent event = data.getValue(MechinaEvent.class);
                    if (event != null && event.date != null) {

                        // --- הוספת ההתראה למערכת ---
                        // אנחנו קוראים לזה כאן כדי שכל אירוע ב-Firebase יקבל התראה,
                        // בלי קשר לחודש שמוצג כרגע בלוח השנה.
                        setEventAlarm(event);

                        try {
                            String[] parts = event.date.split("/");
                            int eventDay = Integer.parseInt(parts[0]);
                            int eventMonth = Integer.parseInt(parts[1]);
                            int eventYear = Integer.parseInt(parts[2]);

                            // בדיקה אם האירוע שייך לחודש ולשנה שמוצגים כרגע ב-UI
                            if (eventMonth == currentMonthInt && eventYear == currentYearInt) {
                                for (Day d : days) {
                                    if (d.isCurrentMonth && d.dayNumber == eventDay) {
                                        d.dayEvents.add(event);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // עדכון ה-Adapter כדי להציג את הנקודות/אירועים על הלוח
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
    private void setEventAlarm(MechinaEvent event) {

        if (event.date == null || event.time == null) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Calendar eventTime = Calendar.getInstance();
            eventTime.setTime(sdf.parse(event.date + " " + event.time));

            Log.d("ALARM_DEBUG", "Event: " + event.mechinaName);
            Log.d("ALARM_DEBUG", "Event time: " + eventTime.getTime());

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            // 🔹 בדיקה אם הזמן עבר
            if (!eventTime.before(Calendar.getInstance())) {
                scheduleAlarm(event, eventTime.getTimeInMillis(), false, alarmManager);
            }

            // 🔹 24 שעות לפני
            Calendar before = (Calendar) eventTime.clone();
            before.add(Calendar.HOUR_OF_DAY, -24);

            // 🔥 לבדיקות (תבטל אחרי!)
            // before.add(Calendar.MINUTE, -1);

            if (!before.before(Calendar.getInstance())) {
                scheduleAlarm(event, before.getTimeInMillis(), true, alarmManager);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // פונקציית עזר כדי לא לשכפל קוד
    private void scheduleAlarm(MechinaEvent event,
                               long timeInMillis,
                               boolean is24hBefore,
                               AlarmManager alarmManager) {

        Intent intent = new Intent(this, AlarmReciever.class);
        intent.putExtra("eventName", event.mechinaName);
        intent.putExtra("is24hBefore", is24hBefore);

        int requestCode = (event.date + event.time).hashCode() + (is24hBefore ? 1 : 0);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d("ALARM_DEBUG", "Setting alarm at: " + timeInMillis);

        // 🔥 Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("ALARM_DEBUG", "Exact alarm NOT allowed!");
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                return;
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
        );
    }

    public void deleteEvent(MechinaEvent event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("my_events");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    MechinaEvent e = data.getValue(MechinaEvent.class);
                    if (e != null && e.mechinaName.equals(event.mechinaName) && e.date.equals(event.date)) {
                        data.getRef().removeValue().addOnSuccessListener(unused -> {
                            Toast.makeText(MonthlyCalendarActivity.this, "האירוע נמחק", Toast.LENGTH_SHORT).show();
                            cancelAlarm(event); // ביטול ההתראות בתוך ה-Activity
                        });
                        break;
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void cancelAlarm(MechinaEvent event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReciever.class);

        int requestCode = (event.date + event.time).hashCode();

        // ביטול התראה ראשית
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) alarmManager.cancel(pi);

        // ביטול התראת 24 שעות
        PendingIntent pi24 = PendingIntent.getBroadcast(this, requestCode + 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) alarmManager.cancel(pi24);
    }
}