package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.example.videomeeting.R;
import com.example.videomeeting.utils.LanguageUtils;
import com.example.videomeeting.utils.PreferenceManager;

import java.util.Locale;

import static com.example.videomeeting.utils.Constants.LANGUAGE_CHINESE;
import static com.example.videomeeting.utils.Constants.LANGUAGE_ENGLISH;
import static com.example.videomeeting.utils.Constants.LANGUAGE_SPANISH;
import static com.example.videomeeting.utils.Constants.PREF_IS_DARK_THEME_ON;
import static com.example.videomeeting.utils.Constants.PREF_LANGUAGE;

public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    private RelativeLayout relativeLayout;
    private int checkedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getString(R.string.settings));
        SettingsActivity.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefManager = new PreferenceManager(getApplicationContext());
        relativeLayout = findViewById(R.id.relativeLayout);

        bindProfileOption();
        bindNotifOption();
        bindLangOption();
        bindThemeOption();
    }

    private void bindProfileOption() {
        CardView profileCV = findViewById(R.id.profileCV);
        profileCV.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ProfileActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

    }

    private void bindNotifOption() {
        CardView notificationsCV = findViewById(R.id.notificationsCV);
        notificationsCV.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsPrefActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        );
    }

    private void bindLangOption() {
        CardView languageCV = findViewById(R.id.languageCV);
        languageCV.setOnClickListener(v -> changeLanguage());
    }

    private void bindThemeOption() {
        CardView themeCV = findViewById(R.id.themeCV);
        themeCV.setOnClickListener(v -> changeTheme());
    }

    private void changeLanguage() {
        String[] items = {"Español", "English", "简体中文"};
        if (prefManager.getSharedPreferences().contains(PREF_LANGUAGE)) {
            switch (prefManager.getString(PREF_LANGUAGE)) {
                case LANGUAGE_SPANISH:
                    checkedItem = 0;
                    break;
                case LANGUAGE_ENGLISH:
                    checkedItem = 1;
                    break;
                case LANGUAGE_CHINESE:
                    checkedItem = 2;
                    break;
            }
        } else {
            checkedItem = 0;//Spanish by default
            String currentLang = Locale.getDefault().getLanguage();
            if (currentLang.equals(new Locale(LANGUAGE_ENGLISH).getLanguage())) {
                checkedItem = 1;
            } else if (currentLang.equals(new Locale(LANGUAGE_CHINESE).getLanguage())) {
                checkedItem = 2;
            }
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
        alertDialog.setTitle(R.string.language)
                .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkedItem = 0;
                            break;
                        case 1:
                            checkedItem = 1;
                            break;
                        case 2:
                            checkedItem = 2;
                            break;
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    switch (checkedItem) {
                        case 0:
                            prefManager.putString(PREF_LANGUAGE, LANGUAGE_SPANISH);
                            LanguageUtils.setLocale(SettingsActivity.this, LANGUAGE_SPANISH);
                            break;
                        case 1:
                            prefManager.putString(PREF_LANGUAGE, LANGUAGE_ENGLISH);
                            LanguageUtils.setLocale(SettingsActivity.this, LANGUAGE_ENGLISH);
                            break;
                        case 2:
                            prefManager.putString(PREF_LANGUAGE, LANGUAGE_CHINESE);
                            LanguageUtils.setLocale(SettingsActivity.this, LANGUAGE_CHINESE);
                            break;
                    }
                    dialog.dismiss();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    overridePendingTransition(0, 0);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void changeTheme() {
        String[] items = {getString(R.string.day), getString(R.string.night)};
        checkedItem = 0; //day by default
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
        alertDialog.setTitle(R.string.theme)
                .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        checkedItem = 0;
                    } else {
                        checkedItem = 1;
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (checkedItem == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }

                    relativeLayout.setBackground(ResourcesCompat.getDrawable(
                            getResources(),
                            R.drawable.background_settings,
                            null)
                    );
                    prefManager.putBoolean(PREF_IS_DARK_THEME_ON, checkedItem != 0);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }
}