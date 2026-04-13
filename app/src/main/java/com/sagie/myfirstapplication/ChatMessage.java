package com.sagie.myfirstapplication;

public class ChatMessage {
    public String text;
    public String senderName;
    public long timestamp;

    public ChatMessage() {} // חובה עבור פיירבייס

    public ChatMessage(String text, String senderName, long timestamp) {
        this.text = text;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }
}