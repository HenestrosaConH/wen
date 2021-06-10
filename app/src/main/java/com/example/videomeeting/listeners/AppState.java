package com.example.videomeeting.listeners;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.database.FirebaseDatabase;

import static com.example.videomeeting.utils.Constants.FIREBASE_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_ONLINE;

public class AppState extends Application implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    /**
     * Detects when the app is backgrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        if (FIREBASE_USER != null) {
            setLastSeen(String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * Detects when the app is foregrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        if (FIREBASE_USER != null) {
            setLastSeen(KEY_LAST_SEEN_ONLINE);
        }
    }

    /**
     * Sets last seen of the user depending on the status of the application
     * @param lastSeen timestamp of the last seen of the user
     */
    private void setLastSeen(String lastSeen) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(FIREBASE_USER.getUid())
                .child(KEY_LAST_SEEN)
                .setValue(lastSeen);
    }
}