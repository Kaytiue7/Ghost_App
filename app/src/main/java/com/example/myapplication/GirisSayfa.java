package com.example.myapplication;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GirisSayfa extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button girisButton;
    private TextView kayitOl;
    private FirebaseAuth auth;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_sayfa);

        // Firebase başlatma
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI bileşenlerini bağlama
        usernameEditText = findViewById(R.id.giris_username);
        passwordEditText = findViewById(R.id.giris_password);
        girisButton = findViewById(R.id.giris_button);
        kayitOl = findViewById(R.id.kayit_ol);

        // SharedPreferences başlatma
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Yerel veritabanından username verisini al
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        if (storedUsername!=null ) {
            navigateToMainActivity();
            Toast.makeText(this, "" + storedUsername, Toast.LENGTH_SHORT).show();
        }



        Toast.makeText(this, "" + storedUsername, Toast.LENGTH_SHORT).show();
        kayitOl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GirisSayfa.this, KayitOlSayfa.class);
                startActivity(intent);
                finish();
            }
        });

        // Oturum açma durumu kontrolü


        // Giriş butonuna tıklama olayı
        girisButton.setOnClickListener(v -> loginUser());
    }



    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String usernamefirebase = document.getString("username");
                            String passwordfirebase = document.getString("password");

                            if (username.equals(usernamefirebase) && password.equals(passwordfirebase)) {
                                // Kullanıcı adı yerel veritabanına kaydediliyor
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_USERNAME, username);
                                editor.apply();


                                navigateToMainActivity();


                                return;
                            }
                        }
                        Toast.makeText(GirisSayfa.this, "Geçersiz kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(GirisSayfa.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
