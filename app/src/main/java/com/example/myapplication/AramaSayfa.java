package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AramaSayfa extends Fragment {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private Button button;
    private List<Post> postList;
private TextView textView;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arama_sayfa, container, false);

        textView = view.findViewById(R.id.textView);

        button = view.findViewById(R.id.button);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData((String) textView.getText());
            }
        });


        postList = new ArrayList<>();
        adapter = new Adapter(getContext(), postList);
        recyclerView.setAdapter(adapter);

        firebaseFirestore = FirebaseFirestore.getInstance();



        return view;
    }
    public void buttonClick(){

    }

    private void getData(String query) {
        firebaseFirestore.collection("Post")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereGreaterThanOrEqualTo("metin", query) // Query ile uyumlu olan metinleri getir
                .whereLessThanOrEqualTo("metin", query + "\uf8ff") // Firebase'de arama için standart yaklaşım
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        postList.clear(); // Yeni verileri eklemeden önce listeyi temizle
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();
                            if (data != null) {
                                String postType = (String) data.get("postType");

                                // Eğer postType "Comment" ise bu postu atla
                                if (!postType.equals("Comment")) {
                                    String id = snapshot.getId();
                                    String replyId = (String) data.get("repyledPost");
                                    String metin = (String) data.get("metin");
                                    String image = (String) data.get("image");
                                    String username = (String) data.get("username");
                                    Timestamp date = (Timestamp) data.get("date");
                                    String date2 = getTimeAgo(date);

                                    Post post = new Post(id, replyId, postType, metin, image, username, date2);
                                    postList.add(post);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }



    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < DateUtils.HOUR_IN_MILLIS) {
            return (diff / DateUtils.MINUTE_IN_MILLIS) + " dakika önce";
        } else if (diff < DateUtils.DAY_IN_MILLIS) {
            return (diff / DateUtils.HOUR_IN_MILLIS) + " saat önce";
        } else if (diff < DateUtils.WEEK_IN_MILLIS) {
            return (diff / DateUtils.DAY_IN_MILLIS) + " gün önce";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(timestamp.toDate());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}