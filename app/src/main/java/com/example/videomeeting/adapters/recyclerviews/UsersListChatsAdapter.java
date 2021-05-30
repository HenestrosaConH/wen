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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.activities.ChatActivity;
import com.example.videomeeting.listeners.CallsListener;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.Constants;

import java.util.List;

public class UsersListChatsAdapter extends RecyclerView.Adapter<UsersListChatsAdapter.UsersChatViewHolder> {

    private final Context context;
    public final List<User> userList;
    private final CallsListener callsListener = new CallsListener();
    private Dialog profileDG;
    private User user;

    public UsersListChatsAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UsersChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersChatViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_user_list_chat,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UsersChatViewHolder holder, int position) {
        user = userList.get(position);
        holder.setChatData();
        profileDG = new Dialog(context, R.style.ThemeDialog);
    }

    private void showProfileCard() {
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
    public int getItemCount() { return userList.size(); }

    class UsersChatViewHolder extends RecyclerView.ViewHolder {
        public TextView defaultProfileTV, usernameTV, dateLastMesTV, aboutTV;
        public ImageView profileIV;

        public UsersChatViewHolder(@NonNull View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            dateLastMesTV = itemView.findViewById(R.id.dateLastMesTV);
            aboutTV = itemView.findViewById(R.id.aboutTV);
            profileIV = itemView.findViewById(R.id.profileIV);
        }

        void setChatData() {
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

            profileIV.setOnClickListener(v -> showProfileCard());
            defaultProfileTV.setOnClickListener(v -> showProfileCard());

            usernameTV.setText(user.getUserName());
            aboutTV.setText(user.getAbout());

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra(Constants.INTENT_USER, user);
                context.startActivity(i);
            });

            /* Possible feature for group texts
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
}
