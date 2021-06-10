package com.example.videomeeting.listeners;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.videomeeting.R;
import com.example.videomeeting.activities.InvitationOutgoingActivity;
import com.example.videomeeting.models.User;

import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_AUDIO;
import static com.example.videomeeting.utils.Constants.INTENT_CALL_TYPE_VIDEO;
import static com.example.videomeeting.utils.Constants.INTENT_USER;

public class CallsListener {

    public CallsListener() {}

    public void initiateVideoCall(User user, Context context) {
        if (user.getFcmToken() == null || user.getFcmToken().trim().isEmpty())
            Toast.makeText(context, user.getUserName() + " " + context.getString(R.string.is_not_available), Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(context, InvitationOutgoingActivity.class);
            intent.putExtra(INTENT_USER, user);
            intent.putExtra(INTENT_CALL_TYPE, INTENT_CALL_TYPE_VIDEO);
            context.startActivity(intent);
        }
    }

    public void initiateCall(User user, Context context) {
        if (user.getFcmToken() == null || user.getFcmToken().trim().isEmpty())
            Toast.makeText(context, user.getUserName() + " " + context.getString(R.string.is_not_available), Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(context, InvitationOutgoingActivity.class);
            intent.putExtra(INTENT_USER, user);
            intent.putExtra(INTENT_CALL_TYPE, INTENT_CALL_TYPE_AUDIO);
            context.startActivity(intent);
        }
    }

}