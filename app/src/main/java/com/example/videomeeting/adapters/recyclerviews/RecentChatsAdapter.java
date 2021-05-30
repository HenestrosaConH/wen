package com.example.videomeeting.adapters.recyclerviews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.activities.ChatActivity;
import com.example.videomeeting.listeners.CallsListener;
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class RecentChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Map<String, User> userMap;
    private final Map<String, Message> messageMap;
    private final CallsListener callsListener = new CallsListener();
    private Dialog profileDG;

    //Result codes
    public static final int ITEM_MSG_SENT = 0;
    public static final int ITEM_MSG_RECEIVED = 1;

    public RecentChatsAdapter(Context context, Map<String, User> userMap, Map<String, Message> messageMap) {
        this.context = context;
        this.userMap = userMap;
        this.messageMap = messageMap;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_MSG_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recent_chat_sent, parent, false);
                return new RecentChatsAdapter.MessageSentHolder(view);
            case ITEM_MSG_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recent_chat_received, parent, false);
                return new RecentChatsAdapter.MessageReceivedHolder(view);
            /*case ITEM_MSG_RECEIVED_GROUP:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_msg_received_group, parent, false);
                return new MessageReceivedGroupHolder(view);*/
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        profileDG = new Dialog(context, R.style.ThemeDialog);
        String keyUser = (String) userMap.keySet().toArray()[position];
        String keyMessage = (String) messageMap.keySet().toArray()[position];
        Log.e("getDate", messageMap.get(keyMessage).getTimestamp());
        switch (holder.getItemViewType()) {
            case ITEM_MSG_SENT:
                ((RecentChatsAdapter.MessageSentHolder) holder).bind(userMap.get(keyUser), messageMap.get(keyMessage));
                break;
            case ITEM_MSG_RECEIVED:
                ((RecentChatsAdapter.MessageReceivedHolder) holder).bind(userMap.get(keyUser), messageMap.get(keyMessage));
                break;
            /*case ITEM_MSG_RECEIVED_GROUP:
                ((MessageReceivedGroupHolder) holder).bind(chat);
                break;*/
        }
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
        profileDG.findViewById(R.id.videocallLY).setOnClickListener(v -> callsListener.initiateVideocall(user, context));

        profileDG.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profileDG.show();
    }

    @Override
    public int getItemCount() {
        return messageMap.size();
    }

    @Override
    public int getItemViewType(int position) {
        String keyMessage = (String) messageMap.keySet().toArray()[position];
        if (messageMap.get(keyMessage).getSenderID().equals(Constants.CURRENT_USER.getId())) {
            return ITEM_MSG_SENT;
        } else {
            return ITEM_MSG_RECEIVED;
        }
    }

    /////////////
    class MessageReceivedHolder extends RecyclerView.ViewHolder {
        public TextView defaultProfileTV, usernameTV, dateLastMesTV, previewMessageTV;
        public ImageView profileIV, onlineIV, offlineIV;

        public MessageReceivedHolder(@NonNull View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            dateLastMesTV = itemView.findViewById(R.id.dateLastMesTV);
            previewMessageTV = itemView.findViewById(R.id.previewMessageTV);
            profileIV = itemView.findViewById(R.id.profileIV);
            onlineIV = itemView.findViewById(R.id.onlineIV);
            offlineIV = itemView.findViewById(R.id.offlineIV);
        }

        void bind(User user, Message message) {
            if (user.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
                defaultProfileTV.setVisibility(View.VISIBLE);
                defaultProfileTV.setText(user.getUserName().substring(0, 1));
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

            profileIV.setOnClickListener(v -> showProfileCard(user));
            defaultProfileTV.setOnClickListener(v -> showProfileCard(user));

            usernameTV.setText(user.getUserName());
            previewMessageTV.setText(message.getMessage());

            //Date
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date currentDay =  new Date(System.currentTimeMillis());
            Date chatDay = new Date(Long.parseLong(message.getTimestamp()));
            if (formatter.format(currentDay).equals(formatter.format(chatDay))) {
                dateLastMesTV.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                        .format(chatDay)
                );
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                dateLastMesTV.setText(dateFormat.format(Long.valueOf(message.getTimestamp())));
            }//

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra(Constants.INTENT_USER, user);
                context.startActivity(i);
            });

            /* Possible feature for text groups
            itemCallLY.setOnLongClickListener(v -> {
                if (selectedUserIV.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(user);
                    selectedUserIV.setVisibility(View.VISIBLE);
                    callIV.setVisibility(View.GONE);
                    videocallIV.setVisibility(View.GONE);
                    usersListener.onMultipleUsersAction(true);
                }
                return true;
            });

            itemCallLY.setOnClickListener(v -> {
                if (selectedUserIV.getVisibility() == View.VISIBLE) {
                    selectedUsers.remove(user);
                    selectedUserIV.setVisibility(View.GONE);
                    callIV.setVisibility(View.VISIBLE);
                    videocallIV.setVisibility(View.VISIBLE);

                    if (selectedUsers.size() == 0)
                        usersListener.onMultipleUsersAction(false);
                } else {
                    if (selectedUsers.size() > 0) {
                        selectedUsers.remove(user);
                        selectedUserIV.setVisibility(View.VISIBLE);
                        callIV.setVisibility(View.GONE);
                        videocallIV.setVisibility(View.GONE);
                    }
                }
            });
             */
        }
    }

     class MessageSentHolder extends RecyclerView.ViewHolder {
        public TextView defaultProfileTV, usernameTV, dateLastMesTV, previewMessageTV;
        public ImageView profileIV, onlineIV, offlineIV, seenIV, notSeenIV;

        MessageSentHolder(View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            dateLastMesTV = itemView.findViewById(R.id.dateLastMesTV);
            previewMessageTV = itemView.findViewById(R.id.previewMessageTV);
            profileIV = itemView.findViewById(R.id.profileIV);
            onlineIV = itemView.findViewById(R.id.onlineIV);
            offlineIV = itemView.findViewById(R.id.offlineIV);
            seenIV = itemView.findViewById(R.id.seenIV);
            notSeenIV = itemView.findViewById(R.id.notSeenIV);
        }

        void bind(User user, Message message) {
            if (user.getLastSeen().equals(Constants.KEY_LAST_SEEN_ONLINE)) {
                onlineIV.setVisibility(View.VISIBLE);
                offlineIV.setVisibility(View.GONE);
            } else {
                onlineIV.setVisibility(View.GONE);
                offlineIV.setVisibility(View.VISIBLE);
            }

            if (message.isSeen()) {
                seenIV.setVisibility(View.VISIBLE);
                notSeenIV.setVisibility(View.GONE);
            } else {
                seenIV.setVisibility(View.GONE);
                notSeenIV.setVisibility(View.VISIBLE);
            }

            if (user.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
                defaultProfileTV.setVisibility(View.VISIBLE);
                defaultProfileTV.setText(user.getUserName().substring(0, 1));
            } else {
                profileIV.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(user.getImageURL())
                        .circleCrop()
                        .into(profileIV);
            }

            profileIV.setOnClickListener(v -> showProfileCard(user));
            defaultProfileTV.setOnClickListener(v -> showProfileCard(user));

            usernameTV.setText(user.getUserName());
            previewMessageTV.setText(message.getMessage());

            //Date
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date currentDay =  new Date(System.currentTimeMillis());
            Date chatDay = new Date(Long.parseLong(message.getTimestamp()));
            if (formatter.format(currentDay).equals(formatter.format(chatDay))) {
                dateLastMesTV.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                        .format(chatDay)
                );
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                dateLastMesTV.setText(dateFormat.format(Long.valueOf(message.getTimestamp())));
            }//

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra(Constants.INTENT_USER, user);
                context.startActivity(i);
            });
        }
    }
}