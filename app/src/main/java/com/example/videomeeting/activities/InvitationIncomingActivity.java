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
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_incoming);

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

        TextView defaultProfileTV = findViewById(R.id.defaultProfileTV);
        String imageURL = getIntent().getStringExtra(KEY_IMAGE_URL);
        TextView usernameTV = findViewById(R.id.usernameTV);
        String username = getIntent().getStringExtra(KEY_USERNAME);

        if (username != null && imageURL != null) {
            if (imageURL.equals(KEY_IMAGE_URL_DEFAULT))
                defaultProfileTV.setText(username.substring(0,1));
            else {
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

        preferenceManager = new PreferenceManager(getApplicationContext());
        ringPhone();
        vibratePhone();

        ImageView acceptIV = findViewById(R.id.acceptIV);
        acceptIV.setOnClickListener(v -> {
            sendInvitationResponse(
                    REMOTE_MSG_INVITATION_ACCEPTED,
                    getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN)
            );
            vibrator.cancel();
            ringtone.stop();
        });

        ImageView rejectIV = findViewById(R.id.rejectIV);
        rejectIV.setOnClickListener(v -> {
            sendInvitationResponse(
                    REMOTE_MSG_INVITATION_REJECTED,
                    getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN)
            );
            ringtone.stop();
            vibrator.cancel();
        });
    }

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

    private void ringPhone() {
        Uri ringtonePath;
        if (preferenceManager.getSharedPreferences().contains(CHANNEL_CALLS_RINGTONE_URI)) {
            ringtonePath = Uri.parse(new PreferenceManager(InvitationIncomingActivity.this).getString(CHANNEL_CALLS_RINGTONE_URI));
        } else {
            ringtonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtonePath == null)
                ringtonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtonePath);
        ringtone.play();
        vibratePhone();
    }

    private void vibratePhone() {
        int ringerMode = ((AudioManager) getSystemService(AUDIO_SERVICE)).getRingerMode();
        if(ringerMode == AudioManager.RINGER_MODE_SILENT) return;

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milis = 250;
        if (preferenceManager.getSharedPreferences().contains(CHANNEL_CALLS_VIBRATION)) {
            switch (preferenceManager.getString(CHANNEL_CALLS_VIBRATION)) {
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

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null && type.equals(REMOTE_MSG_INVITATION_CANCELLED))
                cancelCallResult(getString(R.string.cancelled));
        }
    };

    private void cancelCallResult(String toastMessage) {
        Toast.makeText(InvitationIncomingActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        ringtone.stop();
        vibrator.cancel();
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