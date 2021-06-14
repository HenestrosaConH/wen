package com.example.videomeeting.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.activities.UsersListCallsActivity;
import com.example.videomeeting.adapters.recyclerviews.RecentCallsAdapter;
import com.example.videomeeting.models.Call;
import com.example.videomeeting.models.User;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.videomeeting.utils.Constants.FIREBASE_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CALLS;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_GROUP;

public class RecentCallsFragment extends Fragment {

    private final DatabaseReference callsRef = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_CALLS)
                .child(FIREBASE_USER.getUid());

    private List<User> userList;
    private List<Call> callList;

    private RecyclerView recentCallsRV;
    private TextView errorMessageTV;
    private CardView welcomingCV;
    private ShimmerFrameLayout shimmerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_calls, container, false);

        errorMessageTV = view.findViewById(R.id.errorMessageTV);
        welcomingCV = view.findViewById(R.id.welcomingCV);
        setupRV(view);
        setupFab(view);

        getRecentCalls();
        return view;
    }

    private void setupRV(View view) {
        userList = new ArrayList<>();
        callList = new ArrayList<>();
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        RecentCallsAdapter recentCallsAdapter = new RecentCallsAdapter(getContext(), userList, callList);
        recentCallsRV = view.findViewById(R.id.callsRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recentCallsRV.setLayoutManager(linearLayoutManager);
        recentCallsRV.setAdapter(recentCallsAdapter);
    }

    private void setupFab(View view) {
        view.findViewById(R.id.callsListFB).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UsersListCallsActivity.class);
            startActivity(intent);
        });
    }

    private void getRecentCalls() {
        changeViewsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE);
        callsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    final int[] i = {0};
                    callList.clear();
                    userList.clear();//
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Call call = dataSnapshot.getValue(Call.class);
                        call.setTimestamp(Long.parseLong(dataSnapshot.getKey()));
                        if (call.getCallerID().equals(FIREBASE_USER.getUid()) && !call.getReceiverID().contains("\n")) {
                            call.setSearchID(call.getReceiverID());
                        } else if (call.getReceiverID().contains("\n")) {
                            call.setSearchID(REMOTE_MSG_GROUP);
                        } else {
                            call.setSearchID(call.getCallerID());
                        }
                        callList.add(call);
                        if (++i[0] == snapshot.getChildrenCount()) {
                            for (Call callitem : callList) {
                                getUsersFromCalls(callitem);
                            }
                        }
                    }
                } else {
                    changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE, View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
            }
        });
    }

    private void getUsersFromCalls(Call call) {
        if (call.getSearchID().equals(REMOTE_MSG_GROUP)) {
            User user = new User(
                    call.getReceiverID(),
                    "",
                    KEY_IMAGE_URL_DEFAULT
            );
            userList.add(user);
        } else {
            FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                    .child(call.getSearchID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            user.setId(snapshot.getKey());
                            userList.add(user);
                            if (callList.size() == userList.size()) {
                                recentCallsRV.setAdapter(new RecentCallsAdapter(
                                        getContext(),
                                        userList,
                                        callList)
                                );
                                changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE, View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                            changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
                        }
                    });
        }
    }

    private void changeViewsVisibility(int recentCallsVis, int errorMessageVis, int welcomingVis, int shimmerVis) {
        recentCallsRV.setVisibility(recentCallsVis);
        errorMessageTV.setVisibility(errorMessageVis);
        welcomingCV.setVisibility(welcomingVis);
        shimmerLayout.setVisibility(shimmerVis);
    }

}