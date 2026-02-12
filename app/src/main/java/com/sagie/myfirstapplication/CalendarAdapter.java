package com.sagie.myfirstapplication;

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

        // חשוב מאוד: ניקוי ה-Container כדי למנוע כפילויות בזמן גלילה
        holder.eventsContainer.removeAllViews();

        if (day.dayNumber == 0) {
            holder.tvDayNumber.setText("");
            holder.container.setBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.tvDayNumber.setBackground(null);
        } else {
            holder.tvDayNumber.setText(String.valueOf(day.dayNumber));
            holder.container.setBackgroundColor(day.isCurrentMonth ? Color.WHITE : Color.parseColor("#EEEEEE"));

            // סימון היום הנוכחי בעיגול כחול
            if (day.isToday) {
                holder.tvDayNumber.setBackgroundResource(R.drawable.today_circle);
                holder.tvDayNumber.setTextColor(Color.WHITE);
            } else {
                holder.tvDayNumber.setBackground(null);
                holder.tvDayNumber.setTextColor(Color.BLACK);
            }

            // --- כאן התיקון: הוספת האירועים לתצוגה ---
            if (day.dayEvents != null && !day.dayEvents.isEmpty()) {
                for (MechinaEvent event : day.dayEvents) {
                    TextView tvEvent = new TextView(holder.itemView.getContext());

                    // הגדרת הטקסט (שם המכינה)
                    tvEvent.setText(event.mechinaName);
                    tvEvent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    tvEvent.setTextColor(Color.WHITE);
                    tvEvent.setGravity(Gravity.CENTER);
                    tvEvent.setPadding(4, 2, 4, 2);

                    // עיצוב הרקע של האירוע (וודא שיש לך drawable כזה)
                    tvEvent.setBackgroundResource(R.drawable.event_item_bg);

                    // הגבלה לשורה אחת עם שלוש נקודות אם הטקסט ארוך
                    tvEvent.setLines(1);
                    tvEvent.setEllipsize(TextUtils.TruncateAt.END);

                    // הגדרת רווחים בין אירוע לאירוע
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 2, 0, 2);

                    // הוספה ל-Layout בתוך ה-Item
                    holder.eventsContainer.addView(tvEvent, params);
                }
            }
        }
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