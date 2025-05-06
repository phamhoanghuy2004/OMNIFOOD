package com.example.shortvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.shortvideo.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtName, edtEmail, edtPassword;
    private Button btnSignUp;
    private ImageView imgAvatar;
    private Uri selectedImageUri;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnUpDate);
        imgAvatar = findViewById(R.id.imgAvatar);

        mAuth = FirebaseAuth.getInstance();

        // Mở gallery khi click vào ảnh
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSignUp.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin và chọn ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog dialog = ProgressDialog.show(this, "", "Đang đăng ký...", true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // Upload ảnh lên Cloudinary
                            MediaManager.get().upload(selectedImageUri)
                                    .callback(new UploadCallback() {
                                        @Override
                                        public void onStart(String requestId) {}

                                        @Override
                                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                                        @Override
                                        public void onSuccess(String requestId, Map resultData) {
                                            String imageUrl = resultData.get("secure_url").toString();

                                            // Tạo UserModel
                                            UserModel userModel = new UserModel(uid, name, password, imageUrl,email);

                                            // Lưu vào Firebase Database
                                            FirebaseDatabase.getInstance().getReference("users")
                                                    .child(uid)
                                                    .setValue(userModel)
                                                    .addOnCompleteListener(saveTask -> {
                                                        dialog.dismiss();
                                                        if (saveTask.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, "Lỗi lưu DB: " + saveTask.getException(), Toast.LENGTH_SHORT).show();
                                                            Log.e("SignUpError", "Đăng ký thất bại", task.getException());
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onError(String requestId, ErrorInfo error) {
                                            dialog.dismiss();
                                            Toast.makeText(SignUpActivity.this, "Lỗi Cloudinary: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onReschedule(String requestId, ErrorInfo error) {}
                                    })
                                    .dispatch();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgAvatar.setImageURI(selectedImageUri);
        }
    }
}
