package com.example.videomeeting.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.videomeeting.R;
import com.example.videomeeting.activities.UsersListChatsActivity;
import com.example.videomeeting.adapters.recyclerviews.RecentChatsAdapter;
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.videomeeting.utils.Constants.*;

public class RecentChatsFragment extends Fragment {

    private final DatabaseReference chatReference = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_MESSAGE);
    private ValueEventListener incomingMessagesListener;

    private Map<String, User> userMap;
    private Map<String, Message> messageMap;

    private RecyclerView recentChatsRV;
    private RecentChatsAdapter recentChatsAdapter;
    private TextView errorMessageTV;
    private CardView welcomingCV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);

        errorMessageTV = view.findViewById(R.id.errorMessageTV);
        welcomingCV = view.findViewById(R.id.welcomingCV);
        setupRV(view);

        view.findViewById(R.id.usersListFB).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UsersListChatsActivity.class);
            startActivity(intent);
        });

        getRecentChats();
        return view;
    }

    private void setupRV(View view) {
        userMap = new LinkedHashMap<>();
        messageMap = new LinkedHashMap<>();
        recentChatsAdapter = new RecentChatsAdapter(getContext(), userMap, messageMap);
        recentChatsRV = view.findViewById(R.id.recentChatsRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recentChatsRV.setLayoutManager(linearLayoutManager);
        recentChatsRV.setAdapter(recentChatsAdapter);
    }

    private void getRecentChats() {
        incomingMessagesListener = chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
                } else {
                    userMap.clear();
                    messageMap.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        getUserFromChat(dataSnapshot.getValue(Message.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorMessageTV.setText(error.getMessage());
                changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            }
        });
    }

    private void getUserFromChat(Message message) {
        if (message.getReaderID().equals(FIREBASE_USER.getUid())) {
            fillLists(message.getSenderID(), message);
        } else if (message.getSenderID().equals(FIREBASE_USER.getUid())) {
            fillLists(message.getReaderID(), message);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void fillLists(String userId, Message message) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USER)
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Log.e("date", message.getTimestamp()+"");
                            userMap.put(user.getId(), user);
                            messageMap.put(user.getId(), message);
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

    private void checkUserList() {
        if (userMap.size() > 0) {
            recentChatsAdapter.notifyDataSetChanged();
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void changeViewsVisibility(int recentChatsVis, int errorMessageVis, int welcomingVis) {
        recentChatsRV.setVisibility(recentChatsVis);
        errorMessageTV.setVisibility(errorMessageVis);
        welcomingCV.setVisibility(welcomingVis);
    }

    @Override
    public void onStart() {
        super.onStart();
        chatReference.addListenerForSingleValueEvent(incomingMessagesListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (incomingMessagesListener != null) {
            chatReference.removeEventListener(incomingMessagesListener);
        }
    }
}