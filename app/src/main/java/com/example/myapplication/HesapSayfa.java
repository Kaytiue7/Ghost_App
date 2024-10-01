package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HesapSayfa extends Fragment {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Post> postList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    public ImageView imageView;
    public TextView textView,followerTextView, followedTextView;
    public Button btnPostlarim, btnBegendiklerim, btnYanitlarim, btnTakipEt;
    public String anlik_sayfa="AnaSayfa";
    public String username;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hesap_sayfa, container, false);
        sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseFirestore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        adapter = new Adapter(getContext(), postList);
        recyclerView.setAdapter(adapter);
        followerTextView = view.findViewById(R.id.followerTextView);
        followedTextView = view.findViewById(R.id.followedTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);



        if ( anlik_sayfa.equals("AnaSayfa")){
            swipeRefreshLayout.setOnRefreshListener(this::getData);

        }
        if ( anlik_sayfa.equals("Begenmelerim")){
            swipeRefreshLayout.setOnRefreshListener(this::begenmelerim);

        }
        if ( anlik_sayfa.equals("Yanıtlarım")){
            swipeRefreshLayout.setOnRefreshListener(this::AllPost);

        }

        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        imageView = view.findViewById(R.id.profilePhoto);
        textView = view.findViewById(R.id.username);
        btnPostlarim = view.findViewById(R.id.btnPostlarim);
        btnBegendiklerim = view.findViewById(R.id.btnBegendiklerim);
        btnYanitlarim = view.findViewById(R.id.btnYanitlarim);
        btnTakipEt = view.findViewById(R.id.btnTakipEt);
        btnTakipEt.setOnClickListener(this::FollowOrUnfollow);



        btnBegendiklerim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anlik_sayfa="Begenmelerim";
                begenmelerim();
            }
        });

        btnPostlarim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anlik_sayfa="AnaSayfa";
                getData();
            }
        });
        btnYanitlarim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anlik_sayfa="Yanıtlarım";
                AllPost();
            }
        });

        showFollowerCount();


        showPerson(storedUsername);
        getData();
        return view;
    }

    // Takip etme işlemleri
        public void showFollowerCount(){
            String myUsername = sharedPreferences.getString(KEY_USERNAME, null);
            String profilPageUsername = getArguments() != null ? getArguments().getString("username") : null;
            if (myUsername.equals(profilPageUsername)) {
                btnTakipEt.setVisibility(View.GONE);


                firebaseFirestore.collection("UserFollow")
                        .whereEqualTo("FollowedTo", myUsername)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Takipçi sayısını al
                                int followerCount = task.getResult().size();

                                // followerTextView'e takipçi sayısını yazdır

                                followerTextView.setText("Takipçi Sayısı: " + followerCount);
                            } else {
                                Log.w("Firebase", "Veri çekme hatası", task.getException());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firebase", "Sorgu hatası", e);
                        });

                firebaseFirestore.collection("UserFollow")
                        .whereEqualTo("FollowedFrom", myUsername)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Takipçi sayısını al
                                int followerCount = task.getResult().size();

                                // followerTextView'e takipçi sayısını yazdır

                                followedTextView.setText("Takip Edilen Sayısı: " + followerCount);
                            } else {
                                Log.w("Firebase", "Veri çekme hatası", task.getException());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firebase", "Sorgu hatası", e);
                        });
            } else {

                    firebaseFirestore.collection("UserFollow")
                            .whereEqualTo("FollowedTo", profilPageUsername)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Takipçi sayısını al
                                    int followerCount = task.getResult().size();

                                    // followerTextView'e takipçi sayısını yazdır

                                    followerTextView.setText("Takipçi Sayısı: " + followerCount);
                                } else {
                                    Log.w("Firebase", "Veri çekme hatası", task.getException());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firebase", "Sorgu hatası", e);
                            });


                firebaseFirestore.collection("UserFollow")
                        .whereEqualTo("FollowedFrom", profilPageUsername)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Takipçi sayısını al
                                int followerCount = task.getResult().size();

                                // followerTextView'e takipçi sayısını yazdır

                                followedTextView.setText("Takip Edilen Sayısı: " + followerCount);
                            } else {
                                Log.w("Firebase", "Veri çekme hatası", task.getException());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firebase", "Sorgu hatası", e);
                        });


            }

            firebaseFirestore.collection("UserFollow")
                    .whereEqualTo("FollowedFrom", myUsername)
                    .whereEqualTo("FollowedTo", profilPageUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Eğer takip ediyorsa
                            btnTakipEt.setText("Takibi Bırak");
                        } else {
                            // Takip etmiyorsa
                            btnTakipEt.setText("Takip Et");
                        }
                    });
        }
    private void FollowOrUnfollow(View view) {
        String myUsername = sharedPreferences.getString(KEY_USERNAME, null);
        String profilPageUsername = getArguments() != null ? getArguments().getString("username") : null;

        Log.d("FollowOrUnfollow", "myUsername: " + myUsername);
        Log.d("FollowOrUnfollow", "profilPageUsername: " + profilPageUsername);

        if (myUsername != null && profilPageUsername != null) {


            // UserFollow koleksiyonunda sorgu yap
            firebaseFirestore.collection("UserFollow")
                    .whereEqualTo("FollowedFrom", myUsername)
                    .whereEqualTo("FollowedTo", profilPageUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Eğer böyle bir veri varsa, veriyi sil
                            for (DocumentSnapshot document : task.getResult()) {
                                firebaseFirestore.collection("UserFollow").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Silme işlemi başarılı
                                            Log.d("Firebase", "Takipten çıkıldı.");
                                            showFollowerCount();

                                        })
                                        .addOnFailureListener(e -> {
                                            // Silme işlemi başarısız
                                            Log.w("Firebase", "Takipten çıkma başarısız.", e);
                                        });
                            }
                        } else {
                            // Eğer veri yoksa
                            Map<String, Object> followData = new HashMap<>();
                            followData.put("FollowedFrom", myUsername);
                            followData.put("FollowedTo", profilPageUsername);
                            followData.put("date", new Timestamp(new Date())); // Tarihi ekle

                            firebaseFirestore.collection("UserFollow").add(followData)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("Firebase", "Takip eklendi: " + documentReference.getId());
                                        showFollowerCount();

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firebase", "Takip eklenirken hata oluştu", e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Sorgu başarısız
                        Log.w("Firebase", "Veri sorgulama hatası", e);
                    });
        } else {
            Log.e("FollowOrUnfollow", "Kullanıcı adı veya profil adı null");
        }
        showFollowerCount();



    }

    private void showPerson(String storedUsername) {
        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("Users").whereEqualTo("username", storedUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        if (snapshot.exists()) {
                            Map<String, Object> data = snapshot.getData();
                            String username = (String) data.get("username");
                            String profilePicture = (String) data.get("profilePhoto");


                            textView.setText("@" + username);
                            if (profilePicture != null) {
                                Picasso.get().load(profilePicture).into(imageView);
                            } else {
                                imageView.setImageResource(R.drawable.my_account);
                            }

                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Veri yükleme hatası: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }


    private void getData() {
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("Post")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereEqualTo("username", storedUsername)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing
                        return;
                    }

                    if (value != null) {
                        postList.clear(); // Clear the list before adding new data
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
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing
                    }
                });
    }

    private void AllPost() {
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("Post")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereEqualTo("username", storedUsername)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing
                        return;
                    }

                    if (value != null) {
                        postList.clear(); // Clear the list before adding new data
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();
                            if (data != null) {
                                String postType = (String) data.get("postType");

                                // Eğer postType "Comment" ise bu postu atla


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
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing
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
            long hours = diff / DateUtils.HOUR_IN_MILLIS;
            return hours + " saat önce";
        } else if (diff < DateUtils.WEEK_IN_MILLIS) {
            long days = diff / DateUtils.DAY_IN_MILLIS;
            return days + " gün önce";
        } else if (diff < 56 * DateUtils.WEEK_IN_MILLIS) {
            long weeks = diff / DateUtils.WEEK_IN_MILLIS;
            return weeks + " hafta önce";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(timestamp.toDate());
        }
    }

    private void begenmelerim() {
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        postList.clear(); // Önceki verileri temizle

        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("UsersLiked")
                .whereEqualTo("LikedFrom", storedUsername)
                //.orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            String postId = snapshot.getString("PostId"); // Beğenilen gönderinin ID'si

                            // Gönderi detaylarını almak için Post koleksiyonunu sorgula
                            firebaseFirestore.collection("Post").document(postId)
                                    .get()
                                    .addOnSuccessListener(postSnapshot -> {
                                        if (postSnapshot.exists()) {
                                            Map<String, Object> postData = postSnapshot.getData();
                                            if (postData != null) {
                                                String id = snapshot.getId();
                                                String replyId = (String) postData.get("repyledPost");
                                                String postType = (String) postData.get("postType");
                                                String metin = (String) postData.get("metin");
                                                String image = (String) postData.get("image");
                                                String username = (String) postData.get("username");
                                                Timestamp date = (Timestamp) postData.get("date");
                                                String date2 = getTimeAgo(date);

                                                Post post = new Post(id, replyId,postType,  metin, image, username, date2);
                                                postList.add(post);
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Beğenilen gönderileri yükleme hatası: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }




}
