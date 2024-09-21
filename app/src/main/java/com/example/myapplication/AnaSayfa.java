package com.example.myapplication;


import static com.example.myapplication.DateUtils.getTimeAgo;

import android.animation.TimeAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.text.format.DateUtils;

public class AnaSayfa extends Fragment {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Post> postList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ana_sayfa, container, false);
//selamlar sevgiler
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapter = new Adapter(getContext(), postList);
        recyclerView.setAdapter(adapter);
        firebaseFirestore = FirebaseFirestore.getInstance();



        getData();

        return view;
    }

    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < DateUtils.HOUR_IN_MILLIS) {
            // Less than an hour, show in minutes
            return (diff / DateUtils.MINUTE_IN_MILLIS) + " dakika önce";
        } else if (diff < DateUtils.DAY_IN_MILLIS) {
            // Less than a day, show in hours
            long hours = diff / DateUtils.HOUR_IN_MILLIS;
            return hours + " saat önce";
        } else if (diff < DateUtils.WEEK_IN_MILLIS) {
            // Less than a week, show in days
            long days = diff / DateUtils.DAY_IN_MILLIS;
            return days + " gün önce";
        } else if (diff < 56 * DateUtils.WEEK_IN_MILLIS) {
            // Less than 56 weeks, show in weeks
            long weeks = diff / DateUtils.WEEK_IN_MILLIS;
            return weeks + " hafta önce";
        } else {
            // More than 56 weeks, show the date in "dd.MM.yyyy" format
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(timestamp.toDate());
        }
    }


    private void getData() {
        firebaseFirestore.collection("Post")
                .orderBy("date", Query.Direction.DESCENDING) // Date alanına göre sıralama
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            postList.clear(); // Yeni veriler eklenmeden önce listeyi temizleyin

                            for (DocumentSnapshot snapshot : value.getDocuments()) {
                                Map<String, Object> data = snapshot.getData();
                                if (data != null) {
                                    String id = snapshot.getId();
                                    String metin = (String) data.get("metin");
                                    String image = (String) data.get("image");
                                    String username = (String) data.get("username");
                                    String replyId = (String) data.get("repyledPost");
                                    Timestamp date = (Timestamp) data.get("date");
                                    String date2 = getTimeAgo(date);

                                    // Kullanıcının profil fotoğrafını getirme
                                    firebaseFirestore.collection("Users").whereEqualTo("username", username)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                String pp = null;

                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);
                                                    pp = userSnapshot.getString("profilePhoto");
                                                }

                                                // Post objesini oluştur ve listeye ekle


                                                // Liste güncellendikten sonra adapter'i bilgilendirin
                                                adapter.notifyDataSetChanged();
                                            });
                                    if (replyId!=null){
                                        Post post = new Post(id,replyId, metin, image, username, date2, image);
                                        postList.add(post);
                                    }
                                    else{
                                        Post post = new Post(id,null, metin, image, username, date2, image);
                                        postList.add(post);
                                    }

                                }
                            }
                        }
                    }
                });
    }

    public static String getTimeAgo(long timeDiff) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long weeks = days / 7;

        if (weeks > 0) {
            return weeks + " hafta önce";
        } else if (days > 0) {
            return days + " gün önce";
        } else if (hours > 0) {
            return hours + " saat önce";
        } else if (minutes > 0) {
            return minutes + " dakika önce";
        } else {
            return seconds + " saniye önce";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup if needed
    }
}
