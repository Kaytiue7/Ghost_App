package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Set;

public class AdapterMessageBox extends RecyclerView.Adapter<AdapterMessageBox.UserViewHolder> {

    private List<PostMessage> messageList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    public AdapterMessageBox(Context context, List<PostMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.firebaseFirestore = FirebaseFirestore.getInstance(); // Get Firestore instance

    }

    // Set up button click listener to save selected users


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PostMessage message = messageList.get(position);

        holder.username.setText(message.title);
        Picasso.get().load(R.drawable.ghost).into(holder.profilePicture);


        // Set the CheckBox state based on the user's selection


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView username;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profileImageView);
            username = itemView.findViewById(R.id.usernameTextView);
        }
    }
}
