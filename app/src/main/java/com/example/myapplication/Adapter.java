package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Adapter extends RecyclerView.Adapter<Adapter.PostViewHolder> {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Context context;
    private List<Post> postList;
    private FirebaseFirestore firebaseFirestore;

    private ImageView selectPhotoButton, imageView, delete, selectedPhoto;
    private Button gonder;
    private EditText editText;
    private TextView usernameLabel;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    public String postId;
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
        holder.username.setText("@" + post.username);
        holder.tarih.setText(post.date);

        if (post.image != null) {
            holder.postImage.setVisibility(View.VISIBLE);
            Picasso.get().load(post.image).into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        postId=post.id;

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
                });

        // Profil fotoğrafına tıklama olayı
        holder.PP.setOnClickListener(v -> openProfilePage(post.username));

        // Kullanıcı adına tıklama olayı
        holder.username.setOnClickListener(v -> openProfilePage(post.username));

        // Gönderi metnine tıklama olayı
        holder.postText.setOnClickListener(v -> openPostDetail(post));

        // Gönderi resmine tıklama olayı
        holder.postImage.setOnClickListener(v -> openPostDetail(post));

        // Cevapla butonuna tıklama olayı
        holder.replyButton.setOnClickListener(v -> {
            // Cevapla işlemini burada yapabilirsiniz
            SendReply();
        });

        // Yorum butonuna tıklama olayı
        holder.commentButton.setOnClickListener(v -> {
            // Yorum işlemi burada yapılabilir

        });

        // Beğen butonuna tıklama olayı
        holder.likeButton.setOnClickListener(v -> {
            // Beğenme işlemini burada yapabilirsiniz

        });
    }

    private void openProfilePage(String username) {
        // HesapSayfa fragmentına geçiş kodu
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HesapSayfa hesapSayfaFragment = new HesapSayfa();
        hesapSayfaFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.frame_layout, hesapSayfaFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    private void SendReply() {
        // HesapSayfa fragmentına geçiş kodu
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, null);

        // Cevaplama için dialog penceresi aç
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // Context'i burada kullan
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.yanit_yukleme_ekran, null);
        builder.setView(dialogView);

        ImageView profilePhoto = dialogView.findViewById(R.id.profilePhoto);
        selectedPhoto = dialogView.findViewById(R.id.selectedPhoto);
        selectPhotoButton = dialogView.findViewById(R.id.selectPhotoButton);
        editText = dialogView.findViewById(R.id.editText);
        gonder = dialogView.findViewById(R.id.gonder);
        delete = dialogView.findViewById(R.id.delete);
        usernameLabel = dialogView.findViewById(R.id.usernameMy);
        imageView = dialogView.findViewById(R.id.imageView);
        progressBar = dialogView.findViewById(R.id.progressBar);


        TextView usernameSend = dialogView.findViewById(R.id.usernameSend);
        TextView postTextSend = dialogView.findViewById(R.id.post_textSend);
        ImageView postImageSend = dialogView.findViewById(R.id.post_imageSend);
        ImageView profilePhotoSend = dialogView.findViewById(R.id.profilePhotoSend);

        // Kullanıcının profil resmini ve adını getir


        // Dialogu göster

        firebaseFirestore.collection("Users").whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            if (snapshot.exists()) {
                                Map<String, Object> data = snapshot.getData();
                                usernameLabel.setText("@"+username);
                                String profilePicture = (String) data.get("profilePhoto");


                                if (profilePicture != null) {
                                    Picasso.get().load(profilePicture).into(profilePhoto);
                                }
                            }
                        }
                    }
                });


        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Post").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Post verilerini al
                            String usernameeSend = documentSnapshot.getString("username");
                            String metinSend = documentSnapshot.getString("metin");
                            String imageUrlSend = documentSnapshot.getString("image");

                            // Verileri UI bileşenlerine yerleştir
                            usernameSend.setText("@" + usernameeSend);
                            postTextSend.setText(metinSend);

                            if (imageUrlSend != null && !imageUrlSend.isEmpty()) {
                                postImageSend.setVisibility(View.VISIBLE);
                                Picasso.get().load(imageUrlSend).into(postImageSend);
                            } else {
                                postImageSend.setVisibility(View.GONE);
                            }
                            firebaseFirestore.collection("Users").whereEqualTo("username", usernameeSend)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                if (snapshot.exists()) {
                                                    Map<String, Object> data = snapshot.getData();

                                                    String profilePictureSend = (String) data.get("profilePhoto");


                                                    if (profilePictureSend != null) {
                                                        Picasso.get().load(profilePictureSend).into(profilePhotoSend);
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });

        // Dialogu göster
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


        selectPhotoButton.setOnClickListener(v -> openGallery());

        delete.setOnClickListener(v -> {
            selectedPhoto.setImageResource(0); // Clear the image
            selectedPhoto.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            selectPhotoButton.setVisibility(View.VISIBLE);
        });

        gonder.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {

                gonder.setVisibility(View.GONE);
                if (selectedPhoto.getVisibility() == View.VISIBLE) {
                    uploadImageToStorage(text);
                } else {
                    uploadTextToFirestore(text, null);
                }
            } else if (selectedPhoto.getVisibility() == View.VISIBLE) {
                uploadImageToStorage(null);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((AppCompatActivity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void uploadTextToFirestore(String text, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the username from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "Unknown User");

        // Get current date and time as Timestamp
        Date currentDate = new Date();
        Timestamp currentTimestamp = new Timestamp(currentDate);

        // Öncelikle mevcut belge sayısını bul

        // Firestore'daki mevcut belge sayısını al


        // Veriyi hazırlama
        Map<String, Object> post = new HashMap<>();
        post.put("metin", text);
        post.put("username", username);
        post.put("date", currentTimestamp); // Zaman damgasını ekle
        post.put("repyledPost", postId);

        if (imageUrl != null) {
            post.put("image", imageUrl);
        }

        progressBar.setVisibility(View.VISIBLE);

        // Veriyi Firestore'a ekle
        db.collection("Post")
                .add(post)
                .addOnSuccessListener(documentReference -> {

                    progressBar.setProgress(100);

                    // 2 saniye gecikme ile işlemleri gerçekleştir
                    new Handler().postDelayed(() -> {
                        // Progress barı gizle
                        progressBar.setVisibility(View.GONE);

                        // UI elemanlarını sıfırla
                        delete.setVisibility(View.GONE);
                        selectedPhoto.setVisibility(View.GONE);
                        selectedPhoto.setImageResource(0);
                        selectPhotoButton.setVisibility(View.VISIBLE);
                        editText.setText(null);
                        gonder.setVisibility(View.VISIBLE);
                    }, 2000);
                })
                .addOnFailureListener(e -> {

                });


    }

    private void uploadImageToStorage(String text) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference postRef = storageRef.child("PostPhoto/" + UUID.randomUUID().toString());

        selectedPhoto.setDrawingCacheEnabled(true);
        selectedPhoto.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) selectedPhoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();


        progressBar.setVisibility(View.VISIBLE);

        UploadTask uploadTask = postRef.putBytes(data);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            postRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                uploadTextToFirestore(text, imageUrl);
            });
        }).addOnFailureListener(exception -> {

        });
    }

    private void openPostDetail(Post post) {
        Intent intent = new Intent(context, PostDetay2.class);
        intent.putExtra("metin", post.metin);
        intent.putExtra("id", post.id);
        intent.putExtra("username", post.username);
        intent.putExtra("date", post.date);
        intent.putExtra("image", post.image);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postText, username, tarih;
        ImageView postImage, PP;
        Button replyButton, commentButton, likeButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postText = itemView.findViewById(R.id.post_text);
            postImage = itemView.findViewById(R.id.post_image);
            PP = itemView.findViewById(R.id.profilePhoto);
            username = itemView.findViewById(R.id.username);
            tarih = itemView.findViewById(R.id.tarih);

            // Butonları tanımlıyoruz
            replyButton = itemView.findViewById(R.id.btn_reply);
            commentButton = itemView.findViewById(R.id.btn_comment);
            likeButton = itemView.findViewById(R.id.btn_like);
        }
    }
}

