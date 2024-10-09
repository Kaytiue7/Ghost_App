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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.UserViewHolder> {

    private List<PostMessage> messageList;
    private Context context;
    private Button btnAddGroup; // Button reference
    private Set<Integer> selectedUsers; // Track selected user positions
    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    public AdapterMessage(Context context, List<PostMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.firebaseFirestore = FirebaseFirestore.getInstance(); // Get Firestore instance

    }

    // Set up button click listener to save selected users


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item_checkbox, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PostMessage message = messageList.get(position);
        /*/
        holder.username.setText(user.username);
        Picasso.get().load(user.profilePictureUrl).into(holder.profilePicture);

        holder.checkBox.setOnCheckedChangeListener(null); // Clear any previous listeners

        // Set the CheckBox state based on the user's selection
        holder.checkBox.setChecked(selectedUsers.contains(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsers.add(position);
            } else {
                selectedUsers.remove(position);
            }

            // Show the button if more than 2 users are selected, otherwise hide it
            btnAddGroup.setVisibility(selectedUsers.size() > 0 ? View.VISIBLE : View.GONE);
        });
        /*/

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView username;
        CheckBox checkBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            username = itemView.findViewById(R.id.username);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
