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

import static com.example.videomeeting.utils.Constants.BUNDLE_SEARCH;
import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_MESSAGE;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;

public class SearchMessageFragment extends Fragment {

    private final DatabaseReference messagesReference = FirebaseDatabase.getInstance().getReference()
            .child(KEY_COLLECTION_MESSAGE);
    private final DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference()
            .child(KEY_COLLECTION_USER);

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

    private void setupRV(View view) {
        messageList = new ArrayList<>();
        userList = new ArrayList<>();
        searchMessageRV = view.findViewById(R.id.searchMessageRV);
        searchMessageRV.setHasFixedSize(true);
        searchMessageRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void searchMessage(String query) {
        messageList = new ArrayList<>();
        userList = new ArrayList<>();

        messagesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean doesMessageFitQuery = false;
                    Message message = snapshot.getValue(Message.class);
                    String[] wordsFromMessage = message.getMessage().split(" ");
                    int i;
                    for (i = 0; i < wordsFromMessage.length; i++) {
                        if (wordsFromMessage[i].toLowerCase().startsWith(query.trim().toLowerCase())) {
                            doesMessageFitQuery = true;
                            break;
                        }
                    }

                    if (doesMessageFitQuery
                            && (message.getSenderID().equals(CURRENT_USER.getId())
                            || message.getReaderID().equals(CURRENT_USER.getId()))) {

                        String searchUserID;
                        if (message.getReaderID().equals(CURRENT_USER.getId())) {
                            searchUserID = message.getSenderID();
                        } else {
                            searchUserID = message.getReaderID();
                        }
                        messageList.add(message);

                        usersReference.child(searchUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                userList.add(user);
                                checkList();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    } else {
                        setViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void setViewsVisibility(int searchVis, int searchTipVis, int notFoundVis) {
        searchMessageRV.setVisibility(searchVis);
        searchTipCV.setVisibility(searchTipVis);
        notFoundTV.setVisibility(notFoundVis);
    }

    private void checkList() {
        if (messageList.size() > 0) {
            refreshRV(messageList, userList);
            setViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            setViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
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
