package com.example.videomeeting.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.recyclerviews.UsersListChatsAdapter;
import com.example.videomeeting.models.Contact;
import com.example.videomeeting.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trendyol.bubblescrollbarlib.BubbleScrollBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CONTACTED_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;

public class UsersListChatsActivity extends AppCompatActivity {

    private List<User> usersList;
    private UsersListChatsAdapter usersListChatsAdapter;
    private TextView errorMessageTV;
    private CardView noContactsCV;
    private RecyclerView allUsersRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        setTitle(getString(R.string.my_contacts));
        Objects.requireNonNull(UsersListChatsActivity.this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        errorMessageTV = findViewById(R.id.errorMessageTV);
        noContactsCV = findViewById(R.id.noContactsCV);
        BubbleScrollBar scrollBar = findViewById(R.id.scrollbar);

        usersList = new ArrayList<>();
        usersListChatsAdapter = new UsersListChatsAdapter(UsersListChatsActivity.this, usersList);
        allUsersRV = findViewById(R.id.allUsersRV);
        allUsersRV.setLayoutManager(new LinearLayoutManager(UsersListChatsActivity.this));
        allUsersRV.setAdapter(usersListChatsAdapter);
        scrollBar.attachToRecyclerView(allUsersRV);
        scrollBar.setBubbleTextProvider(i -> usersListChatsAdapter.userList.get(i).getUserName()); //dunno

        getContacts();
    }

    private void getContacts() {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_CONTACTED_USER)
                .child(CURRENT_USER.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {
                            noContactsCV.setVisibility(View.VISIBLE);
                        } else {
                            usersList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getValue(Contact.class).isContact()) {
                                    fillUserList(dataSnapshot);
                                } else {
                                    checkUserList();
                                }
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

    private void fillUserList(DataSnapshot dataSnapshot) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USER)
                .child(dataSnapshot.getKey()) //userID
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersList.add(snapshot.getValue(User.class));
                        checkUserList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorMessageTV.setText(String.format("%s", getString(R.string.no_users_available)));
                        changeViewsVisibility(View.GONE, View.VISIBLE, View.GONE);
                    }
                });
    }

    private void checkUserList() {
        if (usersList.size() > 0) {
            sortUsersAlphab();
            usersListChatsAdapter.notifyDataSetChanged();
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void sortUsersAlphab() {
        Collections.sort(usersList, (user, u1) -> {
            String s1 = user.getUserName();
            String s2 = u1.getUserName();
            return s1.compareToIgnoreCase(s2);
        });
    }

    private void changeViewsVisibility(int allUsersVis, int errorMessageVis, int noContactsVis) {
        allUsersRV.setVisibility(allUsersVis);
        errorMessageTV.setVisibility(errorMessageVis);
        noContactsCV.setVisibility(noContactsVis);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}