package com.example.videomeeting.adapters.recyclerviews;

import android.content.Context;
import android.content.Intent;
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
import com.example.videomeeting.models.Message;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<User> searchedMessagesUsers;
    private final List<Message> searchedMessages;

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

    @Override
    public int getItemCount() {
        return searchedMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (searchedMessages.get(position).getSenderID().equals(Constants.CURRENT_USER.getId())) {
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

            if (message.isSeen()) {
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

    private void formatDate(TextView dateLastMesTV, String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDay =  new Date(System.currentTimeMillis());
        Date chatDay = new Date(Long.parseLong(date));
        if (formatter.format(currentDay).equals(formatter.format(chatDay))) {
            dateLastMesTV.setText(
                    DateFormat.getTimeInstance(
                            DateFormat.SHORT,
                            Locale.getDefault()
                    ).format(chatDay)
            );
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            dateLastMesTV.setText(dateFormat.format(Long.valueOf(date)));
        }
    }

    private void showProfilePic(TextView defaultProfileTV, ImageView profileIV, User user) {
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
    }

    private void openChat(User user) {
        Intent i = new Intent(context, ChatActivity.class);
        i.putExtra(Constants.INTENT_USER, user);
        context.startActivity(i);
    }
}
