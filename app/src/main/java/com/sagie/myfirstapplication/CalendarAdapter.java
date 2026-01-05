package com.sagie.myfirstapplication;

import android.graphics.Color;
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
        // טוען את ה־item.xml בו יופיע כל יום
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Day day = days.get(position);

        // מציג את מספר היום
        holder.tvDayNumber.setText(String.valueOf(day.dayNumber));

        // אם היום שייך לחודש הנוכחי, צבע רגיל, אחרת צבע כהה
        if (day.isCurrentMonth) {
            holder.tvDayNumber.setTextColor(Color.BLACK); // צבע רגיל ליום
            holder.container.setBackgroundColor(Color.parseColor("#D76E6E")); // רקע ליום בשבוע
        } else {
            holder.tvDayNumber.setTextColor(Color.GRAY); // צבע אפור לימים לא של החודש הנוכחי
            holder.container.setBackgroundColor(Color.parseColor("#EEEEEE")); // רקע חיוור לימים מחוץ לחודש הנוכחי
        }

        // ניתן כאן להוסיף אירועים ל-LinearLayout eventsContainer אם יש (למשל, להוסיף TextView עם שם אירוע)
        holder.eventsContainer.removeAllViews(); // אם יש אירועים, להוסיף כאן
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        LinearLayout eventsContainer;
        LinearLayout container;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            eventsContainer = itemView.findViewById(R.id.eventsContainer);
            container = itemView.findViewById(R.id.linearLayoutContainer);
        }
    }
}
