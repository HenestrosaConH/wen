package com.example.videomeeting.adapters.pagers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.videomeeting.fragments.RecentChatsFragment;
import com.example.videomeeting.fragments.RecentCallsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new RecentChatsFragment();
        } else {
            return new RecentCallsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
