package com.example.videomeeting.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.activities.UsersListCallsActivity;
import com.example.videomeeting.adapters.recyclerviews.RecentCallsAdapter;
import com.example.videomeeting.models.Call;
import com.example.videomeeting.models.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.FIREBASE_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CALLS;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.PREF_DEFAULT;

public class RecentCallsFragment extends Fragment {

    private final DatabaseReference callsRef = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_CALLS)
                .child(FIREBASE_USER.getUid());
    private ChildEventListener incomingCallsListener;

    private List<User> userList;
    private Map<String, Call> callMap;

    private RecyclerView recentCallsRV;
    private RecentCallsAdapter recentCallsAdapter;
    private TextView errorMessageTV;
    private CardView welcomingCV;

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

    /**
     * Setups the RecyclerView
     * @param view fragment's inflater
     */
    private void setupRV(View view) {
        userList = new ArrayList<>();
        callMap = new LinkedHashMap<>();
        recentCallsAdapter = new RecentCallsAdapter(getContext(), userList, callMap);
        recentCallsRV = view.findViewById(R.id.callsRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recentCallsRV.setLayoutManager(linearLayoutManager);
        recentCallsRV.setAdapter(recentCallsAdapter);
    }

    /**
     * Setups the FloatingButton which opens the contacts list
     * @param view fragment's inflater
     */
    private void setupFab(View view) {
        view.findViewById(R.id.callsListFB).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UsersListCallsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Gets the recent calls from the user
     */
    private void getRecentCalls() {
        changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        incomingCallsListener = callsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Call call = snapshot.getValue(Call.class);
                        call.setTimestamp(Long.parseLong(snapshot.getKey()));
                        getUserFromCall(call);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorMessageTV.setText(error.getMessage());
                        changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
                    }
                });
    }

    /**
     * Gets the user from the call in order to bind his data into the views
     * @param call contains the ID of the users involved
     */
    private void getUserFromCall(Call call) {
        if (call.getCallerID().equals(FIREBASE_USER.getUid()) && !call.getReceiverID().contains("\n")) {
            fillLists(call.getReceiverID(), call);
        } else if (call.getReceiverID().contains("\n")) {
            User user = new User(
                    call.getReceiverID(),
                    "",
                    KEY_IMAGE_URL_DEFAULT
            );
            userList.add(user);
            callMap.put(String.valueOf(call.getTimestamp()), call);
        } else {
            fillLists(call.getCallerID(), call);
        }
    }

    /**
     * Fills the users and calls lists
     * @param userId The user get from the getUserFromCall()
     * @param call Call that matches the user
     */
    private void fillLists(String userId, Call call) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        user.setId(snapshot.getKey());
                        if (user != null) {
                            userList.add(user);
                            callMap.put(String.valueOf(call.getTimestamp()), call);
                            checkUserList();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                        changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
                    }
                });
    }

    /**
     * Checks the userList and depending on his size, the RecyclerView will be visible or not
     */
    private void checkUserList() {
        if (userList.size() > 0) {
            recentCallsAdapter = new RecentCallsAdapter(getContext(), userList, callMap);
            recentCallsRV.setAdapter(recentCallsAdapter);
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    /**
     * Changes the visibility of some views
     * @param recentCallsVis changes visibility of the RecyclerView
     * @param errorMessageVis changes visibility of the error message TextView
     * @param welcomingVis changes visibility of the welcoming message CardView
     */
    private void changeViewsVisibility(int recentCallsVis, int errorMessageVis, int welcomingVis) {
        recentCallsRV.setVisibility(recentCallsVis);
        errorMessageTV.setVisibility(errorMessageVis);
        welcomingCV.setVisibility(welcomingVis);
    }
}