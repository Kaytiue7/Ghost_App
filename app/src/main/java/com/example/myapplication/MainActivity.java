package com.example.myapplication;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView selectPhotoButton, imageView,delete,selectedPhoto;
    private Button gonder;
    private EditText editText;
    private TextView textView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;


    private FirebaseFirestore firebaseFirestore;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Firebase Authentication'dan mevcut kullanıcıyı al
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Replace the fragment with AnaSayfa
        replaceFragment(new AnaSayfa());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setSelectedItemId(R.id.anasayfa); // Ilanlar tab'ını seçili hale getirin
        firebaseFirestore = FirebaseFirestore.getInstance();
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.anasayfa) {
                replaceFragment(new AnaSayfa());
            } else if (itemId == R.id.search) {
                replaceFragment(new AramaSayfa());
            } else if (itemId == R.id.add) {
                send();
            } else if (itemId == R.id.message) {
               // replaceFragment(new MesajSayfa());
                clearLocalDatabase();

            } else if (itemId == R.id.account) {
                replaceFragment(new HesapSayfa());
            }
            return true;
        });




    }
    private void clearLocalDatabase() {
        // SharedPreferences'i temizle
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME,null); // Yalnızca "username"i siler
        editor.apply(); // Değişiklikleri kaydeder

        Toast.makeText(this, "Username silindi", Toast.LENGTH_SHORT).show();

        // GirisSayfa aktivitesini başlat
        Intent intent = new Intent(MainActivity.this, GirisSayfa.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Geri tuşuyla ana sayfaya dönmeyi önler
        startActivity(intent);
        finish();  // MainActivity'yi kapatır
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @SuppressLint("MissingInflatedId")
    public void send() {

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, null);



        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                selectedPhoto.setImageBitmap(bitmap);
                selectedPhoto.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                selectPhotoButton.setVisibility(View.GONE);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadTextToFirestore(String text, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
                post.put("date", currentTimestamp);// Zaman damgasını ekle
                post.put("likeCount", 0);


                if (imageUrl != null) {
                    post.put("image", imageUrl);
                }

                progressBar.setVisibility(View.VISIBLE);

                // Veriyi Firestore'a ekle
                db.collection("Post")
                        .add(post)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(MainActivity.this, "Post added", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity.this, "Error adding post", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
