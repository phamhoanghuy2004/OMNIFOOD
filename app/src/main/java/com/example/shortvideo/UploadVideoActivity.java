package com.example.shortvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.shortvideo.model.Video1Model;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 100;
    private Uri selectedVideoUri;

    private EditText edtTitle, edtDescription;
    private Button btnSelectVideo, btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnUpload = findViewById(R.id.btnUpload);

        btnSelectVideo.setOnClickListener(v -> openGalleryForVideo());

        btnUpload.setOnClickListener(v -> {
            if (selectedVideoUri == null) {
                Toast.makeText(this, "Vui lòng chọn video!", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadVideoToCloudinary();
        });

    }

    private void uploadVideoToCloudinary() {
        ProgressDialog dialog = ProgressDialog.show(this, "", "Đang tải video lên...", true);

        MediaManager.get().upload(selectedVideoUri)
                .option("resource_type", "video")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        dialog.dismiss();
                        String videoUrl = resultData.get("secure_url").toString();
                        String title = edtTitle.getText().toString();
                        String description = edtDescription.getText().toString();
                        String userId = new SessionManager(UploadVideoActivity.this).getUserId();
                        Long like = 0L;
                        Long disLike = 0L;

                        Video1Model video = new Video1Model();
                        video.setIdUser(userId);
                        video.setUrl(videoUrl);
                        video.setTitle(title);
                        video.setDesc(description);
                        video.setLike(like);
                        video.setDislike(disLike);

                        FirebaseDatabase.getInstance().getReference("Video")
                                .push()
                                .setValue(video)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UploadVideoActivity.this, "Tải video thành công!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent();
                                    i.setClass(UploadVideoActivity.this, MainActivity.class);
                                    startActivity(i);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(UploadVideoActivity.this, "Lỗi lưu Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        dialog.dismiss();
                        Toast.makeText(UploadVideoActivity.this, "Lỗi Cloudinary: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }


    private void openGalleryForVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            Toast.makeText(this, "Video đã chọn!", Toast.LENGTH_SHORT).show();
        }
    }
}

