package com.example.videomeeting.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.pagers.MainPagerAdapter;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.LanguageUtils;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.example.videomeeting.utils.Constants.*;
import static com.example.videomeeting.utils.Constants.NOTIF_IS_GLOBAL;
import static com.example.videomeeting.utils.Constants.PREF_IS_DARK_THEME_ON;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private final int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //enablePersistence();
        checkPreferences();
        setCurrentUserID();

        setupActionBar();
        setupViewPager();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMTokenToDB(task.getResult().getToken());
            }
        });
    }

    private void setupViewPager() {
        ViewPager2 mainVP2 = findViewById(R.id.mainVP2);
        mainVP2.setAdapter(new MainPagerAdapter(this));

        TabLayout mainTL = findViewById(R.id.mainTL);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(mainTL, mainVP2, (tab, position) -> {
            switch (position) {
                case 0: {
                    tab.setText(R.string.chats);
                    BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                    badgeDrawable.setBackgroundColor(
                            ContextCompat.getColor(MainActivity.this, R.color.colorBackground)
                    );
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(100);
                    badgeDrawable.setMaxCharacterCount(3);
                    break;
                } case 1: {
                    tab.setText(R.string.calls);
                    BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                    badgeDrawable.setBackgroundColor(
                            ContextCompat.getColor(MainActivity.this, R.color.colorBackground)
                    );
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(100);
                    badgeDrawable.setMaxCharacterCount(3);
                    break;
                }
            }
        });
        tabLayoutMediator.attach();

        mainVP2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                BadgeDrawable badgeDrawable = mainTL.getTabAt(position).getOrCreateBadge();
                badgeDrawable.setVisible(false);
            }
        });
    }

    private void setCurrentUserID() {
        if (FIREBASE_USER == null || CURRENT_USER == null) {
            FIREBASE_USER = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                    .child(FIREBASE_USER.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            CURRENT_USER = snapshot.getValue(User.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    private void checkPreferences() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(PREF_IS_DARK_THEME_ON)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (preferenceManager.getSharedPreferences().contains(PREF_LANGUAGE)) {
            LanguageUtils.setLocale(MainActivity.this, preferenceManager.getString(PREF_LANGUAGE));
        }
        if (!preferenceManager.getSharedPreferences().contains(NOTIF_IS_GLOBAL)) {
            preferenceManager.putBoolean(NOTIF_IS_GLOBAL, true);
        }
        if (!preferenceManager.getBoolean(PREF_SHOULD_SHOW_BATTERY_DIALOG)) {
            checkForBatteryOptimization();
        }
    }

    private void sendFCMTokenToDB(String token) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(FIREBASE_USER.getUid())
                .child(KEY_FCM_TOKEN)
                .setValue(token)
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, getString(R.string.unable_to_send_token), Toast.LENGTH_LONG).show()
                );
    }

    private void signOut() {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(FIREBASE_USER.getUid())
                .child(KEY_FCM_TOKEN)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPrefsForSignOut();
                    CURRENT_USER = null;
                    FIREBASE_USER = null;
                    startActivity(new Intent(getApplicationContext(), SendOTPActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, getString(R.string.unable_to_sign_out), Toast.LENGTH_SHORT).show());
    }

    private void enablePersistence() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.logoutIT:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.sure_))
                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> signOut())
                            .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();
                    return true;
                case R.id.settingsIT:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
                case R.id.searchIT:
                    startActivity(new Intent(MainActivity.this, SearchActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            return false;
        });
    }

    private void checkForBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                View checkBoxView = View.inflate(this, R.layout.dialog_checkbox, null);
                CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                        preferenceManager.putBoolean(PREF_SHOULD_SHOW_BATTERY_DIALOG, isChecked));
                checkBox.setText(getString(R.string.dont_ask_again));

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.warning)
                        .setCancelable(false)
                        .setView(checkBoxView)
                        .setMessage(R.string.battery_optimization_enabled)
                        .setPositiveButton(R.string.disable, (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION) {
            checkForBatteryOptimization();
        }
    }
}