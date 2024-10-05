package com.example.myapplication;

import static com.example.myapplication.DateUtils.getTimeAgo;

import static java.nio.file.Paths.get;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LikesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LikesFragment extends Fragment {

    private String postId;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private AdapterUsers usersAdapter;
    private List<PostUser> userList; // PostUser, kullanıcı adını ve profil fotoğrafını tutan bir model sınıfı

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        usersAdapter = new AdapterUsers(userList); // UsersAdapter, profil fotoğraflarını göstermek için kullanılan adaptör
        recyclerView.setAdapter(usersAdapter);
        loadLikes(postId); // beğenileri yükle
        return view;
    }

    private void loadLikes(String postId) {
        firebaseFirestore.collection("UsersLiked")
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedUsers = (List<String>) documentSnapshot.get("LikedFrom");
                        if (likedUsers != null) {
                            // Kullanıcı adlarını al ve profil fotoğraflarını yükle
                            getUserProfiles(likedUsers);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Hata durumu
                    Log.e("LikesFragment", "Error getting likes", e);
                });
    }

    private void getUserProfiles(List<String> likedUsers) {
        userList.clear(); // Yeni veriler eklenmeden önce listeyi temizle

        if (likedUsers.isEmpty()) {
            usersAdapter.notifyDataSetChanged(); // Eğer hiç kullanıcı yoksa adaptörü güncelle
            return;
        }

        // Tüm kullanıcıları tek seferde al
        firebaseFirestore.collection("Users")
                .whereIn("username", likedUsers) // likedUsers listesindeki kullanıcıları filtrele
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("username");
                        String profilePhoto = document.getString("profilePhoto");
                        PostUser postUser = new PostUser(username, profilePhoto); // PostUser modelini doldur
                        userList.add(postUser); // Kullanıcıyı listeye ekle
                    }
                    usersAdapter.notifyDataSetChanged(); // Adaptörü güncelle
                })
                .addOnFailureListener(e -> {
                    // Hata durumu
                    Log.e("LikesFragment", "Error getting user profiles", e);
                });
    }

}


