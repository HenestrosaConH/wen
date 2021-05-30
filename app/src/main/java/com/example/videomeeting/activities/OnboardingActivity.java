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

public class OnboardingActivity extends AppCompatActivity {

    private Button nextBT, getStartedBT;
    private TextView skipTV;
    private Animation buttonAnim;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        //Checking if the activity has been opened before
        if (preferenceManager.getBoolean(Constants.PREF_HAS_INTRO_BEEN_OPENED)) {
            Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class );
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_onboarding);

        List<OnboardingItem> itemsList = new ArrayList<>();
        itemsList.add(new OnboardingItem(getString(R.string.app_name), getString(R.string.app_description), R.drawable.ic_launcher_intro));
        itemsList.add(new OnboardingItem(getString(R.string.fast), getString(R.string.fast_description), R.drawable.ic_onboarding_fast));
        itemsList.add(new OnboardingItem(getString(R.string.free), getString(R.string.free_description), R.drawable.ic_onboarding_free));
        itemsList.add(new OnboardingItem(getString(R.string.secure), getString(R.string.secure_description), R.drawable.ic_onboarding_secure));
        itemsList.add(new OnboardingItem(getString(R.string.cloud_based), getString(R.string.cloud_description), R.drawable.ic_onboarding_cloud));

        ViewPager screenVP = findViewById(R.id.screenVP);
        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(this, itemsList);
        screenVP.setAdapter(onboardingAdapter);

        //This allows us to control the ViewPager with the dots of the TabLayout
        TabLayout tabIndicator = findViewById(R.id.tabIndicator);
        tabIndicator.setupWithViewPager(screenVP);

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == itemsList.size() - 1) loadLastScreen();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        //TextView
        skipTV = findViewById(R.id.skipTV);
        skipTV.setOnClickListener(v -> screenVP.setCurrentItem(itemsList.size()));

        //Buttons
        buttonAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_anim);
        getStartedBT = findViewById(R.id.getStartedBT);

        nextBT = findViewById(R.id.nextBT);
        nextBT.setOnClickListener(v -> {
            position = screenVP.getCurrentItem();
            if (position < itemsList.size()) {
                position++;
                screenVP.setCurrentItem(position);
            }
            if (position == itemsList.size() - 1)  //We reached the last screen
                loadLastScreen();
        });

        getStartedBT.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
            startActivity(intent);
            preferenceManager.putBoolean(Constants.PREF_HAS_INTRO_BEEN_OPENED, true);
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