package com.example.myapplication;

import static java.nio.file.Paths.get;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            Toast.makeText(getContext(), ""+postId, Toast.LENGTH_SHORT).show();
        }
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);
        Log.d("LikesFragment", "Fragment created"); // Log ekleyin
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        usersAdapter = new AdapterUsers(userList);
        recyclerView.setAdapter(usersAdapter);
        loadLikes(postId);
        return view;
    }


    private void loadLikes(String postId) {
        Log.d("LikesFragment", "Loading likes for post ID: " + postId); // Log ekleyin
        firebaseFirestore.collection("UsersLiked")
                .whereEqualTo("PostId", postId) // PostId alanına göre sorgu
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String likedUsersString = document.getString("LikedFrom");
                            Log.d("LikesFragment", "LikedFrom: " + likedUsersString); // Log ekleyin
                            if (likedUsersString != null && !likedUsersString.isEmpty()) {
                                List<String> likedUsers = new ArrayList<>(Arrays.asList(likedUsersString.split(",")));
                                Log.d("LikesFragment", "Liked Users: " + likedUsers); // Log ekleyin
                                getUserProfiles(likedUsers);
                            } else {
                                Log.d("LikesFragment", "No users liked this post."); // Log ekleyin
                            }
                        }
                    } else {
                        Log.d("LikesFragment", "Post document does not exist."); // Log ekleyin
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LikesFragment", "Error getting likes", e);
                });
    }



    private void getUserProfiles(List<String> likedUsers) {
        userList.clear();

        if (likedUsers.isEmpty()) {
            Log.d("LikesFragment", "No liked users to retrieve profiles for."); // Log ekleyin
            usersAdapter.notifyDataSetChanged();
            return;
        }

        firebaseFirestore.collection("Users")
                .whereIn("username", likedUsers)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("username");
                        String profilePhoto = document.getString("profilePhoto");
                        Log.d("LikesFragment", "User: " + username + ", Profile Photo: " + profilePhoto); // Log ekleyin
                        PostUser postUser = new PostUser(username, profilePhoto);
                        userList.add(postUser);
                    }
                    usersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("LikesFragment", "Error getting user profiles", e);
                });
    }



}


