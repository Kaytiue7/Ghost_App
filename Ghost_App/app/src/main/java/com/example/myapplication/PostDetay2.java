package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class PostDetay2 extends AppCompatActivity {
    private Post post;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detay2);
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            String metin = intent.getStringExtra("metin");
            String username = intent.getStringExtra("username");
            String date = intent.getStringExtra("date");
            String image = intent.getStringExtra("image");

            TextView postTextView = findViewById(R.id.post_text_view);
            TextView usernameTextView = findViewById(R.id.username_text_view);
            TextView dateTextView = findViewById(R.id.date_text_view);
            ImageView postImageView = findViewById(R.id.post_image_view);
            ImageView ppImageView = findViewById(R.id.pp_image_view);

            postTextView.setText(metin);
            usernameTextView.setText(username);
            dateTextView.setText(date);

            if (image != null) {
                Picasso.get().load(image).into(postImageView);
            }
            else {
                postImageView.setVisibility(View.GONE);
            }

            firebaseFirestore.collection("Users").whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {


                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);
                            String pp = userSnapshot.getString("profilePhoto");
                            Picasso.get().load(pp).into(ppImageView);
                        }

                        // Create the Post object and add it to the list

                    });
            Picasso.get().load(image).into(postImageView);
        }

    }
}