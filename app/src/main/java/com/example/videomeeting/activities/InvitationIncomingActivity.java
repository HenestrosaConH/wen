package com.example.videomeeting.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.example.videomeeting.utils.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.videomeeting.utils.Constants.*;

public class InvitationIncomingActivity extends AppCompatActivity {

    private String callType = null;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_incoming);

        prefManager = new PreferenceManager(getApplicationContext());
        setCallType();
        bindUserData();

        ringPhone();
        vibratePhone();

        setupAcceptIV();
        setupRejectIV();
    }

    /**
     * Checks if the invitation is a call or a video call
     */
    private void setCallType() {
        TextView incomingTV = findViewById(R.id.incomingTV);
        ImageView callTypeIV = findViewById(R.id.callTypeIV);
        callType = getIntent().getStringExtra(REMOTE_MSG_CALL_TYPE);
        if (callType != null) {
            if (callType.equals(INTENT_CALL_TYPE_VIDEO)) {
                callTypeIV.setImageResource(R.drawable.ic_videocam);
                incomingTV.setText(getString(R.string.incoming_videocall));
            } else {
                callTypeIV.setImageResource(R.drawable.ic_call);
                incomingTV.setText(getString(R.string.incoming_call));
            }
        }
    }

    /**
     * Loads the user data into the views of the layout
     */
    private void bindUserData() {
        TextView defaultProfileTV = findViewById(R.id.defaultProfileTV);
        String imageURL = getIntent().getStringExtra(KEY_IMAGE_URL);
        TextView usernameTV = findViewById(R.id.usernameTV);
        String username = getIntent().getStringExtra(KEY_USERNAME);
        if (username != null && imageURL != null) {
            if (username.contains("\n")) {
                defaultProfileTV.setVisibility(View.GONE);
                findViewById(R.id.groupIV).setVisibility(View.VISIBLE);
                String[] usernames = username.split("\n");
                StringBuilder filteredUsernames = new StringBuilder();
                for (String filteredUsername : usernames) {
                    if (!filteredUsername.equals(CURRENT_USER.getUserName())) {
                        filteredUsernames.append(filteredUsername).append("\n");
                    }
                }
                usernameTV.setText(filteredUsernames.toString());
            } else {
                if (imageURL.equals(KEY_IMAGE_URL_DEFAULT)) {
                    defaultProfileTV.setText(username.substring(0, 1));
                } else {
                    defaultProfileTV.setVisibility(View.GONE);
                    ImageView profileIV = findViewById(R.id.profileIV);
                    profileIV.setVisibility(View.VISIBLE);
                    Glide.with(InvitationIncomingActivity.this)
                            .load(imageURL)
                            .circleCrop()
                            .into(profileIV);
                }
                usernameTV.setText(username);
            }
        }
    }

    /**
     * Sets the behaviour of the accept ImageView
     */
    private void setupAcceptIV() {
        ImageView acceptIV = findViewById(R.id.acceptIV);
        acceptIV.setOnClickListener(v -> {
            sendInvitationResponse(
                    REMOTE_MSG_INVITATION_ACCEPTED,
                    getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN)
            );
            stopRingtone();
            stopVibrator();
        });
    }

    /**
     * Sets the behaviour of the reject ImageView
     */
    private void setupRejectIV() {
        ImageView rejectIV = findViewById(R.id.rejectIV);
        rejectIV.setOnClickListener(v -> {
            sendInvitationResponse(
                    REMOTE_MSG_INVITATION_REJECTED,
                    getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN)
            );
            stopRingtone();
            stopVibrator();
        });
    }

    /**
     * Stops the ringtone from ringing
     */
    private void stopRingtone() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    /**
     * Stops the vibrator from vibrating
     */
    private void stopVibrator() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    /**
     * We send the invitation once the type and the receiver token have been get
     * @param type Type of invitation
     * @param receiverToken Determines who will receive the call
     */
    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(INTENT_CALL_TYPE, REMOTE_MSG_INVITATION_RESPONSE);
            data.put(REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(REMOTE_MSG_DATA, data);
            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Stops the ringtone from ringing
     */
    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(REMOTE_MSG_INVITATION_ACCEPTED)) {
                        try {
                            URL serverURL = new URL(REMOTE_BASE_SERVER_URL);

                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(REMOTE_MSG_GROUP));
                            if (callType.equals(INTENT_CALL_TYPE_AUDIO))
                                builder.setVideoMuted(true);

                            JitsiMeetActivity.launch(InvitationIncomingActivity.this, builder.build());
                            finish();
                        } catch (Exception e) {
                            cancelCallResult(e.getMessage());
                        }
                    } else cancelCallResult(getString(R.string.rejected));
                } else cancelCallResult(response.message());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                cancelCallResult(t.getMessage());
            }
        });
    }

    /**
     * Makes the phone ring
     */
    private void ringPhone() {
        Uri ringtonePath;
        if (prefManager.getSharedPreferences().contains(CHANNEL_CALLS_RINGTONE_URI)) {
            ringtonePath = Uri.parse(new PreferenceManager(InvitationIncomingActivity.this).getString(CHANNEL_CALLS_RINGTONE_URI));
        } else {
            ringtonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtonePath == null) {
                ringtonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtonePath);
        ringtone.play();
        vibratePhone();
    }

    /**
     * Makes the phone vibrate
     */
    private void vibratePhone() {
        int ringerMode = ((AudioManager) getSystemService(AUDIO_SERVICE)).getRingerMode();
        if(ringerMode == AudioManager.RINGER_MODE_SILENT) return;

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milis = 250;
        if (prefManager.getSharedPreferences().contains(CHANNEL_CALLS_VIBRATION)) {
            switch (prefManager.getString(CHANNEL_CALLS_VIBRATION)) {
                case PREF_DEFAULT:
                    milis = 250;
                    break;
                case NOTIF_VIBRATION_LONG:
                    milis = 1000;
                    break;
                case NOTIF_VIBRATION_SHORT:
                    milis = 100;
                    break;
                case PREF_OFF:
                    milis = 0;
                    break;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, milis, milis}, 0));
        else
            vibrator.vibrate(new long[]{0, milis, milis}, 0);
    }

    /**
     * Setups a BroadcastReceiver in order to listen to the response of the user(s) that have received the call
     */
    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null && type.equals(REMOTE_MSG_INVITATION_CANCELLED)) {
                cancelCallResult(getString(R.string.cancelled));
            }
        }
    };

    /**
     * If the call if rejected, the activity, the ringtone and the vibrator are finished
     */
    private void cancelCallResult(String toastMessage) {
        Toast.makeText(InvitationIncomingActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        stopRingtone();
        stopVibrator();
        finish();
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
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}