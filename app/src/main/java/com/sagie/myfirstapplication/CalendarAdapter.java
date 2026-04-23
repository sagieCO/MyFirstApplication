package com.sagie.myfirstapplication;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // ניקוי הקונטיינר למניעת כפילויות
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
                    String display = (event.time != null && !event.time.isEmpty() ? event.time + " " : "") + event.mechinaName;

                    tvEvent.setText(display);
                    tvEvent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                    tvEvent.setTextColor(Color.WHITE);
                    tvEvent.setBackgroundResource(R.drawable.event_item_bg);
                    tvEvent.setPadding(4, 1, 4, 1);
                    tvEvent.setLines(1);
                    tvEvent.setEllipsize(TextUtils.TruncateAt.END);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 1, 0, 1);
                    holder.eventsContainer.addView(tvEvent, params);
                }
            }

            // --- 3. הגדרת לחיצה על כל שטח המשבצת ---
            holder.container.setOnClickListener(v -> {
                showDayDetailsDialog(holder.itemView.getContext(), day);
            });
        }
    }

    private void showDayDetailsDialog(android.content.Context context, Day day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("אירועים לתאריך " + day.dayNumber + " בחודש");

        if (day.dayEvents == null || day.dayEvents.isEmpty()) {
            builder.setMessage("אין אירועים רשומים ליום זה.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (MechinaEvent event : day.dayEvents) {
                sb.append("🕒 ").append(event.time != null ? event.time : "--:--")
                        .append(" - ").append(event.mechinaName).append("\n");

                if (event.branch != null && !event.branch.isEmpty()) {
                    sb.append("   שלוחה: ").append(event.branch).append("\n");
                }
                sb.append("   📍 ").append(event.address).append("\n\n");
            }
            builder.setMessage(sb.toString());
        }

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