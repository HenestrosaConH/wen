package com.example.videomeeting.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.recyclerviews.SearchMessageAdapter;
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.example.videomeeting.utils.Constants.FIREBASE_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_MESSAGES;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;

public class SearchMessageFragment extends Fragment {

    private final DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
            .child(KEY_COLLECTION_MESSAGES);
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
            .child(KEY_COLLECTION_USERS);

    private TextView notFoundTV;
    private RecyclerView searchMessageRV;
    private MaterialCardView searchTipCV;
    private ProgressBar searchPB;

    private List<Message> messageList;
    private List<User> userList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_message, container, false);
        notFoundTV = view.findViewById(R.id.notFoundTV);
        searchTipCV = view.findViewById(R.id.searchTipCV);
        setupRV(view);
        return view;
    }

    private void setupRV(View view) {
        messageList = new ArrayList<>();
        userList = new ArrayList<>();
        searchPB = view.findViewById(R.id.searchPB);
        searchMessageRV = view.findViewById(R.id.searchMessageRV);
        searchMessageRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        searchMessageRV.setLayoutManager(linearLayoutManager);
    }

    public void searchMessage(String query) {
        messageList.clear();
        userList.clear();
        setViewsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE);

        if (query != null && !TextUtils.isEmpty(query)) {
            messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getChildrenCount() > 0) {
                        for (DataSnapshot chatIDNode : snapshot.getChildren()) {
                            if (FIREBASE_USER.getUid().equals(chatIDNode.getKey().substring(0, 28))
                                    || FIREBASE_USER.getUid().equals(chatIDNode.getKey().substring(29, 57))) {
                                String remoteUserID;
                                if (FIREBASE_USER.getUid().equals(chatIDNode.getKey().substring(0, 28))) {
                                    remoteUserID = chatIDNode.getKey().substring(29, 57);
                                } else {
                                    remoteUserID = chatIDNode.getKey().substring(0, 28);
                                }
                                for (DataSnapshot timestampNode : chatIDNode.getChildren()) {
                                    boolean doesMessageFitQuery = false;
                                    Message message = timestampNode.getValue(Message.class);
                                    message.setTimestamp(Long.parseLong(timestampNode.getKey()));
                                    String[] wordsFromMessage = message.getMessage().split(" ");
                                    int i;
                                    for (i = 0; i < wordsFromMessage.length; i++) {
                                        if (wordsFromMessage[i].toLowerCase().startsWith(query.trim().toLowerCase())) {
                                            doesMessageFitQuery = true;
                                            break;
                                        }
                                    }
                                    if (doesMessageFitQuery) {
                                        messageList.add(message);
                                        usersRef.child(remoteUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);
                                                user.setId(snapshot.getKey());
                                                userList.add(user);
                                                checkList();
                                                Log.e("length", messageList.size()+"");
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    } else {
                                        setViewsVisibility(View.GONE, View.GONE, View.VISIBLE, View.GONE);
                                    }
                                }
                            }
                        }
                    } else {
                        setViewsVisibility(View.GONE, View.GONE, View.VISIBLE, View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            setViewsVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
        }
    }

    private void setViewsVisibility(int searchVis, int searchTipVis, int notFoundVis, int pbVis) {
        searchMessageRV.setVisibility(searchVis);
        searchTipCV.setVisibility(searchTipVis);
        notFoundTV.setVisibility(notFoundVis);
        searchPB.setVisibility(pbVis);
    }

    private void checkList() {
        refreshRV(messageList, userList);
        setViewsVisibility(View.VISIBLE, View.GONE, View.GONE, View.GONE);
    }

    private void refreshRV(@NotNull List<Message> searchedMessages,
                           @NotNull List<User> searchedMessagesUsers) {
        SearchMessageAdapter searchMessageAdapter = new SearchMessageAdapter(
                getContext(),
                searchedMessages,
                searchedMessagesUsers
        );
        searchMessageRV.setAdapter(searchMessageAdapter);
    }
}
