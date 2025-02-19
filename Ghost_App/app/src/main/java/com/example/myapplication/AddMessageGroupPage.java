package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddMessageGroupPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterUsersCheckbox adapter;
    private List<PostUserCheckbox> userList;
    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message_group_page);

        firebaseFirestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        recyclerView = findViewById(R.id.recyclerView);
        userList = new ArrayList<>();

        // Buton referansını al
        Button btnAddGroup = findViewById(R.id.btnAddGroup);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter'i oluştur
        adapter = new AdapterUsersCheckbox(this, userList, btnAddGroup);
        recyclerView.setAdapter(adapter); // Adapter'i RecyclerView'a ekle

        getData();
    }

    private void getData() {
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        firebaseFirestore.collection("Users")
                .whereNotEqualTo("username", storedUsername)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        userList.clear(); // Yeni veriler eklenmeden önce listeyi temizle
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();
                            if (data != null) {
                                String id = snapshot.getId();
                                String username = (String) data.get("username");
                                String pp = (String) data.get("profilePhoto");
                                PostUserCheckbox post = new PostUserCheckbox(id, username, pp);
                                userList.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged(); // Adapter'ı güncelle
                    }
                });
    }
}
