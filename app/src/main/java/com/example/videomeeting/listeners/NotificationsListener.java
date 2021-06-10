package com.example.videomeeting.listeners;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.app.NotificationManagerCompat;
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

import kotlin.random.Random;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.example.videomeeting.utils.Constants.*;
import static com.example.videomeeting.utils.Constants.CHANNEL_CHATS_NAME;
import static com.example.videomeeting.utils.Constants.KEY_COLLECTION_USERS;
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

    //TODO clean code

    private User remoteUser;
    private boolean isFound = false;
    private FutureTarget<Bitmap> futureTarget;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PreferenceManager prefManager = new PreferenceManager(getApplicationContext());
        String type = remoteMessage.getData().get(INTENT_CALL_TYPE);

        if (type != null) {
            if (type.equals(REMOTE_MSG_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), InvitationIncomingActivity.class);
                putExtras(intent, remoteMessage);
                startActivity(intent);
            } else if (type.equals(REMOTE_MSG_INVITATION_RESPONSE)) {
                Intent intent = new Intent(REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else if (!prefManager.getSharedPreferences().contains(NOTIF_IS_GLOBAL) || prefManager.getBoolean(NOTIF_IS_GLOBAL)) {
            Intent activityIntent = new Intent(this, ChatActivity.class);
            searchUser(remoteMessage);

            activityIntent.putExtra(INTENT_USER, remoteUser);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            Bitmap userPic = getUserPic(remoteMessage);

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, String.valueOf(CHANNEL_CHATS_ID))
                    .setSmallIcon(R.drawable.ic_launcher_icon_text)
                    .setContentTitle(remoteMessage.getData().get(NOTIFICATION_TITLE))
                    .setContentText(remoteMessage.getData().get(NOTIFICATION_BODY))
                    .setGroupSummary(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setGroup(remoteUser.getId())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(NOTIFICATION_BODY)))
                    .setContentIntent(contentIntent);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(CHANNEL_CHATS_ID))
                    .setContentTitle(remoteMessage.getData().get(NOTIFICATION_TITLE))
                    .setContentText(remoteMessage.getData().get(NOTIFICATION_BODY))
                    .setSmallIcon(R.drawable.ic_launcher_icon_text)
                    .setLargeIcon(userPic)
                    //.setOnlyAlertOnce(true)
                    //.setAutoCancel(true)
                    .setGroup(remoteUser.getId())
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    //.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(contentIntent);
                    //.addAction(replyAction)

            if (futureTarget != null) {
                Glide.with(this).clear(futureTarget);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //&& CHANNEL_CHATS_ID == 1) {
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
                createDefaultNotificationChannel(notificationManager);
            } else {//if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setNotifLights(prefManager, notificationBuilder);
                setNotifVibration(prefManager, notificationBuilder);
                setNotifSound(prefManager, notificationBuilder);
                setNotifPopup(prefManager, notificationBuilder);
            }

            int summaryID = Integer.parseInt(remoteUser.getId().replaceAll("\\D+",""));
            notificationManager.notify(summaryID, summaryBuilder.build());
            int notificationID = Random.Default.nextInt();
            notificationManager.notify(notificationID, notificationBuilder.build());
        }
    }

    private void putExtras(Intent intent, RemoteMessage remoteMessage) {
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
    }

    private void searchUser(RemoteMessage remoteMessage) {
        while(!isFound) {
            FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                    .child(remoteMessage.getData().get(KEY_USER_ID))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            remoteUser = snapshot.getValue(User.class);
                            remoteUser.setId(snapshot.getKey());
                            isFound = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    }

    private Bitmap getUserPic(RemoteMessage remoteMessage) {
        Bitmap userPic = null;
        if (!remoteMessage.getData().get(KEY_IMAGE_URL).equals(KEY_IMAGE_URL_DEFAULT)) {
            futureTarget = Glide.with(this)
                    .asBitmap()
                    .load(remoteMessage.getData().get(KEY_IMAGE_URL))
                    .submit();
            try {
                userPic = futureTarget.get();
            } catch (Exception ignored) { }
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_user_bordered, null);
            userPic = drawableToBitmap(drawable);
        }
        return userPic;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void setNotifLights(PreferenceManager prefManager, NotificationCompat.Builder builder) {
        int inMs = 500, onMs = 500;
        if (prefManager.getSharedPreferences().contains(CHANNEL_CHATS_LIGHT)) {
            switch (prefManager.getString(CHANNEL_CHATS_LIGHT)) {
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
        } else {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        }
    }

    private void setNotifVibration(PreferenceManager prefManager, NotificationCompat.Builder builder) {
        if (prefManager.getSharedPreferences().contains(CHANNEL_CHATS_VIBRATION)) {
            switch (prefManager.getString(CHANNEL_CHATS_VIBRATION)) {
                case PREF_DEFAULT:
                    builder.setDefaults(Notification.DEFAULT_VIBRATE);
                case NOTIF_VIBRATION_LONG:
                    builder.setVibrate(new long[] {0, 1000});
                case NOTIF_VIBRATION_SHORT:
                    builder.setVibrate(new long[] {0, 250});
                case PREF_OFF:
                    builder.setVibrate(null);
            }
        } else {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
    }

    private void setNotifSound(PreferenceManager prefManager, NotificationCompat.Builder builder) {
        Uri notificationUri;
        if (prefManager.getSharedPreferences().contains(CHANNEL_CHATS_NOTIFICATION_URI)) {
            notificationUri = Uri.parse(prefManager.getString(CHANNEL_CHATS_NOTIFICATION_URI));
        } else notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(
                notificationUri,
                AudioManager.STREAM_NOTIFICATION
        );
    }

    private void setNotifPopup(PreferenceManager prefManager, NotificationCompat.Builder builder) {
        int importance = NotificationCompat.PRIORITY_HIGH;
        if (prefManager.getSharedPreferences().contains(CHANNEL_CHATS_IMPORTANCE)
                && prefManager.getInt(CHANNEL_CHATS_IMPORTANCE) != 4) {
            importance = NotificationCompat.PRIORITY_DEFAULT;
        }
        builder.setPriority(importance);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createDefaultNotificationChannel(NotificationManagerCompat notificationManager) {
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