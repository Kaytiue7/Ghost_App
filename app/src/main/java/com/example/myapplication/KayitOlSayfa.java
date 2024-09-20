package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class KayitOlSayfa extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button kayitButton;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit_ol_sayfa);

        // Firebase başlatma
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI bileşenlerini bağlama
        usernameEditText = findViewById(R.id.kayit_username);
        passwordEditText = findViewById(R.id.kayit_password);
        kayitButton = findViewById(R.id.kayit_button);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Kayıt butonuna tıklama olayı
        kayitButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        checkUsernameExists(username, password);
    }

    private void checkUsernameExists(String username, String password) {
        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // Kullanıcı adı zaten var
                                Toast.makeText(KayitOlSayfa.this, "Bu kullanıcı adı zaten kullanılıyor.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Kullanıcı adı mevcut değil, devam edebiliriz
                                saveUserDetails(username, password);
                            }
                        } else {
                            // Hata oluştu
                            Toast.makeText(KayitOlSayfa.this, "Kullanıcı adı kontrolü başarısız oldu", Toast.LENGTH_SHORT).show();
                            Log.e("KayitOlSayfa", "Kullanıcı adı kontrol hatası", task.getException());
                        }
                    }
                });
    }

    private void saveUserDetails(String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Başarıyla kaydedildi
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_USERNAME, username);
                    editor.apply();

                    Toast.makeText(KayitOlSayfa.this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(KayitOlSayfa.this, ProfilFoto.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hata oluştu
                    Toast.makeText(KayitOlSayfa.this, "Kayıt başarısız oldu", Toast.LENGTH_SHORT).show();
                    Log.e("KayitOlSayfa", "Kayıt hatası", e);
                });
    }
}
