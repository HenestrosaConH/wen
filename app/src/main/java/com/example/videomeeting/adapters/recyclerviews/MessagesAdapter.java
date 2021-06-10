package com.example.videomeeting.adapters.recyclerviews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videomeeting.R;
import com.example.videomeeting.models.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.videomeeting.utils.Constants.FIREBASE_USER;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;
    public boolean isDateNeeded = false;
    //private final String imageURL;

    //Result codes
    public static final int ITEM_MSG_SENT = 0;
    public static final int ITEM_MSG_RECEIVED = 1;
    public static final int ITEM_MSG_RECEIVED_GROUP = 2;

    public MessagesAdapter(List<Message> messageList) {
        this.messageList = messageList;
        //this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_MSG_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_msg_sent, parent, false);
                return new MessageSentHolder(view);
            case ITEM_MSG_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_msg_received, parent, false);
                return new MessageReceivedHolder(view);
            /*case ITEM_MSG_RECEIVED_GROUP:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_msg_received_group, parent, false);
                return new MessageReceivedGroupHolder(view);*/
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        switch (holder.getItemViewType()) {
            case ITEM_MSG_SENT:
                ((MessageSentHolder) holder).bind(messageList.get(position), isDateNeeded);
                break;
            case ITEM_MSG_RECEIVED:
                ((MessageReceivedHolder) holder).bind(messageList.get(position), isDateNeeded);
                break;
            /*case ITEM_MSG_RECEIVED_GROUP:
                ((MessageReceivedGroupHolder) holder).bind(chat);
                break;*/
        }
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDay =  new Date(message.getTimestamp());
        Date previousDay;

        isDateNeeded = false;
        if (position - 1 >= 0) {
            previousDay = new Date(messageList.get(position - 1).getTimestamp());
            if (!formatter.format(currentDay).equals(formatter.format(previousDay))) { //if currentDate comes a day(s) after previousDate
                if (message.getSenderID().equals(FIREBASE_USER.getUid())) {
                    isDateNeeded = true;
                    return ITEM_MSG_SENT;
                } else { //if currentDate comes after previousDate
                    isDateNeeded = true;
                    return ITEM_MSG_RECEIVED;
                }
            } else {
                if (message.getSenderID().equals(FIREBASE_USER.getUid())) {
                    isDateNeeded = false;
                    return ITEM_MSG_SENT;
                }
                else {
                    isDateNeeded = false;
                    return ITEM_MSG_RECEIVED;
                }
            }
        } else if (position  == 0) {
            if (message.getSenderID().equals(FIREBASE_USER.getUid())) {
                isDateNeeded = true;
                return ITEM_MSG_SENT;
            } else { //if currentDate comes after previousDate
                isDateNeeded = true;
                return ITEM_MSG_RECEIVED;
            }
        }

        if (message.getSenderID().equals(FIREBASE_USER.getUid())) {
            return ITEM_MSG_SENT;
        } else {
            return ITEM_MSG_RECEIVED;
        }
        /*else if (!chat.getSenderID().equals(Constants.CURRENT_USER.getId())
            && some other condition)
            return ITEM_MSG_RECEIVED_GROUP;*/
    }

    ////////////////////////////////////////////////////////////////
    class MessageSentHolder extends RecyclerView.ViewHolder {
        TextView messageTV;
        TextView timeTV, dateTV;
        ImageView seenIV, notSeenIV;
        CardView dateCV;

        MessageSentHolder(View itemView) {
            super(itemView);

            messageTV = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            seenIV = itemView.findViewById(R.id.seenIV);
            notSeenIV = itemView.findViewById(R.id.notSeenIV);
            dateTV = itemView.findViewById(R.id.dateTV);
            dateCV = itemView.findViewById(R.id.dateCV);
        }

        void bind(Message message, boolean isDateNeeded) {
            messageTV.setText(message.getMessage());
            if (message.getSeen()) {
                seenIV.setVisibility(View.VISIBLE);
            } else {
                notSeenIV.setVisibility(View.VISIBLE);
            }

            timeTV.setText(
                    DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                            .format(message.getTimestamp())
            );

            if (isDateNeeded) {
                formatDate(dateCV, dateTV, message);
            }
        }
    }

    class MessageReceivedHolder extends RecyclerView.ViewHolder {
        TextView messageTV;
        TextView timeTV, dateTV;
        CardView dateCV;

        MessageReceivedHolder(View itemView) {
            super(itemView);

            messageTV = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            dateTV = itemView.findViewById(R.id.dateTV);
            dateCV = itemView.findViewById(R.id.dateCV);
        }

        void bind(Message message, boolean isDateNeeded) {
            messageTV.setText(message.getMessage());
            timeTV.setText(
                    DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                            .format(message.getTimestamp())
            );
            if (isDateNeeded) {
                formatDate(dateCV, dateTV, message);
            }
        }
    }

    private void formatDate(CardView dateCV, TextView dateTV, Message message) {
        dateCV.setVisibility(View.VISIBLE);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        dateTV.setText(formatter.format(message.getTimestamp()));
    }

    /*
    class MessageReceivedGroupHolder extends RecyclerView.ViewHolder {
        EmojiconTextView messageTV;
        TextView timeTV, usernameTV, defaultProfileIV;
        ImageView profileIV;

        MessageReceivedGroupHolder(View itemView) {
            super(itemView);

            messageTV = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            defaultProfileIV = itemView.findViewById(R.id.defaultProfileIV);
            profileIV = itemView.findViewById(R.id.profileIV);
        }

        void bind(Chat chat) {
            if (imageURL.equals("default")) { //PROBABLEMENTE TENGA QUE METER UNA LISTA DE USUARIOS
                defaultProfileIV.setVisibility(View.VISIBLE);
                //defaultProfileIV.setText(user.getUserName().substring(0,1);
            } else {
                Glide.with(context)
                        .load(imageURL)
                        .circleCrop()
                        .into(profileIV);
            }

            //usernameTV.setText(user.getUserName());
            messageTV.setText(chat.getMessage());

            timeTV.setText(
                    DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                            .format(chat.getDate())
            );
        }
    }
     */
}