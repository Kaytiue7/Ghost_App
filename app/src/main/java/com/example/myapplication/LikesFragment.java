package com.example.myapplication;

import static java.nio.file.Paths.get;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private List<PostUser> filteredUserList; // Filtrelenmiş kullanıcı listesi
    private EditText searchEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            Toast.makeText(getContext(), "" + postId, Toast.LENGTH_SHORT).show();
        }
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchEditText = view.findViewById(R.id.searchEditText);
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        usersAdapter = new AdapterUsers(filteredUserList); // Başlangıçta filtrelenmiş listeyi bağla
        recyclerView.setAdapter(usersAdapter);

        // Arama kutusunu izlemek için TextWatcher ekleyelim
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Boş bırakılabilir
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Boş bırakılabilir
            }
        });

        loadLikes(postId);
        return view;
    }

    private void loadLikes(String postId) {
        firebaseFirestore.collection("UsersLiked")
                .whereEqualTo("PostId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String likedUsersString = document.getString("LikedFrom");
                            if (likedUsersString != null && !likedUsersString.isEmpty()) {
                                List<String> likedUsers = new ArrayList<>(Arrays.asList(likedUsersString.split(",")));
                                getUserProfiles(likedUsers);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LikesFragment", "Error getting likes", e);
                });
    }

    private void getUserProfiles(List<String> likedUsers) {
        userList.clear();

        if (likedUsers.isEmpty()) {
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
                        PostUser postUser = new PostUser(username, profilePhoto);
                        userList.add(postUser);
                    }
                    filterUsers(searchEditText.getText().toString()); // İlk yüklemede aramayı uygula
                })
                .addOnFailureListener(e -> {
                    Log.e("LikesFragment", "Error getting user profiles", e);
                });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList); // Arama boşsa tüm listeyi göster
        } else {
            for (PostUser user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
        usersAdapter.notifyDataSetChanged(); // Listeyi güncelle
    }
}


