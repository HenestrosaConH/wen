package com.example.videomeeting.utils;

import com.example.videomeeting.models.User;
import com.google.firebase.auth.FirebaseUser;

public class Constants {
    public static final String PREF_GLOBAL_NAME = "wenPrefs";
    public static final String PREF_OFF = "off";
    public static final String PREF_DEFAULT = "default";

    //FIREBASE KEYS//
    //Main nodes
    public static final String KEY_COLLECTION_USERS = "Users";
    public static final String KEY_COLLECTION_USERNAMES = "UserNames";
    public static final String KEY_COLLECTION_RECENT_CHATS = "RecentChats";
    public static final String KEY_COLLECTION_CALLS = "Calls";
    public static final String KEY_COLLECTION_MESSAGES = "Messages";
    //public static final String KEY_COLLECTION_CONTACTED_USER = "ContactedUser";
    public static final String KEY_STORAGE_PROFILE_PICTURES = "ProfilePictures";
    //User node keys
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_ABOUT = "about";
    public static final String KEY_ABOUT_DEFAULT = "Available";
    public static final String KEY_CONTACTED_USERS = "contacts";
    //We need a FCM token of a user to send and receive a meeting invitation. Every time the user signs up/in a new token is created.
    //When the user signs out, we remove the token from the database
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_IMAGE_URL = "imageURL";
    public static final String KEY_IMAGE_URL_DEFAULT = "default";
    //Last seen and the privacy selected by the user
    public static final String KEY_LAST_SEEN = "lastSeen";
    public static final String KEY_LAST_SEEN_ONLINE = "online";
    public static final String KEY_IS_LAST_SEEN_STATUS = "lastSeenStatus";
    public static final String KEY_LAST_SEEN_ALL = "all";
    public static final String KEY_LAST_SEEN_CONTACTS = "contacts";
    public static final String KEY_LAST_SEEN_NONE = "none";
    //Current user
    public static FirebaseUser FIREBASE_USER;
    public static User CURRENT_USER;
    //Call node keys
    public static final String KEY_CALL_MISSED = "missed";
    //Call types are taken from "Intent" section to avoid repetition of variables
    //Chat node keys
    public static final String KEY_SEEN = "seen";
    //ContactedUser node keys
    //public static final String KEY_IS_CONTACT = "contact";

    //SETTINGS PREFERENCES
    public static final String PREF_IS_SIGNED_IN = "isSignedIn";
    public static final String PREF_IS_DARK_THEME_ON = "isDarkThemeOn";
    public static final String PREF_HAS_INTRO_BEEN_OPENED = "hasIntroBeenOpened";
    public static final String PREF_NEEDS_TO_SETUP_PROFILE = "needsToSetupProfile";
    public static final String PREF_SHOULD_SHOW_BATTERY_DIALOG = "showBatteryDialog";
    //Language codes for preferences
    public static final String PREF_LANGUAGE = "language";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_CHINESE = "zh";
    //Notifications common preferences
    public static final String NOTIF_IS_GLOBAL = "isGlobalNotif";
    public static final String NOTIF_LIGHT_BLUE = "blue";
    public static final String NOTIF_LIGHT_CYAN = "cyan";
    public static final String NOTIF_LIGHT_GREEN = "green";
    public static final String NOTIF_LIGHT_PURPLE = "purple";
    public static final String NOTIF_LIGHT_RED = "red";
    public static final String NOTIF_LIGHT_WHITE = "white";
    public static final String NOTIF_LIGHT_YELLOW = "yellow";
    public static final String NOTIF_VIBRATION_LONG = "long";
    public static final String NOTIF_VIBRATION_SHORT = "short";
    //Default notifications channel for chats
    public static final String CHANNEL_CHATS_NAME = "Chats";
    public static final String CHANNEL_CHATS_LIGHT = "chatLight";
    public static final String CHANNEL_CHATS_VIBRATION = "vibrationChat";
    public static final String CHANNEL_CHATS_NOTIFICATION_URI = "notificationUri";
    public static final String CHANNEL_CHATS_IMPORTANCE = "importance";
    public static int CHANNEL_CHATS_ID = 1;
    //Default notifications channel for calls
    public static final String CHANNEL_CALLS_NAME = "Calls";
    public static final String CHANNEL_CALLS_RINGTONE_URI = "ringtoneUri";
    public static final String CHANNEL_CALLS_VIBRATION = "vibrationCall";
    public static final int CHANNEL_CALLS_ID = 1;

    //INTENT
    public static final String INTENT_USER = "user";
    public static final String INTENT_CALL_TYPE = "type";
    public static final String INTENT_CALL_TYPE_AUDIO = "audio";
    public static final String INTENT_CALL_TYPE_VIDEO = "video";
    public static final String INTENT_SELECTED_USERS = "selectedUsers";
    public static final String INTENT_ARE_MULTIPLE_SELECTED_USERS = "isMultiple";
    //Intent OTP
    public static final String INTENT_PHONE_NUMBER = "phoneNumber";
    public static final String INTENT_VERIFICATION_ID = "verificationId";

    //NOTIFICATIONS JSON KEYS
    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_BODY = "body";
    public static final String NOTIFICATION_TO = "to";
    public static final String RESULT_KEY = "key_text_reply";
    //Fields
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_CALL_TYPE = "callType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    //Call invitation response
    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static final String REMOTE_MSG_GROUP = "group";

    //This allows us to communicate with the FCM service. They key is unique for our project
    //Headers for our API request
    public static final String REMOTE_BASE_SERVER_URL = "https://meet.jit.si";
    public static final String REMOTE_SERVER_KEY = "AAAAIrKP8tA:APA91bH-srVZhn8W9Yh1kulNDGoc5wlUyv04sh430rC16er-Ha9JbNefJtiBybgCm2abnZehp-0TbfevmTDaaC8h-IfMe5n6poJXQBQV-8v04ofo1zviN6-zH9D0jzCYrF59kdSBKlzh";
}