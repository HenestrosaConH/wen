package com.example.videomeeting.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.videomeeting.R;
import com.example.videomeeting.utils.PreferenceManager;

import static com.example.videomeeting.utils.Constants.CHANNEL_CALLS_RINGTONE_URI;
import static com.example.videomeeting.utils.Constants.CHANNEL_CALLS_VIBRATION;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_IMPORTANCE;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_LIGHT;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_NOTIFICATION_URI;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_VIBRATION;
import static com.example.videomeeting.utils.Constants.NOTIF_IS_GLOBAL;

public class NotificationsPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final int REQUEST_CODE_NOTIFICATION = 0;
        private final int REQUEST_CODE_RINGTONE = 1;
        private PreferenceManager prefManager;
        private NotificationsPrefActivity activity;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (context instanceof Activity) {
                activity = (NotificationsPrefActivity) context;
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_notifications, rootKey);
            prefManager = new PreferenceManager(activity);
            setGlobalNotif();

            setVibrationChat();
            setPopupChat();
            setLightChat();

            setVibrationCall();
            bindInfo();
        }

        /**
         * Setups the global notifications option
         */
        private void setGlobalNotif() {
            SwitchPreferenceCompat displayNotifications = findPreference("pref_display_notif");
            displayNotifications.setOnPreferenceChangeListener((preference, newValue) -> {
                prefManager.putBoolean(NOTIF_IS_GLOBAL, (boolean) newValue);
                return true;
            });
        }

        /**
         * Setups the vibration chat option
         */
        private void setVibrationChat() {
            ListPreference vibrateChat = findPreference("pref_chat_vibrate");
            vibrateChat.setOnPreferenceChangeListener((preference, newValue) -> {
                setVibration(CHANNEL_CHATS_VIBRATION, (String) newValue);
                return true;
            });
        }

        /**
         * Setups the popup notification option
         */
        private void setPopupChat() {
            SwitchPreferenceCompat displayPopupChat = findPreference("pref_chat_popup");
            displayPopupChat.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((boolean) newValue) prefManager.putInt(CHANNEL_CHATS_IMPORTANCE, 4);
                else prefManager.putInt(CHANNEL_CHATS_IMPORTANCE, 3);
                //updateChatChannel();
                return true;
            });
        }

        /**
         * Setups the light notification option
         */
        private void setLightChat() {
            ListPreference lightChat = findPreference("pref_chat_light");
            lightChat.setOnPreferenceChangeListener((preference, newValue) -> {
                String[] lightValues = getResources().getStringArray(R.array.light_ids);
                if (newValue.equals(lightValues[0]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[0]);
                else if (newValue.equals(lightValues[1]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[1]);
                else if (newValue.equals(lightValues[2]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[2]);
                else if (newValue.equals(lightValues[3]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[3]);
                else if (newValue.equals(lightValues[4]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[4]);
                else if (newValue.equals(lightValues[5]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[5]);
                else if (newValue.equals(lightValues[6]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[6]);
                else if (newValue.equals(lightValues[7]))
                    prefManager.putString(CHANNEL_CHATS_LIGHT, lightValues[7]);
                //updateChatChannel();
                return true;
            });
        }

        /**
         * Setups the vibration call option
         */
        private void setVibrationCall() {
            ListPreference vibrateCall = findPreference("pref_call_vibrate");
            vibrateCall.setOnPreferenceChangeListener((preference, newValue) -> {
                setVibration(CHANNEL_CALLS_VIBRATION, (String) newValue);
                return true;
            });
        }

        /**
         * Setups the info item option
         */
        private void bindInfo() {
            Preference info = findPreference("pref_intent_notifications");
            info.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                    intent.putExtra("app_package", activity.getPackageName());
                    intent.putExtra("app_uid", activity.getApplicationInfo().uid);
                }
                startActivity(intent);
                return true;
            });
        }

        /**
         * Depending on the preferences of the user, we set a certain notification pattern
         */
        private void setVibration(String where, String value) {
            String[] vibrationValues = getResources().getStringArray(R.array.vibration_ids);
            if (value.equals(vibrationValues[0]))
                prefManager.putString(where, vibrationValues[0]);
            else if (value.equals(vibrationValues[1]))
                prefManager.putString(where, vibrationValues[1]);
            else if (value.equals(vibrationValues[2]))
                prefManager.putString(where, vibrationValues[2]);
            else if (value.equals(vibrationValues[3]))
                prefManager.putString(where, vibrationValues[3]);
            //updateChatChannel();
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("pref_chat_notification_sound")) {
                return changeNotifSound();
            } else if (preference.getKey().equals("pref_call_ringtone")) {
                return changeRingtone();
            } else {
                return super.onPreferenceTreeClick(preference);
            }
        }

        /**
         * Changes the notification sound
         */
        private boolean changeNotifSound() {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = prefManager.getString(CHANNEL_CHATS_NOTIFICATION_URI);
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION);
            return true;
        }

        /**
         * Changes the call ringtone
         */
        private boolean changeRingtone() {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_RINGTONE_URI);

            String existingValue = prefManager.getString(CHANNEL_CALLS_RINGTONE_URI);
            if (existingValue != null) {
                if (existingValue.length() == 0)
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                else
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
            } else
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_RINGTONE_URI);

            startActivityForResult(intent, REQUEST_CODE_RINGTONE);
            return true;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_NOTIFICATION && data != null) {
                Uri notification = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (notification != null) prefManager.putString(CHANNEL_CHATS_NOTIFICATION_URI, notification.toString());
                else prefManager.putString(CHANNEL_CHATS_NOTIFICATION_URI, "");
                //updateChatChannel();
            } else if (requestCode == REQUEST_CODE_RINGTONE && data != null) {
                Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (ringtone != null) prefManager.putString(CHANNEL_CALLS_RINGTONE_URI, ringtone.toString());
                else prefManager.putString(CHANNEL_CALLS_RINGTONE_URI, "");
            } else super.onActivityResult(requestCode, resultCode, data);
        }
    }
}