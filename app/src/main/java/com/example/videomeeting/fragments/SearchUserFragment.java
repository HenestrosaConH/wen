package com.example.videomeeting.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.recyclerviews.SearchUserAdapter;
import com.example.videomeeting.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.example.videomeeting.utils.Constants.BUNDLE_SEARCH;
import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;

public class SearchUserFragment extends Fragment {

    private TextView notFoundTV;
    private RecyclerView searchUserRV;
    private MaterialCardView searchTipCV;
    private Map<String, User> userMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_message, container, false);
        notFoundTV = view.findViewById(R.id.notFoundTV);
        searchTipCV = view.findViewById(R.id.searchTipCV);
        refreshRV(view);
        return view;
    }

    private void refreshRV(View view) {
        userMap = new HashMap<>();
        searchUserRV = view.findViewById(R.id.searchMessageRV);
        searchUserRV.setHasFixedSize(true);
        searchUserRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void searchUser(String query) {
        userMap.clear();
        boolean isUserNameValid = true;

        String finalQuery = query.trim();
        if (finalQuery.isEmpty()) {
            setViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            isUserNameValid = false;
        } else if (finalQuery.length() < 4 || !finalQuery.matches("^[a-zA-Z0-9]+$")) {
            setViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            isUserNameValid = false;
        }

        if (isUserNameValid) {
            FirebaseDatabase.getInstance().getReference()
                    .child(KEY_COLLECTION_USER)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getUserName().equals(CURRENT_USER.getUserName())) continue;
                        if (user.getUserName().toLowerCase().startsWith(finalQuery.toLowerCase())) {
                            userMap.put(user.getId(), user);
                        }
                        checkList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    private void setViewsVisibility(int searchVis, int searchTipVis, int notFoundVis) {
        searchUserRV.setVisibility(searchVis);
        searchTipCV.setVisibility(searchTipVis);
        notFoundTV.setVisibility(notFoundVis);
    }

    private void checkList() {
        if (userMap.size() > 0) {
            refreshRV(userMap);
            setViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            setViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void refreshRV(@NotNull Map<String, User> userMap) {
        SearchUserAdapter searchUserAdapter = new SearchUserAdapter(
                getContext(),
                userMap
        );
        searchUserRV.setAdapter(searchUserAdapter);
    }
}