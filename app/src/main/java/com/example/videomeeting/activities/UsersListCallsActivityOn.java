package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.recyclerviews.UsersListCallsAdapter;
import com.example.videomeeting.listeners.OnMultipleCallsListener;
import com.example.videomeeting.models.Contact;
import com.example.videomeeting.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.trendyol.bubblescrollbarlib.BubbleScrollBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_VIDEO;
import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.INTENT_ARE_MULTIPLE_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.INTENT_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CONTACTED_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;

public class UsersListCallsActivityOn extends AppCompatActivity implements OnMultipleCallsListener {

    public List<User> usersList;
    private UsersListCallsAdapter usersListCallsAdapter;
    private boolean isGroupEnabled = false;
    private TextView errorMessageTV;
    private CardView noContactsCV;
    private RecyclerView allUsersRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        setTitle(getString(R.string.my_contacts));
        Objects.requireNonNull(UsersListCallsActivityOn.this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        errorMessageTV = findViewById(R.id.errorMessageTV);
        noContactsCV = findViewById(R.id.noContactsCV);
        BubbleScrollBar scrollBar = findViewById(R.id.scrollbar);

        usersList = new ArrayList<>();
        usersListCallsAdapter = new UsersListCallsAdapter(UsersListCallsActivityOn.this, usersList, this);
        allUsersRV = findViewById(R.id.allUsersRV);
        allUsersRV.setLayoutManager(new LinearLayoutManager(UsersListCallsActivityOn.this));
        allUsersRV.setAdapter(usersListCallsAdapter);
        scrollBar.attachToRecyclerView(allUsersRV);
        scrollBar.setBubbleTextProvider(i -> usersListCallsAdapter.userList.get(i).getUserName()); //dunno

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
            Collections.sort(usersList, (user, u1) -> {
                String s1 = user.getUserName();
                String s2 = u1.getUserName();
                return s1.compareToIgnoreCase(s2);
            });
            usersListCallsAdapter.notifyDataSetChanged();
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
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

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        //groupIT.setEnabled(isMultipleUsersSelected);
        isGroupEnabled = isMultipleUsersSelected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lists, menu);
        MenuItem groupIT = menu.findItem(R.id.groupIT);
        groupIT.setOnMenuItemClickListener(item -> {
            if (!isGroupEnabled)
                Toast.makeText(UsersListCallsActivityOn.this, getString(R.string.group_disabled_tip), Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent(UsersListCallsActivityOn.this, InvitationOutgoingActivity.class);
                intent.putExtra(INTENT_SELECTED_USERS, new Gson().toJson(usersListCallsAdapter.getSelectedUsers()));
                intent.putExtra(INTENT_CALL_TYPE, INTENT_CALL_TYPE_VIDEO);
                intent.putExtra(INTENT_CALL_TYPE, INTENT_CALL_TYPE_VIDEO);
                intent.putExtra(INTENT_ARE_MULTIPLE_SELECTED_USERS, true);
                startActivity(intent);
            }
            return false;
        });
        return true;
    }
}
