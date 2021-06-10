package com.example.videomeeting.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.pagers.SearchPagerAdapter;
import com.example.videomeeting.fragments.SearchMessageFragment;
import com.example.videomeeting.fragments.SearchUserFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SearchActivity extends AppCompatActivity {

    private ViewPager2 searchVP2;
    private String globalQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupActionBar();
        setupViewPager();
    }

    /**
     * Setups ActionBar
     */
    private void setupActionBar() {
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setups ViewPager
     */
    private void setupViewPager() {
        searchVP2 = findViewById(R.id.searchVP2);
        searchVP2.setAdapter(new SearchPagerAdapter(this));

        TabLayout searchTL = findViewById(R.id.searchTL);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(searchTL, searchVP2, (tab, position) -> {
            switch (position) {
                case 0: {
                    tab.setText(R.string.users);
                    break;
                }
                case 1: {
                    tab.setText(R.string.messages);
                    break;
                }
            }
        });
        tabLayoutMediator.attach();

        searchTL.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                searchInCurrentFrag(globalQuery, tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_searchable, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchIT).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                globalQuery = query;
                if (!TextUtils.isEmpty(query)) {
                    searchInCurrentFrag(query, searchVP2.getCurrentItem());
                }
                return true;
            }
        });
        return true;
    }

    /**
     * Checks the instance of the current fragment and uses the search method by passing the query input
     */
    private void searchInCurrentFrag(String query, int position) {
        if (query != null) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + position);
            if (currentFragment instanceof SearchUserFragment) {
                ((SearchUserFragment) currentFragment).searchUser(query);
            } else {
                ((SearchMessageFragment) currentFragment).searchMessage(query);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}