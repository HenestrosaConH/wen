package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.RecentChat;
import com.example.videomeeting.models.User;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.FIREBASE_USER;
import static com.example.videomeeting.utils.Constants.INTENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_MESSAGES;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_RECENT_CHATS;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;
import static com.example.videomeeting.utils.Constants.KEY_CONTACTED_USERS;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_CONTACTS;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_ONLINE;
import static com.example.videomeeting.utils.Constants.KEY_LAST_SEEN_ALL;
import static com.example.videomeeting.utils.Constants.KEY_SEEN;
import static com.example.videomeeting.utils.Constants.KEY_USER_ID;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_BODY;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_TITLE;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_TO;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_DATA;

public class ChatActivity extends AppCompatActivity {

    //Firebase
    private final DatabaseReference contactsRef = FirebaseDatabase.getInstance()
            .getReference(KEY_COLLECTION_USERS)
            .child(FIREBASE_USER.getUid())
            .child(KEY_CONTACTED_USERS);
    private final DatabaseReference messagesRef = FirebaseDatabase.getInstance()
            .getReference(KEY_COLLECTION_MESSAGES);
    private final DatabaseReference recentChatsRef = FirebaseDatabase.getInstance()
            .getReference(KEY_COLLECTION_RECENT_CHATS);
    private final CallsListener callsListener = new CallsListener();
    private String chatRoomKey;
    private ChildEventListener messagesListener, statusMessageListener;
    private ValueEventListener statusUserListener;

    private List<Message> messageList;
    private User remoteUser;
    private boolean isContact = false;
    private boolean shouldShareLastSeen = false;
    private boolean shouldCheckKey = true;

    private TextView lastSeenTV;
    private MessagesAdapter messagesAdapter;
    private MenuItem contactIT;
    private CardView noMessagesCV;
    private RecyclerView messagesRV;

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

