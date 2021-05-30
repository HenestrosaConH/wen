package com.example.videomeeting.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.videomeeting.R;
import com.example.videomeeting.utils.PreferenceManager;

import java.util.Objects;

import static com.example.videomeeting.utils.Constants.*;

public class NotificationsPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);
        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final int REQUEST_CODE_NOTIFICATION = 0;
        private final int REQUEST_CODE_RINGTONE = 1;
        private PreferenceManager preferenceManager;
        private NotificationsPrefActivity activity;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (context instanceof Activity)
                activity = (NotificationsPrefActivity) context;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_notifications, rootKey);
            preferenceManager = new PreferenceManager(activity);

            SwitchPreferenceCompat displayNotifications = findPreference("pref_display_notif");
            Objects.requireNonNull(displayNotifications).setOnPreferenceChangeListener((preference, newValue) -> {
                preferenceManager.putBoolean(NOTIF_IS_GLOBAL, (boolean) newValue);
                return true;
            });

            ListPreference vibrateChat = findPreference("pref_chat_vibrate");
            Objects.requireNonNull(vibrateChat).setOnPreferenceChangeListener((preference, newValue) -> {
                setVibration(CHANNEL_CHATS_VIBRATION, (String) newValue);
                return true;
            });

            SwitchPreferenceCompat displayPopupChat = findPreference("pref_chat_popup");
            Objects.requireNonNull(displayPopupChat).setOnPreferenceChangeListener((preference, newValue) -> {
                if ((boolean) newValue) preferenceManager.putInt(CHANNEL_CHATS_IMPORTANCE, 4);
                else preferenceManager.putInt(CHANNEL_CHATS_IMPORTANCE, 3);
                //updateChatChannel();
                return true;
            });

            ListPreference lightChat = findPreference("pref_chat_light");
            Objects.requireNonNull(lightChat).setOnPreferenceChangeListener((preference, newValue) -> {
                String[] lightValues = getResources().getStringArray(R.array.light_ids);
                if (newValue.equals(lightValues[0]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[0]);
                else if (newValue.equals(lightValues[1]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[1]);
                else if (newValue.equals(lightValues[2]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[2]);
                else if (newValue.equals(lightValues[3]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[3]);
                else if (newValue.equals(lightValues[4]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[4]);
                else if (newValue.equals(lightValues[5]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[5]);
                else if (newValue.equals(lightValues[6]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[6]);
                else if (newValue.equals(lightValues[7]))
                    preferenceManager.putString(CHANNEL_CHATS_LIGHT, lightValues[7]);
                //updateChatChannel();
                return true;
            });

            ListPreference vibrateCall = findPreference("pref_call_vibrate");
            Objects.requireNonNull(vibrateCall).setOnPreferenceChangeListener((preference, newValue) -> {
                setVibration(CHANNEL_CALLS_VIBRATION, (String) newValue);
                return true;
            });

            Preference info = findPreference("pref_intent_notifications");
            Objects.requireNonNull(info).setOnPreferenceClickListener(preference -> {
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

        private void setVibration(String where, String value) {
            String[] vibrationValues = getResources().getStringArray(R.array.vibration_ids);
            if (value.equals(vibrationValues[0]))
                preferenceManager.putString(where, vibrationValues[0]);
            else if (value.equals(vibrationValues[1]))
                preferenceManager.putString(where, vibrationValues[1]);
            else if (value.equals(vibrationValues[2]))
                preferenceManager.putString(where, vibrationValues[2]);
            else if (value.equals(vibrationValues[3]))
                preferenceManager.putString(where, vibrationValues[3]);
            //updateChatChannel();
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("pref_chat_notification_sound")) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                String existingValue = preferenceManager.getString(CHANNEL_CHATS_NOTIFICATION_URI);
                if (existingValue != null) {
                    if (existingValue.length() == 0)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    else
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                } else
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                startActivityForResult(intent, REQUEST_CODE_NOTIFICATION);
                return true;
            }

            else if (preference.getKey().equals("pref_call_ringtone")) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_RINGTONE_URI);

                String existingValue = preferenceManager.getString(CHANNEL_CALLS_RINGTONE_URI);
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

            else
                return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_NOTIFICATION && data != null) {
                Uri notification = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (notification != null) preferenceManager.putString(CHANNEL_CHATS_NOTIFICATION_URI, notification.toString());
                else preferenceManager.putString(CHANNEL_CHATS_NOTIFICATION_URI, "");
                //updateChatChannel();
            } else if (requestCode == REQUEST_CODE_RINGTONE && data != null) {
                Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (ringtone != null) preferenceManager.putString(CHANNEL_CALLS_RINGTONE_URI, ringtone.toString());
                else preferenceManager.putString(CHANNEL_CALLS_RINGTONE_URI, "");
            } else super.onActivityResult(requestCode, resultCode, data);
        }

        /*
        @RequiresApi(api = Build.VERSION_CODES.O)
        private void updateChatChannel() {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel(String.valueOf(CHANNEL_CHATS_ID));
            CHANNEL_CHATS_ID += 1;

            //Pop up on or off
            int importance = 4;
            if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_IMPORTANCE)) {
                if (preferenceManager.getInt(CHANNEL_CHATS_IMPORTANCE) == 3) importance = 3;
            }

            NotificationChannel channel = new NotificationChannel(
                    String.valueOf(CHANNEL_CHATS_ID),
                    CHANNEL_CHATS_NAME,
                    importance
            );

            //Lights
            if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_LIGHT)) {
                switch (preferenceManager.getString(CHANNEL_CHATS_LIGHT)) {
                    case NOTIF_LIGHT_RED:
                        channel.setLightColor(Color.RED);
                        break;
                    case NOTIF_LIGHT_BLUE:
                        channel.setLightColor(Color.BLUE);
                        break;
                    case NOTIF_LIGHT_YELLOW:
                        channel.setLightColor(Color.YELLOW);
                        break;
                    case NOTIF_LIGHT_WHITE:
                        channel.setLightColor(Color.WHITE);
                        break;
                    case NOTIF_LIGHT_PURPLE:
                        channel.setLightColor(Color.MAGENTA);
                        break;
                    case NOTIF_LIGHT_GREEN:
                        channel.setLightColor(Color.GREEN);
                        break;
                    case NOTIF_LIGHT_CYAN:
                        channel.setLightColor(Color.CYAN);
                        break;
                    case PREF_DEFAULT:
                        channel.setLightColor(Notification.DEFAULT_LIGHTS);
                        break;
                    case PREF_OFF:
                        channel.enableLights(false);
                }
            } else channel.setLightColor(Notification.DEFAULT_LIGHTS);

            //Vibration
            if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_VIBRATION)) {
                switch (preferenceManager.getString(CHANNEL_CHATS_VIBRATION)) {
                    case PREF_DEFAULT:
                        channel.setVibrationPattern(new long[] {0, 500});
                    case NOTIF_VIBRATION_LONG:
                        channel.setVibrationPattern(new long[] {0, 1000});
                    case NOTIF_VIBRATION_SHORT:
                        channel.setVibrationPattern(new long[] {0, 250});
                    case PREF_OFF:
                        channel.setVibrationPattern(null);
                }
            }

            //Sound
            Uri notificationUri;
            if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_NOTIFICATION_URI)) {
                notificationUri = Uri.parse(preferenceManager.getString(CHANNEL_CHATS_NOTIFICATION_URI));
            } else notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(
                    notificationUri,
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
            );

            notificationManager.createNotificationChannel(channel);
        }*/
    }
}