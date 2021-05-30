package com.example.videomeeting.models;

public class Call {

    String callerID, receiverID, date, callType;
    boolean isMissed;

    public Call() { }

    public Call(String callerID, String receiverID, String date, boolean isMissed, String callType) {
        this.callerID = callerID;
        this.receiverID = receiverID;
        this.date = date;
        this.isMissed = isMissed;
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

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public boolean isMissed() {
        return isMissed;
    }
    public void setMissed(boolean isMissed) {
        this.isMissed = isMissed;
    }

    public String getCallType() {
        return callType;
    }
    public void setCallType(String callType) {
        this.callType = callType;
    }
}
