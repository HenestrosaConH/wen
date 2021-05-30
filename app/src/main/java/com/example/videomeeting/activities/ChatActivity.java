package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.adapters.recyclerviews.MessagesAdapter;
import com.example.videomeeting.apis.ApiClient;
import com.example.videomeeting.apis.ApiService;
import com.example.videomeeting.listeners.CallsListener;
import com.example.videomeeting.models.Contact;
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.INTENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CONTACTED_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_MESSAGE;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.KEY_IS_CONTACT;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_CONTACTS;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_ONLINE;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_TRUE;
import static com.example.videomeeting.utils.Constants.KEY_SEEN;
import static com.example.videomeeting.utils.Constants.KEY_USER_ID;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_BODY;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_TITLE;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_TO;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_DATA;

public class ChatActivity extends AppCompatActivity {

    //Firebase
    /*private final DatabaseReference myContactedUsersRef = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_CONTACTED_USER)
                .child(CURRENT_USER.getId());*/
    private final DatabaseReference chatsReference = FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_MESSAGE);
    private final CallsListener callsListener = new CallsListener();
    //Models
    private List<Message> messageList;
    private User remoteUser;
    private boolean checkContactedUserAgain = true;
    private boolean isContact = false;
    private boolean shouldShareLastSeen = false;
    //Components
    private TextView lastSeenTV;
    private MessagesAdapter messagesAdapter;
    private MenuItem contactIT;
    private CardView noMessagesCV;
    private RecyclerView messagesRV;
    private ValueEventListener statusMessageEvent, statusUserEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        processExtraData();

        setupToolbar();
        setupLastSeen();
        setupProfilePic();
        setupBottomLayout();
        setupRV();

        getMessages();
        checkMessageStatus();
        checkUserStatus();
    }

    private void setupLastSeen() {
        lastSeenTV = findViewById(R.id.lastSeenTV);
        if (remoteUser.isLastSeenEnabled().equals(KEY_LAST_SEEN_TRUE)) {
            formatLastSeen(remoteUser.getLastSeen());
            shouldShareLastSeen = true;
        } else if (remoteUser.isLastSeenEnabled().equals(KEY_LAST_SEEN_CONTACTS)) {
            FirebaseDatabase.getInstance()
                    .getReference(KEY_COLLECTION_CONTACTED_USER)
                    .child(remoteUser.getId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot == null
                                        || dataSnapshot.getKey().equals(CURRENT_USER.getId())
                                        && dataSnapshot.getValue(Contact.class).isContact()) {
                                    shouldShareLastSeen = true;
                                    break;
                                }
                            }
                            //checkUserStatus();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        TextView usernameTV = findViewById(R.id.usernameTV);
        usernameTV.setText(remoteUser.getUserName());

        LinearLayout imageLY = findViewById(R.id.imageLY);
        imageLY.setOnClickListener(v -> onBackPressed());
    }

    private void setupBottomLayout() {
        ImageView emojiIV = findViewById(R.id.emojiIV);
        View layout = findViewById(R.id.layout);
        EmojiconEditText messageET = findViewById(R.id.messageET);
        EmojIconActions emojIconActions = new EmojIconActions(
                ChatActivity.this,
                layout,
                messageET,
                emojiIV
        );
        emojIconActions.ShowEmojIcon();

        ImageView sendIV = findViewById(R.id.sendIV);
        sendIV.setOnClickListener(view -> {
            String message = messageET.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
            messageET.setText("");
        });
    }

    private void setupRV() {
        messagesRV = findViewById(R.id.messagesRV);
        messagesRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true); //Messages will appear from the bottom to the top
        messagesRV.setLayoutManager(linearLayoutManager);
        noMessagesCV = findViewById(R.id.noMessagesCV);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuBuilder m = (MenuBuilder) menu;
        contactIT = menu.findItem(R.id.contactIT);
        myContactedUsersRef.child(remoteUser.getId())
                .child(KEY_IS_CONTACT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null && (boolean) snapshot.getValue()) {
                            contactIT.setTitle(R.string.delete_contact);
                            contactIT.setIcon(R.drawable.ic_contact_delete);
                            isContact = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        //noinspection RestrictedApi
        m.setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.callIT) {
            callsListener.initiateCall(remoteUser, ChatActivity.this);
            return true;
        } else if (item.getItemId() == R.id.videocallIT) {
            callsListener.initiateVideocall(remoteUser, ChatActivity.this);
            return true;
        } else if (item.getItemId() == R.id.contactIT) {
            isContact = !isContact;
            myContactedUsersRef
                    .child(remoteUser.getId())
                    .child(KEY_IS_CONTACT)
                    .setValue(isContact);
            if (isContact) {
                contactIT.setTitle(R.string.delete_contact);
                contactIT.setIcon(R.drawable.ic_contact_delete);
                isContact = true;
            } else {
                contactIT.setTitle(R.string.add_contact);
                contactIT.setIcon(R.drawable.ic_contact_add);
                isContact = false;
            }
            return true;
        }
        return false;
    }

    private void setupProfilePic() {
        ImageView profileIV = findViewById(R.id.profileIV);
        TextView defaultProfileTV = findViewById(R.id.defaultProfileTV);
        if (remoteUser.getImageURL().equals(KEY_IMAGE_URL_DEFAULT)) {
            defaultProfileTV.setText(remoteUser.getUserName().substring(0, 1));
        } else {
            defaultProfileTV.setVisibility(View.GONE);
            profileIV.setVisibility(View.VISIBLE);
            Glide.with(ChatActivity.this)
                    .load(remoteUser.getImageURL())
                    .circleCrop()
                    .into(profileIV);
        }
    }

    private DateFormat getShortDateInstanceWithoutYears() {
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        sdf.toLocalizedPattern().replaceAll("y", "");
        return sdf;
    }

    private void sendMessage(String messageText) {
        String date = String.valueOf(System.currentTimeMillis());
        Message message = new Message(
            CURRENT_USER.getId(),
            remoteUser.getId(),
            messageText,
            date,
            false
        );

        int randomId = (int) (Math.random() * 1000);
        String id = date + "-" + randomId;
        chatsReference.child(id)
                .setValue(message);

        setNotificationData(message);

        if (checkContactedUserAgain) {
            addUserToRecentChats();
        }
    }

    private void addUserToRecentChats() {
        //If we haven't talked with the user before, we add him to our recent chats
        myContactedUsersRef.child(remoteUser.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            myContactedUsersRef
                                    .child(remoteUser.getId())
                                    .child(KEY_IS_CONTACT)
                                    .setValue(false);
                        }
                        checkContactedUserAgain = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setNotificationData(Message message) {
        //Notification notification = new Notification(remoteUser.getUserName(), message, remoteUser.fcmToken);
        JSONObject body = new JSONObject();
        try {
            JSONObject data = new JSONObject();
            data.put(NOTIFICATION_TITLE, CURRENT_USER.getUserName());
            data.put(NOTIFICATION_BODY, message);
            data.put(KEY_IMAGE_URL, CURRENT_USER.getImageURL());
            data.put(KEY_USER_ID, CURRENT_USER.getId());

            if (remoteUser.getFcmToken() != null) {
                body.put(NOTIFICATION_TO, remoteUser.getFcmToken());
            } else {
                body.put(NOTIFICATION_TO, "");
            }
            body.put(REMOTE_MSG_DATA, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendNotification(body);
    }

    private void sendNotification(JSONObject body) {
        ApiClient.getClient().create(ApiService.class).sendNotification(
                body.toString()
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) { }
            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {}
        });
    }

    private void getMessages() {
        messageList = new ArrayList<>();
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    messagesRV.setVisibility(View.GONE);
                    noMessagesCV.setVisibility(View.VISIBLE);
                } else {
                    messageList.clear();
                    Message message;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        message = dataSnapshot.getValue(Message.class);
                        assert message != null;
                        if (message.getReaderID().equals(CURRENT_USER.getId())
                                && message.getSenderID().equals(remoteUser.getId())
                                || message.getReaderID().equals(remoteUser.getId())
                                && message.getSenderID().equals(CURRENT_USER.getId())) {
                            messageList.add(message);
                        }
                    }
                    if (messageList.size() > 0) {
                        messagesAdapter = new MessagesAdapter(messageList);
                        messagesRV.setAdapter(messagesAdapter);
                        messagesRV.setVisibility(View.VISIBLE);
                        noMessagesCV.setVisibility(View.GONE);
                    } else {
                        messagesRV.setVisibility(View.GONE);
                        noMessagesCV.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void checkMessageStatus() {
        statusMessageEvent = chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Message message;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        message = dataSnapshot.getValue(Message.class);
                        if (message.getReaderID().equals(CURRENT_USER.getId())
                                && message.getSenderID().equals(remoteUser.getId())) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put(KEY_SEEN, true);
                            dataSnapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkUserStatus() {
        statusUserEvent = FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USER)
                .child(remoteUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        remoteUser = snapshot.getValue(User.class);
                        assert remoteUser != null;
                        if (shouldShareLastSeen || remoteUser.getLastSeen().equals(KEY_LAST_SEEN_ONLINE)) {
                            formatLastSeen(remoteUser.getLastSeen());
                            lastSeenTV.setVisibility(View.VISIBLE);
                        } else {
                            lastSeenTV.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void formatLastSeen(String lastSeenString) {
        if (lastSeenString.equals(KEY_LAST_SEEN_ONLINE)) {
            lastSeenTV.setText(KEY_LAST_SEEN_ONLINE);
        } else {
            Date lastDate = new Date(Long.parseLong(lastSeenString));
            Date currentDay = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            long lastSeen = Long.parseLong(lastSeenString);
            if (lastSeen < System.currentTimeMillis() - 31536000000L) { //last seen more than a year ago
                lastSeenTV.setText(
                        String.format("%s %s",
                                getString(R.string.last_seen).toLowerCase(),
                                DateFormat.getDateInstance(
                                        DateFormat.SHORT,
                                        Locale.getDefault()
                                ).format(lastSeen))
                );
            } else if (lastSeen > System.currentTimeMillis() - 31536000000L
                    && lastSeen < System.currentTimeMillis() - 172800000) { //last seen three days ago or more but less than a year
                DateFormat df = getShortDateInstanceWithoutYears();
                lastSeenTV.setText(
                        String.format("%s %s %s %s",
                                getString(R.string.last_seen).toLowerCase(),
                                df.format(lastSeen), getString(R.string.at),
                                DateFormat.getTimeInstance(
                                        DateFormat.SHORT,
                                        Locale.getDefault()
                                ).format(lastSeen)
                        )
                );
            } else if (!formatter.format(currentDay).equals(formatter.format(lastDate))) {//last seen yesterday
                lastSeenTV.setText(
                        String.format("%s %s",
                                getString(R.string.last_seen_yesterday_at),
                                DateFormat.getTimeInstance(
                                        DateFormat.SHORT,
                                        Locale.getDefault()
                                ).format(lastSeen))
                );
            } else if (formatter.format(currentDay).equals(formatter.format(lastDate))) {//last seen today
                lastSeenTV.setText(
                        String.format("%s %s",
                                getString(R.string.last_seen_today_at),
                                DateFormat.getTimeInstance(
                                        DateFormat.SHORT,
                                        Locale.getDefault()
                                ).format(lastSeen)
                        )
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        processExtraData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (statusMessageEvent != null) {
            chatsReference.removeEventListener(statusMessageEvent);
        }
        if (statusUserEvent != null) {
            chatsReference.removeEventListener(statusUserEvent);
        }
    }

    //For notifications purposes
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData(){
        remoteUser = (User) getIntent().getSerializableExtra(INTENT_USER);
    }
}