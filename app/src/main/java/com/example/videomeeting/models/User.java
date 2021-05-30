package com.example.videomeeting.models;

import com.example.videomeeting.utils.Constants;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    public String id, userName, phoneNumber, fcmToken, imageURL, lastSeen, about, isLastSeenEnabled;
    public HashMap<String, Boolean> contactedUser;

    public User() {}

    public User(String id, String userName, String phoneNumber, String fcmToken, String imageURL) {
        this.id = id;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.fcmToken = fcmToken;
        this.imageURL = imageURL;
        about = Constants.KEY_ABOUT_DEFAULT;
        isLastSeenEnabled = Constants.KEY_LAST_SEEN_TRUE;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String isLastSeenEnabled() { return isLastSeenEnabled; }
    public void setLastSeenEnabled(String isLastSeenEnabled) { this.isLastSeenEnabled = isLastSeenEnabled; }

    public HashMap<String, Boolean> getContactedUser() {
        return contactedUser;
    }
    public void setContactedUser(HashMap<String, Boolean> contactedUser) {
        this.contactedUser = contactedUser;
    }
}