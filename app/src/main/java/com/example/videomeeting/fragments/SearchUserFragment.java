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

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;

public class SearchUserFragment extends Fragment {

    private TextView notFoundTV;
    private RecyclerView searchUserRV;
    private MaterialCardView searchTipCV;
    private Map<String, User> userMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_user, container, false);
        notFoundTV = view.findViewById(R.id.notFoundTV);
        searchTipCV = view.findViewById(R.id.searchTipCV);
        setupRV(view);
        return view;
    }

    /**
     * Setups the RecyclerView
     * @param view fragment's inflater
     */
    private void setupRV(View view) {
        userMap = new HashMap<>();
        searchUserRV = view.findViewById(R.id.searchUserRV);
        searchUserRV.setHasFixedSize(true);
        searchUserRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Gets the query and searches on the users node to retrieve the results
     * @param query Name of the user input by the user
     */
    public void searchUser(String query) {
        userMap.clear();
        boolean isUserNameValid = true;

        String finalQuery = query.trim();
        if (finalQuery.isEmpty()) {
            changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            isUserNameValid = false;
        } else if (finalQuery.length() < 4 || !finalQuery.matches("^[a-zA-Z0-9]+$")) {
            changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            isUserNameValid = false;
        }

        if (isUserNameValid) {
            FirebaseDatabase.getInstance().getReference()
                    .child(KEY_COLLECTION_USERS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User searchedUser;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        searchedUser = snapshot.getValue(User.class);
                        searchedUser.setId(snapshot.getKey());
                        if (searchedUser.getUserName().equals(CURRENT_USER.getUserName())) continue;
                        if (searchedUser.getUserName().toLowerCase().startsWith(finalQuery.toLowerCase())) {
                            userMap.put(searchedUser.getId(), searchedUser);
                        }
                    }
                    checkList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    /**
     * Changes the visibility of some views
     * @param searchVis changes visibility of the RecyclerView
     * @param searchTipVis changes visibility of the tip CardView
     * @param notFoundVis changes visibility of the not found message TextView
     */
    private void changeViewsVisibility(int searchVis, int searchTipVis, int notFoundVis) {
        searchUserRV.setVisibility(searchVis);
        searchTipCV.setVisibility(searchTipVis);
        notFoundTV.setVisibility(notFoundVis);
    }

    /**
     * Checks the userMap and refreshes the RecyclerView
     */
    private void checkList() {
        if (userMap.size() > 0) {
            refreshRV(userMap);
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    /**
     * Refreshes the RecyclerView if new values have been found
     * @param userMap Contains the results from the query
     */
    private void refreshRV(@NotNull Map<String, User> userMap) {
        SearchUserAdapter searchUserAdapter = new SearchUserAdapter(
                getContext(),
                userMap
        );
        searchUserRV.setAdapter(searchUserAdapter);
    }
}
