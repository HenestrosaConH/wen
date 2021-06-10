package com.example.videomeeting.adapters.recyclerviews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.activities.ChatActivity;
import com.example.videomeeting.listeners.CallsListener;
import com.example.videomeeting.models.Call;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.videomeeting.utils.Constants.*;

public class RecentCallsAdapter extends RecyclerView.Adapter<RecentCallsAdapter.CallViewHolder> {

    private final CallsListener callsListener = new CallsListener();
    private final Context context;
    private final List<User> userList;
    private final Map<String, Call> callMap;

    private final List<User> selectedUsers;
    private Dialog profileDG;

    public RecentCallsAdapter(Context context, List<User> userList, Map<String, Call> callMap) {
        this.context = context;
        this.userList = userList;
        this.callMap = callMap;
        selectedUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CallViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_recent_call,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        String keyMessage = (String) callMap.keySet().toArray()[position];
        holder.setIsRecyclable(false);
        holder.bind(userList.get(position), callMap.get(keyMessage));
        profileDG = new Dialog(context, R.style.ThemeDialog);
    }

    private void showProfileCard(User user) {
        profileDG.setContentView(R.layout.dialog_profile);
        profileDG.setCancelable(true);

        ImageView profileIV = profileDG.findViewById(R.id.profileIV);
        TextView defaultProfileTV = profileDG.findViewById(R.id.defaultProfileTV);

        if (user.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
            defaultProfileTV.setText(user.getUserName().substring(0,1));
        } else {
            defaultProfileTV.setVisibility(View.GONE);
            profileIV.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getImageURL())
                    .centerCrop()
                    .into(profileIV);
        }

        TextView usernameTV = profileDG.findViewById(R.id.usernameTV);
        usernameTV.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        usernameTV.setSelected(true);
        usernameTV.setSingleLine(true);
        usernameTV.setText(user.getUserName());

        TextView aboutTV = profileDG.findViewById(R.id.aboutTV);
        if (user.getAbout() != null) aboutTV.setText(user.getAbout());
        else aboutTV.setVisibility(View.GONE);

        profileDG.findViewById(R.id.chatLY).setOnClickListener(v -> {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra(Constants.INTENT_USER, user);
            context.startActivity(i);
        });

        profileDG.findViewById(R.id.callLY).setOnClickListener(v -> callsListener.initiateCall(user, context));
        profileDG.findViewById(R.id.videocallLY).setOnClickListener(v -> callsListener.initiateVideoCall(user, context));

        profileDG.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profileDG.show();
    }

    @Override
    public int getItemCount() {
        return callMap.size();
    }

    class CallViewHolder extends RecyclerView.ViewHolder {
        TextView defaultProfileTV, usernameTV, dateTV;
        ImageView profileIV,
                callIV, videoCallIV,
                selectedUserIV, groupIV,
                missedCallIV, missedVideoCallIV,
                outgoingCallIV, incomingCallIV,
                onlineIV, offlineIV;
        CardView statusCV;
        ConstraintLayout itemCallLY;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            profileIV = itemView.findViewById(R.id.profileIV);
            dateTV = itemView.findViewById(R.id.dateTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            callIV = itemView.findViewById(R.id.callIV);
            videoCallIV = itemView.findViewById(R.id.videocallIV);
            missedCallIV = itemView.findViewById(R.id.missedCallIV);
            missedVideoCallIV = itemView.findViewById(R.id.missedVideoCallIV);
            outgoingCallIV = itemView.findViewById(R.id.outgoingCallIV);
            incomingCallIV = itemView.findViewById(R.id.incomingCallIV);
            selectedUserIV = itemView.findViewById(R.id.selectedUserIV);
            groupIV = itemView.findViewById(R.id.groupIV);
            itemCallLY = itemView.findViewById(R.id.itemCallLY);
            statusCV = itemView.findViewById(R.id.statusCV);
            onlineIV = itemView.findViewById(R.id.onlineIV);
            offlineIV = itemView.findViewById(R.id.offlineIV);
        }

        void bind(User user, Call call) {
            if (call.getReceiverID().contains("\n")) {
                groupIV.setVisibility(View.VISIBLE);
                statusCV.setVisibility(View.GONE);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault());
                dateTV.setText(dateFormat.format(call.getTimestamp()));

                usernameTV.setText(user.getUserName().replace("\n", ", "));
                usernameTV.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                usernameTV.setMarqueeRepeatLimit(-1);
                usernameTV.setSelected(true);
            } else {
                if (user.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
                    defaultProfileTV.setText(user.getUserName().substring(0, 1));
                    defaultProfileTV.setVisibility(View.VISIBLE);
                } else {
                    profileIV.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(user.getImageURL())
                            .circleCrop()
                            .into(profileIV);
                }

                if (user.getLastSeen().equals(Constants.KEY_LAST_SEEN_ONLINE)) {
                    onlineIV.setVisibility(View.VISIBLE);
                    offlineIV.setVisibility(View.GONE);
                } else {
                    onlineIV.setVisibility(View.GONE);
                    offlineIV.setVisibility(View.VISIBLE);
                }

                defaultProfileTV.setText(user.getUserName().substring(0, 1));
                usernameTV.setText(user.getUserName());

                callIV.setOnClickListener(v -> callsListener.initiateCall(user, context));
                videoCallIV.setOnClickListener(v -> callsListener.initiateVideoCall(user, context));

                profileIV.setOnClickListener(v -> showProfileCard(user));
                defaultProfileTV.setOnClickListener(v -> showProfileCard(user));

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault());
                dateTV.setText(dateFormat.format(call.getTimestamp()));

                if (call.getCallType().equals(INTENT_CALL_TYPE_AUDIO)) {
                    callIV.setVisibility(View.VISIBLE);
                } else {
                    videoCallIV.setVisibility(View.VISIBLE);
                }

                if (call.getCallerID().equals(FIREBASE_USER.getUid())) { //Outgoing call
                    outgoingCallIV.setVisibility(View.VISIBLE);
                    if (call.getMissed()) {
                        outgoingCallIV.setColorFilter(context.getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        outgoingCallIV.setColorFilter(context.getResources().getColor(android.R.color.holo_green_light));
                    }
                } else { //Incoming call
                    if (call.getMissed()) {
                        if (call.getCallType().equals(INTENT_CALL_TYPE_VIDEO)) {
                            missedVideoCallIV.setVisibility(View.VISIBLE);
                        } else {
                            missedCallIV.setVisibility(View.VISIBLE);
                        }
                    } else {
                        incomingCallIV.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}