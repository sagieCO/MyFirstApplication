package com.sagie.myfirstapplication;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.Activities.MonthlyCalendarActivity;
import com.sagie.myfirstapplication.models.AlarmReciever;
import com.sagie.myfirstapplication.models.Day;
import com.sagie.myfirstapplication.models.MechinaEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<Day> days;

    public CalendarAdapter(List<Day> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Day day = days.get(position);

        // ניקוי הקונטיינר למניעת כפילויות של אירועים במיחזור של ה-ViewHolder
        holder.eventsContainer.removeAllViews();

        if (day.dayNumber == 0) {
            holder.tvDayNumber.setText("");
            holder.container.setBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.container.setOnClickListener(null);
        } else {
            holder.tvDayNumber.setText(String.valueOf(day.dayNumber));
            holder.container.setBackgroundColor(day.isCurrentMonth ? Color.WHITE : Color.parseColor("#EEEEEE"));

            // סימון היום הנוכחי
            if (day.isToday) {
                holder.tvDayNumber.setBackgroundResource(R.drawable.today_circle);
                holder.tvDayNumber.setTextColor(Color.WHITE);
            } else {
                holder.tvDayNumber.setBackground(null);
                holder.tvDayNumber.setTextColor(Color.BLACK);
            }

            // --- 1. מיון האירועים לפי שעה (מהמוקדם למאוחר) ---
            if (day.dayEvents != null && day.dayEvents.size() > 1) {
                Collections.sort(day.dayEvents, new Comparator<MechinaEvent>() {
                    @Override
                    public int compare(MechinaEvent e1, MechinaEvent e2) {
                        String t1 = (e1.time == null || e1.time.isEmpty()) ? "23:59" : e1.time;
                        String t2 = (e2.time == null || e2.time.isEmpty()) ? "23:59" : e2.time;
                        return t1.compareTo(t2);
                    }
                });
            }

            // --- 2. הצגת אירועים בתצוגה מקוצרת במשבצת ---
            if (day.dayEvents != null && !day.dayEvents.isEmpty()) {
                for (MechinaEvent event : day.dayEvents) {
                    TextView tvEvent = new TextView(holder.itemView.getContext());

                    // פורמט תצוגה: "שעה שם המכינה"
                    String display = (event.time != null && !event.time.isEmpty() ? event.time + " " : "") + event.mechinaName;
                    tvEvent.setText(display);
                    tvEvent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                    tvEvent.setTextColor(Color.WHITE);
                    tvEvent.setPadding(6, 2, 6, 2);
                    tvEvent.setLines(1);
                    tvEvent.setEllipsize(TextUtils.TruncateAt.END);

                    // --- עדכון הצבע הדינמי ---
                    // יצירת רקע עם פינות מעוגלות וצבע מה-DB
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setCornerRadius(8f); // רדיוס הפינות

                    // שימוש בצבע מה-Database, אם לא קיים נשתמש בצבע ברירת מחדל
                    int eventColor;
                    try {
                        if (event.eventColor != null && !event.eventColor.isEmpty()) {
                            eventColor = Color.parseColor(event.eventColor);
                        } else {
                            eventColor = Color.parseColor("#3F51B5"); // כחול ברירת מחדל
                        }
                    } catch (Exception e) {
                        eventColor = Color.GRAY; // הגנה במקרה של פורמט לא תקין
                    }
                    shape.setColor(eventColor);
                    tvEvent.setBackground(shape);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(2, 1, 2, 1); // רווח קטן בין אירועים

                    holder.eventsContainer.addView(tvEvent, params);
                }
            }

            // --- 3. הגדרת לחיצה על כל שטח המשבצת ---
            holder.container.setOnClickListener(v -> {
                showDayDetailsDialog(holder.itemView.getContext(), day);
            });
        }
    }

    private void showDayDetailsDialog(Context context, Day day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("אירועים לתאריך " + day.dayNumber);

        // קונטיינר ראשי עם ריווח
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(50, 40, 50, 40);

        if (day.dayEvents == null || day.dayEvents.isEmpty()) {
            TextView emptyTv = new TextView(context);
            emptyTv.setText("אין אירועים רשומים ליום זה.");
            emptyTv.setGravity(Gravity.CENTER);
            rootLayout.addView(emptyTv);
        } else {
            for (MechinaEvent event : day.dayEvents) {
                // Layout לכל שורה של אירוע
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.VERTICAL); // שיניתי לורטיקלי כדי שהכפתור יהיה מתחת או לצד בצורה מסודרת
                row.setPadding(0, 20, 0, 20);

                // טקסט פרטי האירוע
                TextView tvInfo = new TextView(context);
                String info = "🕒 " + (event.time != null ? event.time : "--:--") + " - " + event.mechinaName;
                if (event.branch != null && !event.branch.isEmpty()) {
                    info += "\nשלוחה: " + event.branch;
                }
                tvInfo.setText(info);
                tvInfo.setTextColor(Color.BLACK);
                tvInfo.setTextSize(16);
                row.addView(tvInfo);

                // יצירת כפתור מחיקה אמיתי
                android.widget.Button btnDelete = new android.widget.Button(context);
                btnDelete.setText("מחיקת אירוע");
                btnDelete.setBackgroundColor(Color.parseColor("#FF5252")); // צבע אדום
                btnDelete.setTextColor(Color.WHITE);

                // הגדרת מרחק לכפתור
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                buttonParams.setMargins(0, 10, 0, 10);
                btnDelete.setLayoutParams(buttonParams);

                // לחיצה על כפתור המחיקה
                btnDelete.setOnClickListener(v -> {
                    if (context instanceof MonthlyCalendarActivity) {
                        // הצגת דיאלוג אישור לפני מחיקה (מומלץ)
                        new AlertDialog.Builder(context)
                                .setTitle("אישור מחיקה")
                                .setMessage("האם אתה בטוח שברצונך למחוק את " + event.mechinaName + "?")
                                .setPositiveButton("כן, מחק", (d, which) -> {
                                    ((MonthlyCalendarActivity) context).deleteEvent(event);
                                })
                                .setNegativeButton("ביטול", null)
                                .show();
                    }
                });

                row.addView(btnDelete);
                rootLayout.addView(row);

                // קו מפריד בין אירועים
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(Color.LTGRAY);
                rootLayout.addView(divider);
            }
        }

        builder.setView(rootLayout);
        builder.setPositiveButton("סגור", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public int getItemCount() { return days.size(); }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        LinearLayout eventsContainer, container;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            eventsContainer = itemView.findViewById(R.id.eventsContainer);
            container = itemView.findViewById(R.id.linearLayoutContainer);
        }
    }
}