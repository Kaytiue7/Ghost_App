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
            // Şifreyi şifreleme işlemi
            String encryptedPassword = encryptPassword(password);

            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("password", encryptedPassword);

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
