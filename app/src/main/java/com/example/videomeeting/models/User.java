package com.example.videomeeting.models;

import com.example.videomeeting.utils.Constants;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    String id, userName, fcmToken, imageURL, lastSeen, about, lastSeenStatus;
    HashMap<String, Boolean> contacts;

    public User() {}

    public User(String userName, String fcmToken, String imageURL) {
        this.userName = userName;
        this.fcmToken = fcmToken;
        this.imageURL = imageURL;
        lastSeen = Constants.KEY_LAST_SEEN_ONLINE;
        about = Constants.KEY_ABOUT_DEFAULT;
        lastSeenStatus = Constants.KEY_LAST_SEEN_ALL;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFcmToken() {
        return fcmToken;
    }
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getAbout() {
        return about;
    }
    public void setAbout(String about) {
        this.about = about;
    }

    public String getLastSeenStatus() {
        return lastSeenStatus;
    }
    public void setLastSeenStatus(String lastSeenStatus) {
        this.lastSeenStatus = lastSeenStatus;
    }

    public HashMap<String, Boolean> getContacts() {
        return contacts;
    }
    public void setContacts(HashMap<String, Boolean> contacts) {
        this.contacts = contacts;
    }

    @Exclude
    public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }
}