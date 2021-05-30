package com.example.videomeeting.adapters.recyclerviews;

import android.content.Context;
import android.content.Intent;
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
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.Constants;

import java.util.Map;

public class SearchUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Map<String, User> searchedUsers;

    public SearchUserAdapter(Context context, Map<String, User> searchedUsers) {
        this.context = context;
        this.searchedUsers = searchedUsers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_searched, parent, false);
        return new SearchUserAdapter.UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String key = (String) searchedUsers.keySet().toArray()[position];
        ((SearchUserAdapter.UserHolder) holder).bind(searchedUsers.get(key));
    }

    @Override
    public int getItemCount() {
        return searchedUsers.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {
        public ImageView profileIV;
        public TextView defaultProfileTV, usernameTV, aboutTV;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            defaultProfileTV = itemView.findViewById(R.id.defaultProfileTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            aboutTV = itemView.findViewById(R.id.aboutTV);
            profileIV = itemView.findViewById(R.id.profileIV);
        }

        void bind(User user) {
            usernameTV.setText(user.getUserName());
            aboutTV.setText(user.getAbout());
            showProfilePic(defaultProfileTV, profileIV, user);
            itemView.setOnClickListener(v -> openChat(user));
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
