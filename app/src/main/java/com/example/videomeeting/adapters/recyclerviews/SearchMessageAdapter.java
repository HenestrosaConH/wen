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
import java.util.List;
import java.util.Locale;

import static com.example.videomeeting.utils.Constants.FIREBASE_USER;

public class SearchMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final CallsListener callsListener = new CallsListener();
    private final Context context;
    private final List<User> searchedMessagesUsers;
    private final List<Message> searchedMessages;
    private Dialog profileDG;

    //Result codes
    public static final int ITEM_MSG_SENT = 0;
    public static final int ITEM_MSG_RECEIVED = 1;

    public SearchMessageAdapter(Context context, List<Message> searchedMessages, List<User> searchedMessagesUsers) {
        this.context = context;
        this.searchedMessages = searchedMessages;
        this.searchedMessagesUsers = searchedMessagesUsers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_MSG_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recent_chat_sent, parent, false);
                return new SearchMessageAdapter.MessageSentHolder(view);
            case ITEM_MSG_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recent_chat_received, parent, false);
                return new SearchMessageAdapter.MessageReceivedHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        profileDG = new Dialog(context, R.style.ThemeDialog);
        switch (holder.getItemViewType()) {
            case ITEM_MSG_SENT:
                ((SearchMessageAdapter.MessageSentHolder) holder).bind(
                        searchedMessages.get(position),
                        searchedMessagesUsers.get(position)
                );
                break;
            case ITEM_MSG_RECEIVED:
                ((SearchMessageAdapter.MessageReceivedHolder) holder).bind(
                        searchedMessages.get(position),
                        searchedMessagesUsers.get(position)
                );
                break;
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
        if (user.getAbout() != null) {
            aboutTV.setText(user.getAbout());
        } else {
            aboutTV.setVisibility(View.GONE);
        }

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
        return searchedMessagesUsers.size();
    }//searchedMessages.size();

    @Override
    public int getItemViewType(int position) {
        if (searchedMessages.get(position).getSenderID().equals(FIREBASE_USER.getUid())) {
            return ITEM_MSG_SENT;
        } else {
            return ITEM_MSG_RECEIVED;
        }
    }

    class MessageReceivedHolder extends RecyclerView.ViewHolder {
        public TextView defaultProfileTV, usernameTV, dateLastMesTV, previewMessageTV;
        public ImageView profileIV;
        public CardView statusCV;

        public MessageReceivedHolder(@NonNull View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            dateLastMesTV = itemView.findViewById(R.id.dateLastMesTV);
            previewMessageTV = itemView.findViewById(R.id.previewMessageTV);
            profileIV = itemView.findViewById(R.id.profileIV);
            statusCV = itemView.findViewById(R.id.statusCV);
        }

        void bind(Message message, User user) {
            statusCV.setVisibility(View.GONE);

            usernameTV.setText(user.getUserName());
            previewMessageTV.setText(message.getMessage());

            formatDate(dateLastMesTV, message.getTimestamp());
            showProfilePic(defaultProfileTV, profileIV, user);

            itemView.setOnClickListener(v -> openChat(user));
        }
    }

    class MessageSentHolder extends RecyclerView.ViewHolder {
        public TextView defaultProfileTV, usernameTV, dateLastMesTV, previewMessageTV;
        public ImageView profileIV, seenIV, notSeenIV;
        public CardView statusCV;

        MessageSentHolder(View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            dateLastMesTV = itemView.findViewById(R.id.dateLastMesTV);
            previewMessageTV = itemView.findViewById(R.id.previewMessageTV);
            profileIV = itemView.findViewById(R.id.profileIV);
            seenIV = itemView.findViewById(R.id.seenIV);
            notSeenIV = itemView.findViewById(R.id.notSeenIV);
            statusCV = itemView.findViewById(R.id.statusCV);
        }

        void bind(Message message, User user) {
            statusCV.setVisibility(View.GONE);

            if (message.getSeen()) {
                seenIV.setVisibility(View.VISIBLE);
                notSeenIV.setVisibility(View.GONE);
            } else {
                seenIV.setVisibility(View.GONE);
                notSeenIV.setVisibility(View.VISIBLE);
            }

            usernameTV.setText(user.getUserName());
            previewMessageTV.setText(message.getMessage());
            showProfilePic(defaultProfileTV, profileIV, user);
            formatDate(dateLastMesTV, message.getTimestamp());
            itemView.setOnClickListener(v -> openChat(user));
        }
    }

    private void formatDate(TextView dateLastMesTV, long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDay =  new Date(System.currentTimeMillis());
        Date chatDay = new Date(timestamp);
        if (formatter.format(currentDay).equals(formatter.format(chatDay))) {
            dateLastMesTV.setText(
                    DateFormat.getTimeInstance(
                            DateFormat.SHORT,
                            Locale.getDefault()
                    ).format(chatDay)
            );
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            dateLastMesTV.setText(dateFormat.format(timestamp));
        }
    }

    private void showProfilePic(TextView defaultProfileTV, ImageView profileIV, User user) {
        if (user.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
            defaultProfileTV.setOnClickListener(v -> showProfileCard(user));
            defaultProfileTV.setVisibility(View.VISIBLE);
            defaultProfileTV.setText(user.getUserName().substring(0, 1));
        } else {
            profileIV.setOnClickListener(v -> showProfileCard(user));
            profileIV.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getImageURL())
                    .circleCrop()
                    .into(profileIV);
        }
    }

    private void openChat(User user) {
        Intent i = new Intent(context, ChatActivity.class);
        i.putExtra(Constants.INTENT_USER, user);
        context.startActivity(i);
    }
}
