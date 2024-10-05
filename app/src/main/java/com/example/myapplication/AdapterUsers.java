package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.UserViewHolder> {

    private List<PostUser> userList;

    public AdapterUsers(List<PostUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PostUser user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        // Profil fotoğrafını yükle (örneğin, Picasso veya Glide kullanarak)
        holder.profileImageView.setImageDrawable(null);
        Picasso.get().load(user.getProfilePhoto()).into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageView profileImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
