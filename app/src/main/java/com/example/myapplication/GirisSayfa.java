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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GirisSayfa extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button girisButton;
    private TextView kayitOl;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_sayfa);

        // Firebase başlatma
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        if (storedUsername != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Users koleksiyonunda storedUsername ile eşleşen verileri sorgula
            db.collection("Users")
                    .whereEqualTo("username", storedUsername) // username alanını kontrol et
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean userExists = false; // Kullanıcı mevcut mu kontrolü

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExists = true; // Eğer bir kullanıcı bulunduysa
                                break;
                            }

                            if (userExists) {
                                // Kullanıcı bulundu, MainActivity'ye yönlendir
                                navigateToMainActivity();
                                Toast.makeText(this, "Hoş geldiniz, " + storedUsername, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Sorgulama hatası: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }



        // UI bileşenlerini bağlama
        usernameEditText = findViewById(R.id.giris_username);
        passwordEditText = findViewById(R.id.giris_password);
        girisButton = findViewById(R.id.giris_button);
        kayitOl = findViewById(R.id.kayit_ol);

        // SharedPreferences başlatma
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Giriş butonuna tıklama olayı
        girisButton.setOnClickListener(v -> loginUser());

        // Kayıt olma butonuna tıklama olayı
        kayitOl.setOnClickListener(v -> {
            Intent intent = new Intent(GirisSayfa.this, KayitOlSayfa.class);
            startActivity(intent);

        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kullanıcı adını ve şifreyi veritabanından kontrol et
        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String usernamefirebase = document.getString("username");
                            String passwordfirebase = document.getString("password");

                            // Şifreyi şifrele
                            String encryptedPassword = encryptPassword(password);

                            // Giriş kontrolü
                            if (username.equals(usernamefirebase) && encryptedPassword.equals(passwordfirebase)) {
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

    private String encryptPassword(String password) {
        StringBuilder encrypted = new StringBuilder();
        for (char c : password.toCharArray()) {
            switch (c) {
                case 'A':
                    encrypted.append("1");
                    break;
                case 'B':
                    encrypted.append("!");
                    break;
                case 'C':
                    encrypted.append("Z");
                    break;
                case 'D':
                    encrypted.append("2");
                    break;
                case 'E':
                    encrypted.append("X");
                    break;
                case 'F':
                    encrypted.append("#");
                    break;
                case 'G':
                    encrypted.append("A");
                    break;
                case 'H':
                    encrypted.append("3");
                    break;
                case 'I':
                    encrypted.append("$");
                    break;
                case 'J':
                    encrypted.append("Q");
                    break;
                case 'K':
                    encrypted.append("^");
                    break;
                case 'L':
                    encrypted.append("D");
                    break;
                case 'M':
                    encrypted.append("4");
                    break;
                case 'N':
                    encrypted.append("@");
                    break;
                case 'O':
                    encrypted.append("R");
                    break;
                case 'P':
                    encrypted.append("5");
                    break;
                case 'Q':
                    encrypted.append("T");
                    break;
                case 'R':
                    encrypted.append("6");
                    break;
                case 'S':
                    encrypted.append("*");
                    break;
                case 'T':
                    encrypted.append("7");
                    break;
                case 'U':
                    encrypted.append("E");
                    break;
                case 'V':
                    encrypted.append("8");
                    break;
                case 'W':
                    encrypted.append("Y");
                    break;
                case 'X':
                    encrypted.append("U");
                    break;
                case 'Y':
                    encrypted.append("9");
                    break;
                case 'Z':
                    encrypted.append(")");
                    break;
                case 'a':
                    encrypted.append("0");
                    break;
                case 'b':
                    encrypted.append("?");
                    break;
                case 'c':
                    encrypted.append("W");
                    break;
                case 'd':
                    encrypted.append("!");
                    break;
                case 'e':
                    encrypted.append("J");
                    break;
                case 'f':
                    encrypted.append("5");
                    break;
                case 'g':
                    encrypted.append("H");
                    break;
                case 'h':
                    encrypted.append("6");
                    break;
                case 'i':
                    encrypted.append("^");
                    break;
                case 'j':
                    encrypted.append("K");
                    break;
                case 'k':
                    encrypted.append("M");
                    break;
                case 'l':
                    encrypted.append("S");
                    break;
                case 'm':
                    encrypted.append("8");
                    break;
                case 'n':
                    encrypted.append("Z");
                    break;
                case 'o':
                    encrypted.append("F");
                    break;
                case 'p':
                    encrypted.append("3");
                    break;
                case 'q':
                    encrypted.append("B");
                    break;
                case 'r':
                    encrypted.append("4");
                    break;
                case 's':
                    encrypted.append("V");
                    break;
                case 't':
                    encrypted.append("P");
                    break;
                case 'u':
                    encrypted.append("G");
                    break;
                case 'v':
                    encrypted.append("2");
                    break;
                case 'w':
                    encrypted.append("T");
                    break;
                case 'x':
                    encrypted.append("L");
                    break;
                case 'y':
                    encrypted.append("A");
                    break;
                case 'z':
                    encrypted.append("7");
                    break;
                case '0':
                    encrypted.append("G");
                    break;
                case '1':
                    encrypted.append("H");
                    break;
                case '2':
                    encrypted.append("B");
                    break;
                case '3':
                    encrypted.append("O");
                    break;
                case '4':
                    encrypted.append("J");
                    break;
                case '5':
                    encrypted.append("T");
                    break;
                case '6':
                    encrypted.append("N");
                    break;
                case '7':
                    encrypted.append("E");
                    break;
                case '8':
                    encrypted.append("Y");
                    break;
                case '9':
                    encrypted.append("Q");
                    break;
                default:
                    encrypted.append(c); // Şifreleme dışında kalan karakterler
                    break;
            }
        }
        return encrypted.toString();
    }
}
