package com.sagie.myfirstapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyCalendarActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CalendarAdapter adapter;
    private List<Day> days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_calendar);

        recyclerView = findViewById(R.id.calendarRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 ימים בשורה

        days = new ArrayList<>();
        loadMonth(); // טוען את החודש הנוכחי

        adapter = new CalendarAdapter(days);
        recyclerView.setAdapter(adapter);
    }

    private void loadMonth() {
        // מאתחל את ה־calendar כדי לדעת איזה יום מתחיל את החודש
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // מתחיל את החודש מהיום הראשון

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // מספר הימים בחודש הנוכחי

        // הימים שלא שייכים לחודש הנוכחי (ימים מהחודש הקודם)
        // מוסיף ימים ריקים לפני התאריך הראשון של החודש (לפי היום בשבוע שבו מתחיל החודש)
        // הימים האלה יתווספו רק אם הם צריכים להימצא בשבוע הראשון של החודש
        for (int i = 0; i < firstDayOfWeek; i++) {
            // במקום להוסיף 0, אנחנו פשוט לא מוסיפים את הימים הריקים כלל
        }

        // הימים של החודש הנוכחי
        for (int i = 1; i <= daysInMonth; i++) {
            // הוספת ימים מהחודש הנוכחי
            days.add(new Day(i, true)); // הוספת ימים מהחודש הנוכחי
        }
    }


}
