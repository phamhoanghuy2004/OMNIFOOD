package com.example.shortvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.shortvideo.adapter.MyVideoAdapter;
import com.example.shortvideo.model.Video1Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {


    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtName, edtEmail;
    private Button btnUpdate;
    private ImageView imgAvatar;
    private Uri selectedImageUri;

    MyVideoAdapter adapter;
    List<Video1Model> listVideo;
    RecyclerView rvMyVideos;

    @Override
    protected void onStop() {
        super.onStop();
        Intent i = new Intent();
        i.setClass(ProfileActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        btnUpdate = findViewById(R.id.btnUpDate);
        imgAvatar = findViewById(R.id.imgAvatar);
        rvMyVideos = findViewById(R.id.rvMyVideos);

        loadVideoOfUser();

        SessionManager sessionManager = new SessionManager(ProfileActivity.this);
        edtName.setText(sessionManager.getUsername());
        edtEmail.setText(sessionManager.getEmail());
        Glide.with(ProfileActivity.this)
                .load(sessionManager.getImg())
                .circleCrop()
                .placeholder(R.drawable.ic_person_pin)
                .into(imgAvatar);


        // Mở gallery khi click vào ảnh
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageUri == null) {
                    Toast.makeText(ProfileActivity.this, "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
                }

                ProgressDialog dialog = ProgressDialog.show(ProfileActivity.this, "", "Đang đăng ký...", true);

                // Upload ảnh lên Cloudinary
                MediaManager.get().upload(selectedImageUri).callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();

                        // Lấy ID người dùng hiện tại
                        String userId = new SessionManager(ProfileActivity.this).getUserId();

                        // Tạo dữ liệu cần cập nhật
                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("uriImg", imageUrl);
                        updateMap.put("name", edtName.getText().toString());
                        updateMap.put("email", edtEmail.getText().toString());

                        // Cập nhật vào Firebase
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(userId)
                                .updateChildren(updateMap)
                                .addOnSuccessListener(aVoid -> {
                                    dialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                                    // Cập nhật lại thông tin local (nếu bạn lưu session)
                                    SessionManager session = new SessionManager(ProfileActivity.this);
                                    session.saveUser(userId, edtEmail.getText().toString(), imageUrl, edtName.getText().toString());
                                    Intent i = new Intent();
                                    i.setClass(ProfileActivity.this,MainActivity.class);
                                    startActivity(i);
                                })
                                .addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Lỗi cập nhật Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        dialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Lỗi Cloudinary: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }


                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
            }
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

    private void loadVideoOfUser (){
        listVideo = new ArrayList<>();
        adapter = new MyVideoAdapter(listVideo);
        rvMyVideos.setLayoutManager(new LinearLayoutManager(this));
        rvMyVideos.setAdapter(adapter);

        // Lấy id user từ session
        String currentUserId = new SessionManager(this).getUserId();

        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("Video");

        videoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listVideo.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Video1Model video = data.getValue(Video1Model.class);
                    if (video != null && video.getIdUser().equals(currentUserId)) {
                        listVideo.add(video);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}