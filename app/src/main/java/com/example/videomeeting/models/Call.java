package com.example.videomeeting.models;

import com.google.firebase.database.Exclude;

public class Call {

    String callerID, receiverID, callType;
    long timestamp;
    boolean missed;

    public Call() { }

    public Call(String callerID, String receiverID, boolean missed, String callType) {
        this.callerID = callerID;
        this.receiverID = receiverID;
        this.missed = missed;
        this.callType = callType;
    }

    public String getCallerID() {
        return callerID;
    }
    public void setCallerID(String callerID) {
        this.callerID = callerID;
    }

    public String getReceiverID() {
        return receiverID;
    }
    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public boolean getMissed() {
        return missed;
    }
    public void setMissed(boolean missed) {
        this.missed = missed;
    }

    public String getCallType() {
        return callType;
    }
    public void setCallType(String callType) {
        this.callType = callType;
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
