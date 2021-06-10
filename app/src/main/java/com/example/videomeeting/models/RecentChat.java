package com.example.videomeeting.models;

import com.google.firebase.database.Exclude;

public class RecentChat {

    String lastMessage, senderID, remoteUserID;
    long timestamp;
    boolean seen;

    public RecentChat() {}

    public RecentChat(String lastMessage, long timestamp, String senderID, boolean seen) {
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.senderID = senderID;
        this.seen = seen;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public boolean getSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Exclude
    public String getRemoteUserID() {
        return remoteUserID;
    }
    @Exclude
    public void setRemoteUserID(String remoteUserID) {
        this.remoteUserID = remoteUserID;
    }
}
