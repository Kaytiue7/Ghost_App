package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MesajSayfa extends Fragment {



    private EditText textBox;
    private Button button;
    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firebaseFirestore;

    private AdapterMessageBox adapter;
    private List<PostMessage> messageList;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment'in görünümünü şişiriyoruz
        View view = inflater.inflate(R.layout.fragment_mesaj_sayfa, container, false);

        sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        textBox = view.findViewById(R.id.textBox);
        button = view.findViewById(R.id.button);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Swipe-to-refresh yapıldığında veriyi yeniden çek
            getData();
        });

        messageList = new ArrayList<>();
        adapter = new AdapterMessageBox(getContext(), messageList);
        recyclerView.setAdapter(adapter);

        firebaseFirestore = FirebaseFirestore.getInstance();

        // RecyclerView için adapter ve liste ayarla (placeholder veri ile)


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Butona basıldığında yeni fragmenti aç
                Intent intent =  new Intent(getContext(),AddMessageGroupPage.class);
                startActivity(intent);
            }
        });

        getData();
        return view;
    }


    private void getData() {

        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        firebaseFirestore.collection("Messages")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {

                        // Yeni veriler eklenmeden önce listeyi temizle
                        messageList.clear();

                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();
                            if (data != null) {
                                String groupId = snapshot.getId();
                                String groupType = (String) data.get("groupType");
                                String groupTitle = (String) data.get("groupTitle");

                                if (groupType.equals("DirectMessage")) {
                                    firebaseFirestore.collection("Messages")
                                            .document(groupId)
                                            .collection("Users")
                                            .addSnapshotListener((value2, error2) -> {
                                                if (value2 != null) {
                                                    for (DocumentSnapshot snapshot2 : value2.getDocuments()) {
                                                        Map<String, Object> data2 = snapshot2.getData();
                                                        if (data2 != null) {
                                                            String username = (String) data2.get("username");

                                                            if (storedUsername.equals(username)) {
                                                                String pp = (String) data2.get("profilePhoto");

                                                                PostMessage post = new PostMessage(groupId, groupTitle, pp);
                                                                messageList.add(post);
                                                            }
                                                        }
                                                    }
                                                    adapter.notifyDataSetChanged(); // Adapter'ı güncelle
                                                }
                                            });
                                }
                                if (groupType.equals("Group")) {
                                    firebaseFirestore.collection("Messages")
                                            .document(groupId)
                                            .collection("Users")
                                            .addSnapshotListener((value2, error2) -> {
                                                if (value2 != null) {
                                                    for (DocumentSnapshot snapshot2 : value2.getDocuments()) {
                                                        Map<String, Object> data2 = snapshot2.getData();
                                                        if (data2 != null) {
                                                            String username = (String) data2.get("username");

                                                            if (storedUsername.equals(username)) {
                                                                String pp = (String) data2.get("profilePhoto");

                                                                PostMessage post = new PostMessage(groupId, groupTitle, pp);
                                                                messageList.add(post);
                                                            }
                                                        }
                                                    }
                                                    adapter.notifyDataSetChanged(); // Adapter'ı güncelle
                                                }
                                            });
                                }
                            }
                        }
                        adapter.notifyDataSetChanged(); // Adapter'ı güncelle
                    }
                });
    }







}