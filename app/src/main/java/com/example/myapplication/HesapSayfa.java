package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HesapSayfa extends Fragment {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Post> postList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static String pp=null;
    private static String  date =null;
private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    public ImageView imageView;
    public TextView textView ;
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

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getData);

        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        imageView = view.findViewById(R.id.profilePhoto);
        textView = view.findViewById(R.id.username);


        showPerson(storedUsername);
        getData();
        return view;
    }

    public void showPerson(String storedUsername){

        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("Users").whereEqualTo("username", storedUsername)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            if (snapshot.exists()) {
                                Map<String, Object> data = snapshot.getData();
                                String username = (String) data.get("username");
                                String ProfilePicture = (String) data.get("profilePhoto");

                                textView.setText("@"+username);
                                if (ProfilePicture!=null){
                                    Picasso.get().load(ProfilePicture).into(imageView);

                                }
                                else {
                                    imageView.setImageResource(R.drawable.my_account);
                                }

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Veri yükleme hatası: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getTimeAgo(Timestamp timestamp) {
        long time = timestamp.toDate().getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < android.text.format.DateUtils.HOUR_IN_MILLIS) {
            // Less than an hour, show in minutes
            return (diff / android.text.format.DateUtils.MINUTE_IN_MILLIS) + " dakika önce";
        } else if (diff < android.text.format.DateUtils.DAY_IN_MILLIS) {
            // Less than a day, show in hours
            long hours = diff / android.text.format.DateUtils.HOUR_IN_MILLIS;
            return hours + " saat önce";
        } else if (diff < android.text.format.DateUtils.WEEK_IN_MILLIS) {
            // Less than a week, show in days
            long days = diff / android.text.format.DateUtils.DAY_IN_MILLIS;
            return days + " gün önce";
        } else if (diff < 56 * android.text.format.DateUtils.WEEK_IN_MILLIS) {
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

        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        if (getArguments() != null) {
            storedUsername = getArguments().getString("username");
        }

        firebaseFirestore.collection("Post")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereEqualTo("username", storedUsername)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            //Toast.makeText(getContext(), "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                                    String username = (String) data.get("username");
                                    Timestamp date =(Timestamp) data.get("date");
                                    String date2 = getTimeAgo(date);

                                    // Fetch the profilePhoto for the username
                                    firebaseFirestore.collection("Users").whereEqualTo("username", username)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                String pp = null; // Profile photo başlangıçta null

                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);
                                                    pp = userSnapshot.getString("profilePhoto");
                                                }

                                                // Create the Post object and add it to the list
                                                Post post = new Post(id,metin, image, username, date2, pp);
                                                postList.add(post);
                                                adapter.notifyDataSetChanged();
                                                swipeRefreshLayout.setRefreshing(false); // Stop refreshing
                                            });

                                }
                            }
                        }
                    }
                });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup if needed
    }
}
