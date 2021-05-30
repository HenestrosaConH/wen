package com.example.videomeeting.models;

import java.io.Serializable;

public class Message implements Serializable {

    private String senderID, readerID, message, timestamp;
    private boolean isSeen;

    public Message() {}

    public Message(String senderID, String readerID, String message, String timestamp, boolean isSeen) {
        this.senderID = senderID;
        this.readerID = readerID;
        this.message = message;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public String getSenderID() {
        return senderID;
    }
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }


    public String getReaderID() {
        return readerID;
    }
    public void setReaderID(String readerID) {
        this.readerID = readerID;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }
    public void setSeen(boolean seen) {
        this.isSeen = seen;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
