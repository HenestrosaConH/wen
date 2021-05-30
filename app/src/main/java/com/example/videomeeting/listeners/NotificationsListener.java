package com.example.videomeeting.listeners;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.example.videomeeting.R;
import com.example.videomeeting.activities.ChatActivity;
import com.example.videomeeting.activities.InvitationIncomingActivity;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import kotlin.random.Random;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.example.videomeeting.utils.Constants.*;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_NAME;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USER;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL;
import static com.example.videomeeting.utils.Constants.KEY_IMAGE_URL_DEFAULT;
import static com.example.videomeeting.utils.Constants.KEY_USER_ID;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_BODY;
import static com.example.videomeeting.utils.Constants.NOTIFICATION_TITLE;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_LIGHT;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_NOTIFICATION_URI;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_VIBRATION;
import static com.example.videomeeting.utils.Constants.PREF_DEFAULT;
import static com.example.videomeeting.utils.Constants.NOTIF_IS_GLOBAL;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_BLUE;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_CYAN;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_GREEN;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_PURPLE;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_RED;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_WHITE;
import static com.example.videomeeting.utils.Constants.NOTIF_LIGHT_YELLOW;
import static com.example.videomeeting.utils.Constants.NOTIF_VIBRATION_LONG;
import static com.example.videomeeting.utils.Constants.NOTIF_VIBRATION_SHORT;

public class NotificationsListener extends FirebaseMessagingService {

    private User user;
    private boolean isFound = false;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String type = remoteMessage.getData().get(INTENT_CALL_TYPE);

        if (type != null) {
            if (type.equals(REMOTE_MSG_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), InvitationIncomingActivity.class);
                intent.putExtra(
                        REMOTE_MSG_CALL_TYPE,
                        remoteMessage.getData().get(REMOTE_MSG_CALL_TYPE)
                );
                intent.putExtra(
                        KEY_USER_ID,
                        remoteMessage.getData().get(KEY_USER_ID)
                );
                intent.putExtra(
                        KEY_USERNAME,
                        remoteMessage.getData().get(KEY_USERNAME)
                );
                intent.putExtra(
                        KEY_IMAGE_URL,
                        remoteMessage.getData().get(KEY_IMAGE_URL)
                );
                intent.putExtra(
                        REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        REMOTE_MSG_GROUP,
                        remoteMessage.getData().get(REMOTE_MSG_GROUP)
                );

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (type.equals(REMOTE_MSG_INVITATION_RESPONSE)) {
                Intent intent = new Intent(REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else if (preferenceManager.getSharedPreferences().contains(NOTIF_IS_GLOBAL)
                && preferenceManager.getBoolean(NOTIF_IS_GLOBAL)) {

            Intent activityIntent = new Intent(this, ChatActivity.class);

            while(!isFound) {
                FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USER)
                        .child(remoteMessage.getData().get(KEY_USER_ID))
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                user = snapshot.getValue(User.class);
                                isFound = true;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            }
            activityIntent.putExtra(INTENT_USER, user);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    this,
                    0,
                    activityIntent,
                    FLAG_UPDATE_CURRENT
            );

        /*Reply function (need some more code from https://www.youtube.com/watch?v=DsFYPTnCbs8&t=9s&ab_channel=CodinginFlow)
        RemoteInput remoteInput = new RemoteInput.Builder(Constants.RESULT_KEY)
                .setLabel(getResources().getString(R.string.reply))
                .build();

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0,//should be the user id tho
                        new Intent(
                                getApplicationContext(),
                                ChatActivity.class
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_reply,
                        getResources().getString(R.string.reply),
                        replyPendingIntent
                ).addRemoteInput(remoteInput)
                .build();
        */
            Bitmap userImage = null;
            FutureTarget<Bitmap> futureTarget = null;
            if (!Objects.requireNonNull(remoteMessage.getData().get(KEY_IMAGE_URL)).equals(KEY_IMAGE_URL_DEFAULT)) {
                futureTarget = Glide.with(this)
                        .asBitmap()
                        .load(remoteMessage.getData().get(KEY_IMAGE_URL))
                        .submit();
                try {
                    userImage = futureTarget.get();
                } catch (Exception ignored) { }
            } else userImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_bordered);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_CHATS_NAME)
                    .setContentTitle(remoteMessage.getData().get(NOTIFICATION_TITLE))
                    .setContentText(remoteMessage.getData().get(NOTIFICATION_BODY))
                    .setSmallIcon(R.drawable.ic_launcher_icon_text)
                    .setLargeIcon(userImage)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(contentIntent);
                    //.addAction(replyAction)

            if (futureTarget != null) Glide.with(this).clear(futureTarget);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && CHANNEL_CHATS_ID == 1)
                createDefaultNotificationChannel(notificationManager);
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //LIGHTS
                int inMs = 500, onMs = 500;
                if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_LIGHT)) {
                    switch (preferenceManager.getString(CHANNEL_CHATS_LIGHT)) {
                        case NOTIF_LIGHT_RED:
                            builder.setLights(Color.RED, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_BLUE:
                            builder.setLights(Color.BLUE, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_YELLOW:
                            builder.setLights(Color.YELLOW, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_WHITE:
                            builder.setLights(Color.WHITE, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_PURPLE:
                            builder.setLights(Color.MAGENTA, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_GREEN:
                            builder.setLights(Color.GREEN, onMs, inMs);
                            break;
                        case NOTIF_LIGHT_CYAN:
                            builder.setLights(Color.CYAN, onMs, inMs);
                            break;
                        case PREF_DEFAULT:
                            builder.setDefaults(Notification.DEFAULT_LIGHTS);
                            break;
                    }
                } else builder.setDefaults(Notification.DEFAULT_LIGHTS);

                //VIBRATION
                if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_VIBRATION)) {
                    switch (preferenceManager.getString(CHANNEL_CHATS_VIBRATION)) {
                        case PREF_DEFAULT:
                            builder.setDefaults(Notification.DEFAULT_VIBRATE);
                        case NOTIF_VIBRATION_LONG:
                            builder.setVibrate(new long[] {0, 1000});
                        case NOTIF_VIBRATION_SHORT:
                            builder.setVibrate(new long[] {0, 250});
                        case PREF_OFF:
                            builder.setVibrate(null);
                    }
                } else builder.setDefaults(Notification.DEFAULT_VIBRATE);

                //SOUND
                Uri notificationUri;
                if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_NOTIFICATION_URI)) {
                    notificationUri = Uri.parse(preferenceManager.getString(CHANNEL_CHATS_NOTIFICATION_URI));
                } else notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(
                        notificationUri,
                        AudioManager.STREAM_NOTIFICATION
                );

                //Pop up on or off
                int importance = NotificationCompat.PRIORITY_HIGH;
                if (preferenceManager.getSharedPreferences().contains(CHANNEL_CHATS_IMPORTANCE)
                        && preferenceManager.getInt(CHANNEL_CHATS_IMPORTANCE) != 4)
                    importance = NotificationCompat.PRIORITY_DEFAULT;
                builder.setPriority(importance);
            }
            Notification notification = builder.build();
            int notificationID = Random.Default.nextInt();
            notificationManager.notify(notificationID, notification);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createDefaultNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                String.valueOf(CHANNEL_CHATS_ID),
                CHANNEL_CHATS_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
        );
        channel.setDescription(this.getString(R.string.channel_chat_description));

        notificationManager.createNotificationChannel(channel);
    }
}