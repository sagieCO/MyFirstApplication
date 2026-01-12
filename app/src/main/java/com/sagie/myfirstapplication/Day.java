package com.sagie.myfirstapplication;

import java.util.ArrayList;
import java.util.List;

public class Day {
    public int dayNumber;
    public boolean isCurrentMonth;
    public List<MechinaEvent> dayEvents = new ArrayList<>();

    public Day(int dayNumber, boolean isCurrentMonth) {
        this.dayNumber = dayNumber;
        this.isCurrentMonth = isCurrentMonth;
    }
}