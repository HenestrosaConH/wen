package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Map;
import java.util.Objects;

import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_VIDEO;
import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.INTENT_ARE_MULTIPLE_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.INTENT_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;

public class UsersListCallsActivity extends AppCompatActivity implements OnMultipleCallsListener {

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
        UsersListCallsActivity.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        errorMessageTV = findViewById(R.id.errorMessageTV);
        noContactsCV = findViewById(R.id.noContactsCV);
        setupRV();
        getContacts();
    }

    private void setupRV() {
        usersList = new ArrayList<>();
        usersListCallsAdapter = new UsersListCallsAdapter(UsersListCallsActivity.this, usersList, this);
        allUsersRV = findViewById(R.id.allUsersRV);
        allUsersRV.setLayoutManager(new LinearLayoutManager(UsersListCallsActivity.this));
        allUsersRV.setAdapter(usersListCallsAdapter);
        BubbleScrollBar scrollBar = findViewById(R.id.scrollbar);
        scrollBar.attachToRecyclerView(allUsersRV);
        scrollBar.setBubbleTextProvider(i -> usersListCallsAdapter.userList.get(i).getUserName()); //dunno
    }

    private void getContacts() {
        if (CURRENT_USER.getContacts() != null) {
            usersList.clear();
            for (Map.Entry<String, Boolean> contactedUser : CURRENT_USER.getContacts().entrySet()) {
                if (contactedUser.getValue()) {
                    getContactData(contactedUser.getKey());
                }
            }
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void getContactData(String userID) {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        user.setId(snapshot.getKey());
                        usersList.add(user);
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
            sortListAlphab();
            usersListCallsAdapter.notifyDataSetChanged();
            changeViewsVisibility(View.VISIBLE, View.GONE, View.GONE);
        } else {
            changeViewsVisibility(View.GONE, View.GONE, View.VISIBLE);
        }
    }

    private void sortListAlphab() {
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
            if (isGroupEnabled) {
                Intent intent = new Intent(UsersListCallsActivity.this, InvitationOutgoingActivity.class);
                intent.putExtra(INTENT_SELECTED_USERS, new Gson().toJson(usersListCallsAdapter.getSelectedUsers()));
                intent.putExtra(INTENT_CALL_TYPE, INTENT_CALL_TYPE_VIDEO);
                intent.putExtra(INTENT_ARE_MULTIPLE_SELECTED_USERS, true);
                startActivity(intent);
            } else {
                Toast.makeText(UsersListCallsActivity.this, getString(R.string.group_disabled_tip), Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        return true;
    }
}
