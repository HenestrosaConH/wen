package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.pagers.OnboardingAdapter;
import com.example.videomeeting.models.OnboardingItem;
import com.example.videomeeting.utils.Constants;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static com.example.videomeeting.utils.Constants.*;

public class OnboardingActivity extends AppCompatActivity {

    private Button nextBT, getStartedBT;
    private TextView skipTV;
    private Animation buttonAnim;
    private int position = 0;
    private PreferenceManager prefManager;
    private List<OnboardingItem> onboardingItems;
    private ViewPager screenVP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Wen);
        super.onCreate(savedInstanceState);
        prefManager = new PreferenceManager(getApplicationContext());
        hasBeenOpened();
        setContentView(R.layout.activity_onboarding);

        loadFragments();
        setupViewPager();
        setupButtons();
    }

    private void hasBeenOpened() {
        if (prefManager.getBoolean(PREF_HAS_INTRO_BEEN_OPENED)) {
            Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class );
            startActivity(intent);
            finish();
        }
    }

    private void loadFragments() {
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(getString(R.string.app_name), getString(R.string.app_description), R.drawable.ic_launcher_intro));
        onboardingItems.add(new OnboardingItem(getString(R.string.fast), getString(R.string.fast_description), R.drawable.ic_onboarding_fast));
        onboardingItems.add(new OnboardingItem(getString(R.string.free), getString(R.string.free_description), R.drawable.ic_onboarding_free));
        onboardingItems.add(new OnboardingItem(getString(R.string.secure), getString(R.string.secure_description), R.drawable.ic_onboarding_secure));
        onboardingItems.add(new OnboardingItem(getString(R.string.cloud_based), getString(R.string.cloud_description), R.drawable.ic_onboarding_cloud));
    }

    private void setupViewPager() {
        screenVP = findViewById(R.id.screenVP);
        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(this, onboardingItems);
        screenVP.setAdapter(onboardingAdapter);

        //This allows us to control the ViewPager with the dots of the TabLayout
        TabLayout tabIndicator = findViewById(R.id.tabIndicator);
        tabIndicator.setupWithViewPager(screenVP);

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == onboardingItems.size() - 1) loadLastScreen();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupButtons() {
        skipTV = findViewById(R.id.skipTV);
        skipTV.setOnClickListener(v -> screenVP.setCurrentItem(onboardingItems.size()));

        buttonAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_anim);
        getStartedBT = findViewById(R.id.getStartedBT);

        nextBT = findViewById(R.id.nextBT);
        nextBT.setOnClickListener(v -> {
            position = screenVP.getCurrentItem();
            if (position < onboardingItems.size()) {
                position++;
                screenVP.setCurrentItem(position);
            }
            if (position == onboardingItems.size() - 1)  //We reached the last screen
                loadLastScreen();
        });

        getStartedBT.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
            startActivity(intent);
            prefManager.putBoolean(PREF_HAS_INTRO_BEEN_OPENED, true);
            finish();
        });
    }

    /**
     * Shows the "Get started" button and hides the next button
     */
    private void loadLastScreen() {
        nextBT.setVisibility(View.INVISIBLE);
        skipTV.setVisibility(View.INVISIBLE);
        getStartedBT.setVisibility(View.VISIBLE);
        getStartedBT.setAnimation(buttonAnim);
    }
}