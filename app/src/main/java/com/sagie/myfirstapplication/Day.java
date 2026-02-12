package com.sagie.myfirstapplication;
import java.util.ArrayList;
import java.util.List;

public class Day {
    public int dayNumber;
    public boolean isCurrentMonth;
    public boolean isToday; // הוסף את זה
    public List<MechinaEvent> dayEvents = new ArrayList<>();

    public Day(int dayNumber, boolean isCurrentMonth) {
        this.dayNumber = dayNumber;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = false;
    }
}