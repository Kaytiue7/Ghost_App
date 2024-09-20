package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class Adapter extends RecyclerView.Adapter<Adapter.PostViewHolder> {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Context context;

    private List<Post> postList;
    private FirebaseFirestore firebaseFirestore;



    private ImageView selectPhotoButton, imageView,delete,selectedPhoto;
    private Button gonder;
    private EditText editText;
    private TextView textView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;



    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";

    private static final String KEY_USERNAME = "username";

    public Adapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new PostViewHolder(view);



    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.postText.setText(post.metin);
        holder.username.setText("@"+post.username);
        holder.tarih.setText(post.date);

        if (post.image != null) {
            holder.postImage.setVisibility(View.VISIBLE);
            Picasso.get().load(post.image).into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
/*/
        if (post.pp != null) {
            holder.PP.setImageDrawable(null);
            Picasso.get().load(post.pp).into(holder.PP);
        }

 */
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").whereEqualTo("username", post.username)
                .get()
                .addOnCompleteListener(task -> {
                    String pp = null;

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);
                        pp = userSnapshot.getString("profilePhoto");
                        holder.PP.setImageDrawable(null);
                        Picasso.get().load(pp).into(holder.PP);
                    }

                    // Post objesini oluştur ve listeye ekle


                    // Liste güncellendikten sonra adapter'i bilgilendirin

                });




        holder.PP.setOnClickListener(v -> {
            // Get the username from the post
            String username = post.username;

            // Create a bundle to send to the fragment
            Bundle bundle = new Bundle();
            bundle.putString("username", username);

            // Replace fragment with HesapSayfa and pass the bundle
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HesapSayfa hesapSayfaFragment = new HesapSayfa();
            hesapSayfaFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.frame_layout, hesapSayfaFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        holder.username.setOnClickListener(v -> {
            // Get the username from the post
            String username = post.username;

            // Create a bundle to send to the fragment
            Bundle bundle = new Bundle();
            bundle.putString("username", username);

            // Replace fragment with HesapSayfa and pass the bundle
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HesapSayfa hesapSayfaFragment = new HesapSayfa();
            hesapSayfaFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.frame_layout, hesapSayfaFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        holder.reply.setOnClickListener(v -> {

        });

        holder.postText.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetay2.class);
            intent.putExtra("metin", post.metin);
            intent.putExtra("id", post.id);
            intent.putExtra("username", post.username);
            intent.putExtra("date", post.date);
            intent.putExtra("image", post.image);
            context.startActivity(intent);
        });
        holder.postImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetay2.class);
            intent.putExtra("metin", post.metin);
            intent.putExtra("id", post.id);
            intent.putExtra("username", post.username);
            intent.putExtra("date", post.date);
            intent.putExtra("image", post.image);
            context.startActivity(intent);

        });
     }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postText, username,tarih;
        ImageView postImage;
        ImageView PP;
        Button reply;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postText = itemView.findViewById(R.id.post_text);
            reply = itemView.findViewById(R.id.reply);
            postImage = itemView.findViewById(R.id.post_image);
            PP = itemView.findViewById(R.id.profilePhoto);
            username = itemView.findViewById(R.id.username);
            tarih = itemView.findViewById(R.id.tarih);

        }
    }
    @SuppressLint("MissingInflatedId")
    public void send() {

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, null);



        AlertDialog.Builder builder = new AlertDialog.Builder(Adapter.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.yukleme_ekran, null); // Ensure your layout file name is correct
        builder.setView(dialogView);

        ImageView profilePhoto = dialogView.findViewById(R.id.profilePhoto);
        selectedPhoto = dialogView.findViewById(R.id.selectedPhoto);
        selectPhotoButton = dialogView.findViewById(R.id.selectPhotoButton);
        editText = dialogView.findViewById(R.id.editText);
        gonder = dialogView.findViewById(R.id.gonder);
        delete = dialogView.findViewById(R.id.delete);
        textView = dialogView.findViewById(R.id.textView);
        imageView= dialogView.findViewById(R.id.imageView);
        progressBar= dialogView.findViewById(R.id.progressBar);



        firebaseFirestore.collection("Users").whereEqualTo("username", username)
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
                });

        selectPhotoButton.setOnClickListener(v -> openGallery());

        delete.setOnClickListener(v -> {
            selectedPhoto.setImageResource(0); // Clear the image
            selectedPhoto.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            selectPhotoButton.setVisibility(View.VISIBLE);
        });
    }
}
