package com.example.shortvideo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActicity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin,btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acticity);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.bthSignUp);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // Lấy thông tin người dùng
                                        String id = snapshot.child("id").getValue(String.class);
                                        String name = snapshot.child("name").getValue(String.class);
                                        String uriImg = snapshot.child("uriImg").getValue(String.class);
                                        String emailUser =  FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                        SessionManager sessionManager = new SessionManager(LoginActicity.this);
                                        sessionManager.saveUser(id,emailUser,uriImg,name);

                                        // Ví dụ: lưu vào sharedPreferences, intent, hoặc hiển thị
                                        Toast.makeText(LoginActicity.this, "Chào " + name, Toast.LENGTH_SHORT).show();

                                        // Chuyển sang MainActivity
                                        Intent intent = new Intent(LoginActicity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActicity.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(LoginActicity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // Thất bại
                            Toast.makeText(LoginActicity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(LoginActicity.this,SignUpActivity.class);
                startActivity(i);
            }
        });
    }
}