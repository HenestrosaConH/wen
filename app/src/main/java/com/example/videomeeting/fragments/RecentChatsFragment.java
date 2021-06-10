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
import com.example.videomeeting.models.RecentChat;
import com.example.videomeeting.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.videomeeting.utils.Constants.*;

public class RecentChatsFragment extends Fragment {

    private final DatabaseReference recentChatsRef = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_RECENT_CHATS);
    private ValueEventListener incomingMessagesListener;

    //private Map<String, RecentChat> recentChatMap;
    private List<RecentChat> recentChatList;
    private List<User> remoteUserList;

    private RecyclerView recentChatsRV;
    private TextView errorMessageTV;
    private CardView welcomingCV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);

        errorMessageTV = view.findViewById(R.id.errorMessageTV);
        welcomingCV = view.findViewById(R.id.welcomingCV);
        setupRV(view);
        setupFab(view);
        getRecentChats();
        return view;
    }

    private void setupRV(View view) {
        recentChatList = new ArrayList<>();
        remoteUserList = new ArrayList<>();
        //recentChatsAdapter = new RecentChatsAdapter(getContext(), recentChatList, remoteUser);
        recentChatsRV = view.findViewById(R.id.recentChatsRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recentChatsRV.setLayoutManager(linearLayoutManager);
        //recentChatsRV.setAdapter(recentChatsAdapter);
    }

    private void setupFab(View view) {
        view.findViewById(R.id.usersListFB).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UsersListChatsActivity.class);
            startActivity(intent);
        });
    }

    private void getRecentChats() {
        changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        incomingMessagesListener = recentChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int[] i = {0};
                recentChatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.e("getRecentChats", dataSnapshot.getKey()+"");
                    String[] usersID = dataSnapshot.getKey().split("-");
                    if (FIREBASE_USER.getUid().equals(usersID[0]) || FIREBASE_USER.getUid().equals(usersID[1])) {
                        RecentChat recentChat = dataSnapshot.getValue(RecentChat.class);
                        if (FIREBASE_USER.getUid().equals(usersID[0])) {
                            recentChat.setRemoteUserID(usersID[1]);
                        } else {
                            recentChat.setRemoteUserID(usersID[0]);
                        }
                        recentChatList.add(recentChat);
                    }
                    if (++i[0] == snapshot.getChildrenCount()) {
                        checkChatList();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
            }
        });
    }

    private void getRemoteUser() {
        remoteUserList.clear();
        for (RecentChat recentChat : recentChatList) {
            FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                    .child(recentChat.getRemoteUserID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User remoteUser = snapshot.getValue(User.class);
                            remoteUser.setId(snapshot.getKey());
                            Log.e("snaoshot", snapshot.getKey()+"");
                            remoteUserList.add(remoteUser);
                            if (remoteUserList.size() == recentChatList.size()) {
                                recentChatsRV.setAdapter(new RecentChatsAdapter(
                                        getContext(),
                                        recentChatList,
                                        remoteUserList)
                                );
                                changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                            changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
                        }
                    });
        }
    }

    private void checkChatList() {
        sortListAlphab(); //Ordering by timestamp
        getRemoteUser();
    }

    private void sortListAlphab() {
        Collections.sort(recentChatList, (recentChatList, rc1) -> {
            long s1 = recentChatList.getTimestamp();
            long s2 = rc1.getTimestamp();
            return Long.compare(s1, s2);
        });
    }

    private void changeViewsVisibility(int recentChatsVis, int errorMessageVis, int welcomingVis) {
        recentChatsRV.setVisibility(recentChatsVis);
        errorMessageTV.setVisibility(errorMessageVis);
        welcomingCV.setVisibility(welcomingVis);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (incomingMessagesListener != null) {
            recentChatsRef.removeEventListener(incomingMessagesListener);
        }
    }
}