package com.sagie.myfirstapplication;

public class Event {

    public String text;
    public long timestamp;

    // Firebase צריך קונסטרקטור ריק
    public Event() {}

    public Event(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }
}
