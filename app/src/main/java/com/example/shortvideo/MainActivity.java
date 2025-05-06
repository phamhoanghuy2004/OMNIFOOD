package com.example.shortvideo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.shortvideo.adapter.VideoFireBaseAdapter;
import com.example.shortvideo.model.Video1Model;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private VideoFireBaseAdapter videosAdapter;
    private ImageView imgView,imgAddVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgView = findViewById(R.id.imPerson3);
        imgAddVideo = findViewById(R.id.imgAddVideo);

        SessionManager sessionManager = new SessionManager(MainActivity.this);
        Glide.with(MainActivity.this)
                .load(sessionManager.getImg())
                .circleCrop()
                .placeholder(R.drawable.ic_person_pin)
                .into(imgView);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this,ProfileActivity.class);
                startActivity(i);
                finish();
            }
        });

        imgAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this,UploadVideoActivity.class);
                startActivity(i);
                finish();
            }
        });

        viewPager2 = findViewById(R.id.vpager);
        getVideo();
    }

    private void getVideo() {
        // ** set database
        DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference("Video");

        FirebaseRecyclerOptions<Video1Model> options = new FirebaseRecyclerOptions.Builder<Video1Model>()
                .setQuery(mDataBase, Video1Model.class)
                .build();

        // ** set adapter
        videosAdapter = new VideoFireBaseAdapter(options);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(videosAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        videosAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videosAdapter.stopListening();
        if (videosAdapter != null) {
            // Cập nhật các thay đổi trước khi dừng ứng dụng
            videosAdapter.updatePendingLikes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videosAdapter.notifyDataSetChanged();
    }
}