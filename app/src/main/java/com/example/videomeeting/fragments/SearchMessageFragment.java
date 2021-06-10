package com.example.videomeeting.fragments;

import android.os.Bundle;
import android.text.TextUtils;
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

    /**
     * Setups the RecyclerView
     * @param view fragment's inflater
     */
    private void setupRV(View view) {
        messageList = new ArrayList<>();
        userList = new ArrayList<>();
        searchMessageRV = view.findViewById(R.id.searchMessageRV);
        searchMessageRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        searchMessageRV.setLayoutManager(linearLayoutManager);
    }

    /**
     * Gets the query and searches on the messages node to retrieve the results
     * @param query Text contained on the message searched by the user
     */
    public void searchMessage(String query) {
        messageList.clear();
        userList.clear();

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
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    } else {
                                        changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                    }
                                }
                            }
                        }
                    } else {
                        changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
        }
    }

    /**
     * Changes the visibility of some views
     * @param searchVis changes visibility of the RecyclerView
     * @param searchTipVis changes visibility of the tip CardView
     * @param notFoundVis changes visibility of the not found message TextView
     */
    private void changeViewsVisibility(int searchVis, int searchTipVis, int notFoundVis) {
        searchMessageRV.setVisibility(searchVis);
        searchTipCV.setVisibility(searchTipVis);
        notFoundTV.setVisibility(notFoundVis);
    }

    /**
     * Checks the messageList and refreshes the RecyclerView
     */
    private void checkList() {
        if (messageList.size() > 0) {
            refreshRV(messageList, userList);
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    /**
     * Refreshes the RecyclerView if new values have been found
     * @param searchedMessages Contains the results from the query
     * @param searchedMessagesUsers Contains the user that sent the message from the list above
     */
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
