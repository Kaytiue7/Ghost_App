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

public class AdapterUsersCheckbox extends RecyclerView.Adapter<AdapterUsersCheckbox.UserViewHolder> {

    private List<PostUserCheckbox> userList;
    private Context context;
    private Button btnAddGroup; // Button reference
    private Set<Integer> selectedUsers; // Track selected user positions
    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    public String groupTitle;
    private Set<String> selectedUsernames; // Track selected usernames

    public AdapterUsersCheckbox(Context context, List<PostUserCheckbox> userList, Button btnAddGroup) {
        this.context = context;
        this.userList = userList;
        this.btnAddGroup = btnAddGroup;
        this.selectedUsers = new HashSet<>(); // Initialize the set for user positions
        this.selectedUsernames = new HashSet<>(); // Initialize the set for usernames
        this.firebaseFirestore = FirebaseFirestore.getInstance(); // Get Firestore instance
        setupButtonListener(); // Set up the button listener
    }

    // Set up button click listener to save selected users
    private void setupButtonListener() {
        btnAddGroup.setOnClickListener(v -> {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

            List<String> selectedUsernamesList = new ArrayList<>();

            // Seçilen kullanıcı adlarını listeye ekle
            for (Integer position : selectedUsers) {
                PostUserCheckbox user = userList.get(position);
                selectedUsernamesList.add(user.username);
            }

            // Eğer storedUsername null değilse, selectedUsernamesList'e ekle
            if (storedUsername != null) {
                selectedUsernamesList.add(storedUsername);
            }

            String groupId = UUID.randomUUID().toString(); // Benzersiz bir grup kimliği oluştur
            for (String username : selectedUsernamesList) {
                String randomDocId = UUID.randomUUID().toString();
                Date currentDate = new Date();
                Timestamp currentTimestamp = new Timestamp(currentDate);

                String groupType = selectedUsernamesList.size() > 2 ? "Group" : "DirectMessage";

                
                StringBuilder groupTitleBuilder = new StringBuilder();

                for (int i = 0; i < selectedUsernamesList.size(); i++) {
                    groupTitleBuilder.append(selectedUsernamesList.get(i));
                    if (i != selectedUsernamesList.size() - 1) {
                        groupTitleBuilder.append(", "); // Son kullanıcı adı hariç virgülle ayır
                    }
                }

                if (selectedUsernamesList.size() > 1) {
                    groupTitleBuilder.append(" Grubu"); // Birden fazla kullanıcı varsa 'Grubu' ekle
                }

                groupTitle = groupTitleBuilder.toString();

                firebaseFirestore.collection("Messages")
                        .document(groupId)
                        .set(new HashMap<String, Object>() {{
                            put("groupType", groupType);
                            put("date", currentTimestamp);
                            put("groupTitle", groupTitle);
                        }})
                        .addOnSuccessListener(aVoid -> {
                            // Tüm kullanıcılar eklendiğinde groupType'ı güncelle

                            Toast.makeText(context, "Grup başarıyla eklendi: " + groupType, Toast.LENGTH_SHORT).show();
                            firebaseFirestore.collection("Messages")
                                    .document(groupId)
                                    .collection("Users")
                                    .document(randomDocId)
                                    .set(new HashMap<String, Object>() {{
                                        put("username", username);
                                        put("date", currentTimestamp);

                                    }}, SetOptions.merge()) // Mevcut alanlarla birleştir
                                    .addOnSuccessListener(Void -> {
                                        Toast.makeText(context, "Kullanıcı başarıyla eklendi: " + username, Toast.LENGTH_SHORT).show();
                                        openFragment();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "GroupType eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });


                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Kullanıcı eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            // Seçilen kullanıcıları temizle ve butonu gizle
            selectedUsers.clear();
            btnAddGroup.setVisibility(View.GONE);
        });
    }


    private void openFragment() {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Burada açmak istediğiniz Fragment'ı belirtin
        MesajSayfa MesajSayfa = new MesajSayfa(); // YourFragment'ı uygun olanla değiştirin
        fragmentTransaction.replace(R.id.frame_layout, MesajSayfa); // fragment_container, Fragment'ların ekleneceği container'ın ID'sidir
        fragmentTransaction.addToBackStack(null); // Geri tuşuyla çıkabilmek için
        fragmentTransaction.commit();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item_checkbox, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PostUserCheckbox user = userList.get(position);
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
    }

    @Override
    public int getItemCount() {
        return userList.size();
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