        chatRoomKey = FIREBASE_USER.getUid() + "-" + remoteUser.getId();
        getMessages(chatRoomKey);
        checkUserStatus();
    }

    /**
     * Setups the last seen of the user in the views
     * */
    private void setupLastSeen() {
        lastSeenTV = findViewById(R.id.lastSeenTV);
        if (remoteUser.getLastSeenStatus().equals(KEY_LAST_SEEN_ALL)) {
            formatLastSeen(remoteUser.getLastSeen());
            shouldShareLastSeen = true;
        } else if (remoteUser.getLastSeenStatus().equals(KEY_LAST_SEEN_CONTACTS)) {
            for (Map.Entry<String, Boolean> contactedUser : remoteUser.getContacts().entrySet()) {
                if (contactedUser.getKey().equals(FIREBASE_USER.getUid()) && contactedUser.getValue()) {
                    shouldShareLastSeen = true;
                    break;
                }
            }
        }
    }

    /**
     * Setups the toolbar of the activity
     * */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        TextView usernameTV = findViewById(R.id.usernameTV);
        usernameTV.setText(remoteUser.getUserName());

        LinearLayout imageLY = findViewById(R.id.imageLY);
        imageLY.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Setups the bottom layout, which contains the message TextView, the emojis ImageView and the send ImageView
     * */
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

    /**
     * Setups the RecyclerView
     * */
    private void setupRV() {
        messageList = new ArrayList<>();
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
        if (CURRENT_USER.getContacts() != null) {
            for (Map.Entry<String, Boolean> contactedUser : CURRENT_USER.getContacts().entrySet()) {
                if (contactedUser.getKey().equals(remoteUser.getId()) && contactedUser.getValue()) {
                    setContactIT(true);
                }
            }
        } else {
            setContactIT(false);
        }
        //noinspection RestrictedApi
        m.setOptionalIconsVisible(true);
        return true;
    }

    /**
     * Checks and sets the contact Item from the ActionBar depending if the user is contact or not
     * @param isRemoveEnabled If true, when we click on the MenuItem, the user will be removed
     * */
    private void setContactIT(boolean isRemoveEnabled) {
        if (isRemoveEnabled) {
            contactIT.setTitle(R.string.delete_contact);
            contactIT.setIcon(R.drawable.ic_contact_delete);
            isContact = true;
        } else {
            contactIT.setTitle(R.string.add_contact);
            contactIT.setIcon(R.drawable.ic_contact_add);
            isContact = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.callIT) {
            callsListener.initiateCall(remoteUser, ChatActivity.this);
            return true;
        } else if (item.getItemId() == R.id.videocallIT) {
            callsListener.initiateVideoCall(remoteUser, ChatActivity.this);
            return true;
        } else if (item.getItemId() == R.id.contactIT) {
            isContact = !isContact;
            if (isContact) {
                addUserToContacts();
            } else {
                removeUserFromContacts();
            }
            setContactIT(isContact);
            return true;
        }
        return false;
    }

    /**
     * Setups the profile picture of the user.
     * */
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

    /**
     * SimpleDateFormat that shows the date without the years
     * */
    private DateFormat getShortDateInstanceWithoutYears() {
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        sdf.toLocalizedPattern().replaceAll("y", "");
        return sdf;
    }

    /**
     * Gets the messages exchanged with the user from Firebase
     * @param chatRoomKey The key of the node which contains all the messages between us and the user
     * */
    private void getMessages(String chatRoomKey) {
        if (shouldCheckKey) {
            messagesRef.child(chatRoomKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getChildrenCount() == 0) {
                        ChatActivity.this.chatRoomKey = remoteUser.getId() + "-" + FIREBASE_USER.getUid();
                        getMessages(ChatActivity.this.chatRoomKey);
                        shouldCheckKey = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        messagesRV.setVisibility(View.GONE);
        noMessagesCV.setVisibility(View.VISIBLE);
        messagesListener = messagesRef.child(chatRoomKey)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        message.setTimestamp(Long.parseLong(snapshot.getKey()));
                        if (!message.getSenderID().equals(FIREBASE_USER.getUid()) && !message.getSeen()) {
                            message.setSeen(true);
                            recentChatsRef.child(chatRoomKey).child(KEY_SEEN).setValue(true);
                            messagesRef.child(chatRoomKey).child(snapshot.getKey()).child(KEY_SEEN).setValue(true);
                        }
                        messageList.add(message);
                        /*
                        if (messageList.size() == snapshot.getChildrenCount()) {
                            checkMessageStatus(chatRoomKey);
                        }*/
                        checkMessageList();
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

                    }
                });
    }

    /**
     * For every iteration in getMessages(), we load the message in the adapter of the RecyclerView
     * */
    private void checkMessageList() {
        messagesAdapter = new MessagesAdapter(messageList);
        messagesRV.setAdapter(messagesAdapter);
        messagesRV.setVisibility(View.VISIBLE);
        noMessagesCV.setVisibility(View.GONE);
    }

    /**
     * Every time we click on the send ImageView, this method will be called. We create the object and store it on the DB
     * @param messageText The text that will be send to the user
     * */
    private void sendMessage(String messageText) {
        Message message = new Message(
            FIREBASE_USER.getUid(),
            messageText,
            false
        );
        long timestamp = System.currentTimeMillis();
        message.setTimestamp(timestamp);
        if (chatRoomKey == null) {
            chatRoomKey = FIREBASE_USER.getUid() + "-" + remoteUser.getId();
        }
        messagesRef.child(chatRoomKey).child(String.valueOf(timestamp)).setValue(message);

        RecentChat recentChat = new RecentChat(
            messageText,
            timestamp,
            FIREBASE_USER.getUid(),
            false
        );
        recentChatsRef.child(chatRoomKey).setValue(recentChat);
        setNotificationData(messageText);
    }

    /**
     * If the user is not in the contacts list and we add him, this method will store him on out contacts list in Firebase
     * */
    private void addUserToContacts() {
        HashMap<String, Boolean> contactedUserMap = new HashMap<>();
        if (CURRENT_USER.getContacts() != null) {
            if (CURRENT_USER.getContacts().get(remoteUser.getId()) == null) {
                contactedUserMap = CURRENT_USER.getContacts();
                contactedUserMap.put(remoteUser.getId(), true);
            }
        } else {
            contactedUserMap.put(remoteUser.getId(), true);
        }
        CURRENT_USER.setContacts(contactedUserMap);
        contactsRef.setValue(contactedUserMap);
    }

    /**
     * If the user is in the contacts list and we remove him, this method will remove him off our contacts list in Firebase
     * */
    private void removeUserFromContacts() {
        HashMap<String, Boolean> contactedUserMap = CURRENT_USER.getContacts();
        contactedUserMap.remove(remoteUser.getId());
        CURRENT_USER.setContacts(contactedUserMap);
        contactsRef.setValue(contactedUserMap);
    }

    /**
     * When we send a message, we set the notification data that will be send to the user
     * @param messageText We get the message text that will be visible on the notifications
     * */
    private void setNotificationData(String messageText) {
        JSONObject body = new JSONObject();
        try {
            JSONObject data = new JSONObject();
            data.put(NOTIFICATION_TITLE, CURRENT_USER.getUserName());
            data.put(NOTIFICATION_BODY, messageText);
            data.put(KEY_IMAGE_URL, CURRENT_USER.getImageURL());
            data.put(KEY_USER_ID, FIREBASE_USER.getUid());

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

    /**
     * Once we set the notification data, we send the notification to him
     * @param body The JSON body which contains the notification data
     */
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

    /**
     * We put a ValueEventListener in order to show the current status of the user
     */
    private void checkUserStatus() {
        statusUserListener = FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                .child(remoteUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            remoteUser = snapshot.getValue(User.class);
                            remoteUser.setId(snapshot.getKey());
                            if (shouldShareLastSeen || remoteUser.getLastSeen().equals(KEY_LAST_SEEN_ONLINE)) {
                                formatLastSeen(remoteUser.getLastSeen());
                                lastSeenTV.setVisibility(View.VISIBLE);
                            } else {
                                lastSeenTV.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    /**
     * We format the status of the user depending on how much time has passed since the last seen
     * @param lastSeenString Timestamp string that we format
     */
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
        if (messagesRef != null) {
            messagesRef.removeEventListener(statusUserListener);
            messagesRef.removeEventListener(messagesListener);
            messagesListener = null;
            statusUserListener = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    //For notifications purposes
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    /**
     * When we click in the notification, we get the intent from it in order to open the ChatActivity with that user
     */
    private void processExtraData(){
        remoteUser = (User) getIntent().getSerializableExtra(INTENT_USER);
    }
}