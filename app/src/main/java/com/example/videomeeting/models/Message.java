package com.example.videomeeting.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Message implements Serializable {

    String senderID, message;
    long timestamp;
    boolean seen;

    public Message() {}

    public Message(String senderID, String message, boolean seen) {
        this.senderID = senderID;
        this.message = message;
        this.seen = seen;
    }

    public String getSenderID() {
        return senderID;
    }
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Exclude
    public long getTimestamp() {
        return timestamp;
    }
    @Exclude
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
