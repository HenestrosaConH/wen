package com.example.videomeeting.adapters.pagers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.videomeeting.fragments.SearchMessageFragment;
import com.example.videomeeting.fragments.SearchUserFragment;

public class SearchPagerAdapter extends FragmentStateAdapter {

    public SearchPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new SearchUserFragment();
        } else {
            return new SearchMessageFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
