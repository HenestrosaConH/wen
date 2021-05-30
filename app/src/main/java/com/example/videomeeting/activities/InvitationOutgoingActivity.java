package com.example.videomeeting.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.apis.ApiClient;
import com.example.videomeeting.apis.ApiService;
import com.example.videomeeting.models.Call;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Callback;
import retrofit2.Response;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.INTENT_ARE_MULTIPLE_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_AUDIO;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_VIDEO;
import static com.example.videomeeting.utils.Constants.INTENT_SELECTED_USERS;
import static com.example.videomeeting.utils.Constants.INTENT_USER;
import static com.example.videomeeting.utils.Constants.KEY_CALL_MISSED;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_CALL;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.KEY_USERNAME;
import static com.example.videomeeting.utils.Constants.KEY_USER_ID;
import static com.example.videomeeting.utils.Constants.REMOTE_BASE_SERVER_URL;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_CALL_TYPE;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_DATA;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_GROUP;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITATION;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITATION_ACCEPTED;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITATION_CANCELLED;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITATION_REJECTED;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITATION_RESPONSE;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_INVITER_TOKEN;
import static com.example.videomeeting.utils.Constants.REMOTE_MSG_REGISTRATION_IDS;

public class InvitationOutgoingActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String callType = null;
    private String group = null;

    private MediaPlayer mediaPlayer;
    private TextView defaultProfileTV, usernameTV;

    private int rejectionCount = 0;
    private int totalReceivers = 0;
    private User user;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_outgoing);

        mediaPlayer = MediaPlayer.create(InvitationOutgoingActivity.this, R.raw.ringback_tone);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.pause();
            Toast.makeText(InvitationOutgoingActivity.this, getString(R.string.not_available), Toast.LENGTH_SHORT).show();
            if (user != null)
                cancelInvitation(user.fcmToken, null);
        });
        mediaPlayer.start();

        preferenceManager = new PreferenceManager(getApplicationContext());

        TextView outgoingTV = findViewById(R.id.outgoingTV);
        ImageView callTypeIV = findViewById(R.id.callTypeIV);
        callType = getIntent().getStringExtra(INTENT_CALL_TYPE);

        if (callType != null) {
            if (callType.equals(INTENT_CALL_TYPE_VIDEO)) {
                callTypeIV.setImageResource(R.drawable.ic_videocam);
                outgoingTV.setText(getString(R.string.outgoing_videocall));
            } else {
                callTypeIV.setImageResource(R.drawable.ic_call);
                outgoingTV.setText(getString(R.string.outgoing_call));
            }
        }

        defaultProfileTV = findViewById(R.id.defaultProfileTV);
        usernameTV = findViewById(R.id.usernameTV);

        user = (User) getIntent().getSerializableExtra(INTENT_USER);
        if (user != null) {
            if (user.getImageURL().equals(KEY_IMAGE_URL_DEFAULT))
                defaultProfileTV.setText(user.getUserName().substring(0,1));
            else {
                defaultProfileTV.setVisibility(View.GONE);
                ImageView profileIV = findViewById(R.id.profileIV);
                profileIV.setVisibility(View.VISIBLE);
                Glide.with(InvitationOutgoingActivity.this)
                        .load(user.getImageURL())
                        .circleCrop()
                        .into(profileIV);
            }
            usernameTV.setText(user.getUserName());
        }

        ImageView rejectIV = findViewById(R.id.rejectIV);
        rejectIV.setOnClickListener(v -> {
            mediaPlayer.pause();
            if (getIntent().getBooleanExtra(INTENT_ARE_MULTIPLE_SELECTED_USERS, false)) {
                Type type = new TypeToken<ArrayList<User>>(){}.getType();
                ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra(INTENT_SELECTED_USERS), type);
                cancelInvitation(null, receivers);
            } else if (user != null)
                cancelInvitation(user.fcmToken, null);
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();
                if (callType != null) {
                    if (getIntent().getBooleanExtra(INTENT_ARE_MULTIPLE_SELECTED_USERS, false)) {
                        Type type = new TypeToken<ArrayList<User>>(){}.getType();
                        ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra(INTENT_SELECTED_USERS), type);
                        if (receivers != null)
                            totalReceivers = receivers.size();
                        initiateCall(callType, null, receivers);
                    } else {
                        if (user != null) {
                            totalReceivers = 1;
                            initiateCall(callType, user.fcmToken, null);
                        }
                    }
                }
            }
        });
    }

    private void initiateCall(String callType, String receiverToken, ArrayList<User> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receivers != null) tokens.put(receiverToken);
            //If group call
            if (receivers != null && receivers.size() > 0) {
                StringBuilder userNames = new StringBuilder();
                int i;
                for (i = 0; i < receivers.size(); i++) {
                    tokens.put(receivers.get(i).fcmToken);
                    userNames.append(receivers.get(i).getUserName()).append("\n");
                }
                defaultProfileTV.setVisibility(View.GONE);
                usernameTV.setText(userNames.toString());
            }

            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(INTENT_CALL_TYPE, REMOTE_MSG_INVITATION);
            data.put(REMOTE_MSG_CALL_TYPE, callType);
            data.put(KEY_USER_ID, CURRENT_USER.getUserName());
            data.put(KEY_USERNAME, CURRENT_USER.getUserName());
            data.put(KEY_IMAGE_URL, CURRENT_USER.getImageURL());
            data.put(REMOTE_MSG_INVITER_TOKEN, inviterToken);

            group =
                    preferenceManager.getString(KEY_USER_ID) + "_" +
                            UUID.randomUUID().toString() .substring(0, 5);
            data.put(REMOTE_MSG_GROUP, group);

            body.put(REMOTE_MSG_DATA, data);
            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), REMOTE_MSG_INVITATION);

            String date = String.valueOf(System.currentTimeMillis());
            Call call = new Call(
                    CURRENT_USER.getId(),
                    user.getId(),
                    date,
                    false,
                    callType
            );

            int randomId = (int) (Math.random() * 1000);
            id = date + "-" + randomId;
            FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_CALL)
                    .child(id)
                    .setValue(call);

        } catch (Exception e) {
            Toast.makeText(InvitationOutgoingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (!type.equals(REMOTE_MSG_INVITATION)) {
                        mediaPlayer.pause();
                        Toast.makeText(InvitationOutgoingActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
                        setCallMissedTrue();
                        finish();
                    }
                } else {
                    mediaPlayer.pause();
                    Toast.makeText(InvitationOutgoingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call call, @NonNull Throwable t) {
                mediaPlayer.pause();
                Toast.makeText(InvitationOutgoingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken, ArrayList<User> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null) tokens.put(receiverToken);
            if (receivers != null && receivers.size() > 0) {
                for (User user : receivers)
                    tokens.put(user.fcmToken);
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(INTENT_CALL_TYPE, REMOTE_MSG_INVITATION_RESPONSE);
            data.put(REMOTE_MSG_INVITATION_RESPONSE, REMOTE_MSG_INVITATION_CANCELLED);

            body.put(REMOTE_MSG_DATA, data);
            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), REMOTE_MSG_INVITATION_RESPONSE);

            mediaPlayer.pause();
            setCallMissedTrue();
        } catch (Exception e) {
            mediaPlayer.pause();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL(REMOTE_BASE_SERVER_URL);

                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(group);
                        if (callType.equals(INTENT_CALL_TYPE_AUDIO))
                            builder.setVideoMuted(true);

                        mediaPlayer.pause();
                        JitsiMeetActivity.launch(InvitationOutgoingActivity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        mediaPlayer.pause();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(REMOTE_MSG_INVITATION_REJECTED)) {
                    rejectionCount++;
                    if (rejectionCount == totalReceivers) {
                        Toast.makeText(context, getString(R.string.rejected), Toast.LENGTH_SHORT).show();
                        mediaPlayer.pause();
                        setCallMissedTrue();
                        finish();
                    }
                }
            }
        }
    };

    private void setCallMissedTrue() {
        FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_CALL)
                .child(id)
                .child(KEY_CALL_MISSED)
                .setValue(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}