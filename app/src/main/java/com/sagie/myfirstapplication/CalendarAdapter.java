package com.sagie.myfirstapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sagie.myfirstapplication.Activities.MonthlyCalendarActivity;
import com.sagie.myfirstapplication.models.Day;
import com.sagie.myfirstapplication.models.MechinaEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
        final Day day = days.get(position);

        holder.eventsContainer.removeAllViews();

        if (day.dayNumber == 0) {
            holder.tvDayNumber.setText("");
            holder.container.setBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.container.setOnClickListener(null);
        } else {
            holder.tvDayNumber.setText(String.valueOf(day.dayNumber));

            int bgColor;
            if (day.isCurrentMonth) {
                bgColor = Color.WHITE;
            } else {
                bgColor = Color.parseColor("#EEEEEE");
            }
            holder.container.setBackgroundColor(bgColor);

            if (day.isToday) {
                holder.tvDayNumber.setBackgroundResource(R.drawable.today_circle);
                holder.tvDayNumber.setTextColor(Color.WHITE);
            } else {
                holder.tvDayNumber.setBackground(null);
                holder.tvDayNumber.setTextColor(Color.BLACK);
            }

            if (day.dayEvents != null && day.dayEvents.size() > 1) {
                Collections.sort(day.dayEvents, new Comparator<MechinaEvent>() {
                    @Override
                    public int compare(MechinaEvent e1, MechinaEvent e2) {
                        String t1, t2;
                        if (e1.time == null || e1.time.isEmpty()) t1 = "23:59"; else t1 = e1.time;
                        if (e2.time == null || e2.time.isEmpty()) t2 = "23:59"; else t2 = e2.time;
                        return t1.compareTo(t2);
                    }
                });
            }

            if (day.dayEvents != null && !day.dayEvents.isEmpty()) {
                Iterator<MechinaEvent> iterator = day.dayEvents.iterator();
                while (iterator.hasNext()) {
                    MechinaEvent event = iterator.next();
                    TextView tvEvent = new TextView(holder.itemView.getContext());

                    String display;
                    if (event.time != null && !event.time.isEmpty()) {
                        display = event.time + " " + event.mechinaName;
                    } else {
                        display = event.mechinaName;
                    }

                    tvEvent.setText(display);
                    tvEvent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                    tvEvent.setTextColor(Color.WHITE);
                    tvEvent.setPadding(6, 2, 6, 2);
                    tvEvent.setLines(1);
                    tvEvent.setEllipsize(TextUtils.TruncateAt.END);

                    // עיצוב רקע דינמי
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(8f);

                    int eventColor;
                    try {
                        if (event.eventColor != null && !event.eventColor.isEmpty()) {
                            eventColor = Color.parseColor(event.eventColor);
                        } else {
                            eventColor = Color.parseColor("#3F51B5");
                        }
                    } catch (Exception e) {
                        eventColor = Color.GRAY;
                    }
                    shape.setColor(eventColor);
                    tvEvent.setBackground(shape);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(2, 1, 2, 1);

                    holder.eventsContainer.addView(tvEvent, params);
                }
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDayDetailsDialog(v.getContext(), day);
                }
            });
        }
    }

    private void showDayDetailsDialog(final Context context, Day day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("אירועים לתאריך " + day.dayNumber);

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(50, 40, 50, 40);

        if (day.dayEvents == null || day.dayEvents.isEmpty()) {
            TextView emptyTv = new TextView(context);
            emptyTv.setText("אין אירועים רשומים ליום זה.");
            emptyTv.setGravity(Gravity.CENTER);
            rootLayout.addView(emptyTv);
        } else {
            Iterator<MechinaEvent> iterator = day.dayEvents.iterator();
            while (iterator.hasNext()) {
                final MechinaEvent event = iterator.next();

                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, 20, 0, 20);

                TextView tvInfo = new TextView(context);
                String timeStr;
                if (event.time != null) timeStr = event.time; else timeStr = "--:--";
                String info = "🕒 " + timeStr + " - " + event.mechinaName;
                if (event.branch != null && !event.branch.isEmpty()) {
                    info += "\nשלוחה: " + event.branch;
                }
                tvInfo.setText(info);
                tvInfo.setTextColor(Color.BLACK);
                tvInfo.setTextSize(16);
                row.addView(tvInfo);

                Button btnDelete = new Button(context);
                btnDelete.setText("מחיקת אירוע");
                btnDelete.setBackgroundColor(Color.parseColor("#FF5252"));
                btnDelete.setTextColor(Color.WHITE);

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                buttonParams.setMargins(0, 10, 0, 10);
                btnDelete.setLayoutParams(buttonParams);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (context instanceof MonthlyCalendarActivity) {
                            AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context);
                            confirmBuilder.setTitle("אישור מחיקה");
                            confirmBuilder.setMessage("האם אתה בטוח שברצונך למחוק את " + event.mechinaName + "?");
                            confirmBuilder.setPositiveButton("כן, מחק", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MonthlyCalendarActivity) context).deleteEvent(event);
                                }
                            });
                            confirmBuilder.setNegativeButton("ביטול", null);
                            confirmBuilder.show();
                        }
                    }
                });

                row.addView(btnDelete);
                rootLayout.addView(row);

                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(Color.LTGRAY);
                rootLayout.addView(divider);
            }
        }

        builder.setView(rootLayout);
        builder.setPositiveButton("סגור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

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