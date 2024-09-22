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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public TextView textView,takipciSayisiTextView, takipEtmeSayisiTextView;
    public Button btnPostlarim, btnBegendiklerim, btnYanitlarim, btnTakipEt;

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
        takipciSayisiTextView = view.findViewById(R.id.takipciSayisiTextView);
        takipEtmeSayisiTextView = view.findViewById(R.id.takipEtmeSayisiTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getData);

        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        imageView = view.findViewById(R.id.profilePhoto);
        textView = view.findViewById(R.id.username);
        btnPostlarim = view.findViewById(R.id.btnPostlarim);
        btnBegendiklerim = view.findViewById(R.id.btnBegendiklerim);
        btnYanitlarim = view.findViewById(R.id.btnYanitlarim);
        btnTakipEt = view.findViewById(R.id.btnTakipEt);
        btnTakipEt.setOnClickListener(this::TakipEt);

        btnPostlarim.setOnClickListener(v -> getData());
        btnBegendiklerim.setOnClickListener(v -> begenmelerim());
        String currentUserId = sharedPreferences.getString(KEY_USERNAME, null);
        String targetUserId = getArguments() != null ? getArguments().getString("username") : null;
        if (currentUserId.equals(targetUserId)) {
            btnTakipEt.setEnabled(false); // Butonu devre dışı bırak
            btnTakipEt.setVisibility(View.GONE); // İstersen butonu tamamen gizleyebilirsin
        } else {
            checkFollowingStatus(firebaseFirestore, currentUserId, targetUserId, btnTakipEt, isFollowing -> {
                // Takip durumu kontrolü tamamlandığında yapılacak işlemler
                btnTakipEt.setText(isFollowing ? "Takibi Bırak" : "Takip Et");
            });
        }

        showPerson(storedUsername);
        getData();
        return view;
    }

    // Takip etme işlemleri

    private void TakipEt(View view) {
        String currentUserId = sharedPreferences.getString(KEY_USERNAME, null);
        String targetUserId = getArguments() != null ? getArguments().getString("username") : null;

        // Kullanıcının takip edip etmediğini kontrol et
        checkFollowingStatus(firebaseFirestore, currentUserId, targetUserId, btnTakipEt, isFollowing -> {
            // Takip etme veya takipten çıkma işlemini yap
            followUser(firebaseFirestore, currentUserId, targetUserId, btnTakipEt, getContext(), isFollowing);
        });

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
                            Long takipciSayisi = (Long) data.get("Takipçi "+"takipciSayisi");
                            Long takipEtmeSayisi = (Long) data.get("Takip "+"takipEtmeSayisi");

                            textView.setText("@" + username);
                            if (profilePicture != null) {
                                Picasso.get().load(profilePicture).into(imageView);
                            } else {
                                imageView.setImageResource(R.drawable.my_account);
                            }

                            // Takipçi ve takip etme sayılarını göster
                            takipciSayisiTextView.setText(takipciSayisi != null ? takipciSayisi.toString() : "0");
                            takipEtmeSayisiTextView.setText(takipEtmeSayisi != null ? takipEtmeSayisi.toString() : "0");
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
                                String id = snapshot.getId();
                                String metin = (String) data.get("metin");
                                String image = (String) data.get("image");
                                String replyId = (String) data.get("repyledPost");
                                String username = (String) data.get("username");
                                Timestamp date = (Timestamp) data.get("date");
                                String date2 = getTimeAgo(date);

                                Post post = new Post(id, replyId, metin, image, username, date2, image, 0);
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

        firebaseFirestore.collection("usersLiked")
                .whereEqualTo("username", storedUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            String postId = snapshot.getString("postId"); // Beğenilen gönderinin ID'si

                            // Gönderi detaylarını almak için Post koleksiyonunu sorgula
                            firebaseFirestore.collection("Post").document(postId)
                                    .get()
                                    .addOnSuccessListener(postSnapshot -> {
                                        if (postSnapshot.exists()) {
                                            Map<String, Object> postData = postSnapshot.getData();
                                            if (postData != null) {
                                                String id = postSnapshot.getId();
                                                String metin = (String) postData.get("metin");
                                                String image = (String) postData.get("image");
                                                String replyId = (String) postData.get("repyledPost");
                                                String username = (String) postData.get("username");
                                                Timestamp date = (Timestamp) postData.get("date");
                                                String date2 = getTimeAgo(date);

                                                // Post nesnesini oluştur ve listeye ekle
                                                Post post = new Post(id, replyId, metin, image, username, date2, image, 0);
                                                postList.add(post);

                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Beğenilen gönderileri yükleme hatası: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void followUser(FirebaseFirestore firestore, String currentUserId, String targetUserId, Button followButton, Context context, boolean isFollowing) {
        Map<String, Object> followData = new HashMap<>();
        followData.put("follower", currentUserId);

        if (isFollowing) {
            // Takipten çık
            firestore.collection("Following").document(currentUserId)
                    .collection("following").document(targetUserId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        followButton.setText("Takip Et");
                        Toast.makeText(context, "Takipten çıkıldı!", Toast.LENGTH_SHORT).show();
                        updateFollowCount(firestore, currentUserId, targetUserId, -1); // -1 ile azalt
                    })
                    .addOnFailureListener(e -> showError(context, "Takipten çıkarma hatası: " + e.getMessage()));
        } else {
            // Takip et
            firestore.collection("Following").document(currentUserId)
                    .collection("following").document(targetUserId)
                    .set(followData)
                    .addOnSuccessListener(aVoid -> {
                        followButton.setText("Takibi Bırak");
                        Toast.makeText(context, "Takip ediliyor!", Toast.LENGTH_SHORT).show();
                        updateFollowCount(firestore, currentUserId, targetUserId, 1); // 1 ile artır
                    })
                    .addOnFailureListener(e -> showError(context, "Takip hatası: " + e.getMessage()));
        }
    }

    private void updateFollowCount(FirebaseFirestore firestore, String currentUserId, String targetUserId, int increment) {
        // Öncelikle, hedef kullanıcının belgesinin varlığını kontrol et
        firestore.collection("Users").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Takip edilen kullanıcının takipçi sayısını güncelle
                        firestore.collection("Users").document(targetUserId)
                                .update("takipciSayisi", FieldValue.increment(increment))
                                .addOnSuccessListener(aVoid -> Log.d("FollowCount", "Takipçi sayısı güncellendi: " + increment))
                                .addOnFailureListener(e -> Log.e("FollowCount", "Takipçi sayısı güncellenirken hata: " + e.getMessage()));
                    } else {
                        Log.e("FollowCount", "Hedef kullanıcı belgesi bulunamadı: " + targetUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowCount", "Kullanıcı belgesini alma hatası: " + e.getMessage()));

        // Takip eden kullanıcının takip sayısını güncelle
        firestore.collection("Users").document(currentUserId)
                .update("takipSayisi", FieldValue.increment(increment))
                .addOnSuccessListener(aVoid -> Log.d("FollowCount", "Takip sayısı güncellendihyhy: " + increment))
                .addOnFailureListener(e -> Log.e("FollowCount", "Takip sayısı güncellenirken hatayhy: " + e.getMessage()));
    }





    private void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }



    private void unfollowUser(FirebaseFirestore firestore, String currentUserId, String targetUserId, Button followButton, Context context) {
        firestore.collection("Following").document(currentUserId)
                .collection("following").document(targetUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    followButton.setText("Takip Et");
                    Toast.makeText(context, "Takipten çıkıldı!", Toast.LENGTH_SHORT).show();

                    // Takip edilen kullanıcının takipçi sayısını azalt
                    firestore.collection("Users").document(targetUserId)
                            .update("takipciSayisi", FieldValue.increment(-1));

                    // Takip eden kullanıcının takip etme sayısını azalt
                    firestore.collection("Users").document(currentUserId)
                            .update("takipEtmeSayisi", FieldValue.increment(-1));
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Takipten çıkarma hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void checkFollowingStatus(FirebaseFirestore firestore, String currentUserId, String targetUserId, Button followButton, OnCheckFollowingStatusListener listener) {
        if (currentUserId == null || targetUserId == null) {
            Log.e("HesapSayfa", "User IDs cannot be null");
            followButton.setEnabled(false);
            return;
        }

        // Kendini takip edemezsin kontrolü
        if (currentUserId.equals(targetUserId)) {
            Toast.makeText(getContext(), "Kendini takip edemezsin", Toast.LENGTH_SHORT).show();
            followButton.setEnabled(false);
            return;
        }

        // Takip durumu kontrolü
        firestore.collection("Following").document(currentUserId)
                .collection("following").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isFollowing = documentSnapshot.exists();
                    followButton.setText(isFollowing ? "Takibi Bırak" : "Takip Et");
                    listener.onCheckComplete(isFollowing);
                })
                .addOnFailureListener(e -> {
                    followButton.setText("Takip Et");
                    listener.onCheckComplete(false);
                });

    }
    interface OnCheckFollowingStatusListener {
        void onCheckComplete(boolean isFollowing);
    }



}
