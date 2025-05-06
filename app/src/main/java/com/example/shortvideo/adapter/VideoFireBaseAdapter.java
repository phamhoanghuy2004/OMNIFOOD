package com.example.shortvideo.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shortvideo.R;
import com.example.shortvideo.SessionManager;
import com.example.shortvideo.model.Video1Model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class VideoFireBaseAdapter  extends FirebaseRecyclerAdapter<Video1Model,VideoFireBaseAdapter.MyHolder > {

    public VideoFireBaseAdapter(@NonNull FirebaseRecyclerOptions<Video1Model> options) {
        super(options);
    }

    private final Map<String, Video1Model> tempChanges = new HashMap<>();
    public void updatePendingLikes() {
        // Cập nhật tất cả các thay đổi đang chờ vào Firebase
        for (Map.Entry<String, Video1Model> entry : tempChanges.entrySet()) {
            String key = entry.getKey();
            Video1Model model = entry.getValue();

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("like", model.getLike());
            updateMap.put("dislike", model.getDislike());
            updateMap.put("likedBy", model.getLikedBy());

            FirebaseDatabase.getInstance().getReference("Video")
                    .child(key)
                    .updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> Log.d("Update", "Cập nhật thành công"))
                    .addOnFailureListener(e -> Log.e("Update", "Lỗi cập nhật", e));
        }

        tempChanges.clear(); // Xóa sau khi cập nhật
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_video_row, parent, false);
        return new MyHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull Video1Model model) {
        String keyVideo = getRef(position).getKey();
        holder.videoKey = keyVideo;

        holder.textVideoTitle.setText(model.getTitle());
        holder.textVideoDescription.setText(model.getDesc());
        holder.videoView.setVideoURI(Uri.parse(model.getUrl()));
        holder.likeCount.setText(String.valueOf(model.getLike()));
        holder.dislikeCount.setText(String.valueOf(model.getDislike()));

        // Load avatar & email của người đăng video
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(model.getIdUser());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Glide.with(holder.itemView.getContext())
                            .load(snapshot.child("uriImg").getValue(String.class))
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_pin)
                            .into(holder.imgUser);
                    holder.emailUser.setText(snapshot.child("email").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi load user", error.toException());
            }
        });

        // Lấy thông tin người dùng hiện tại
        Context context = holder.itemView.getContext();
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();

        // Xử lý likedBy
        Map<String, Boolean> likedBy = model.getLikedBy();
        if (likedBy == null) likedBy = new HashMap<>();
        final Map<String, Boolean> finalLikedBy = likedBy;
        model.setLikedBy(finalLikedBy);

        Boolean isLiked = finalLikedBy.get(userId);
        if (isLiked != null) {
            if (isLiked) {
                holder.favorites.setImageResource(R.drawable.ic_fill_favorite);
                holder.dislike.setImageResource(R.drawable.ic_dislike);
            } else {
                holder.favorites.setImageResource(R.drawable.ic_favorite);
                holder.dislike.setImageResource(R.drawable.ic_fill_dislike);
            }
        } else {
            holder.favorites.setImageResource(R.drawable.ic_favorite);
            holder.dislike.setImageResource(R.drawable.ic_dislike);
        }

        // Video auto play
        holder.videoView.setOnPreparedListener(mp -> {
            holder.videoProgressBar.setVisibility(View.GONE);
            mp.start();

            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
            float scale = videoRatio / screenRatio;
            if (scale >= 1f) {
                holder.videoView.setScaleX(scale);
            } else {
                holder.videoView.setScaleY(1f / scale);
            }
        });

        holder.videoView.setOnCompletionListener(mp -> mp.start());

        // Like/Dislike
        holder.favorites.setOnClickListener(v -> {
            if (userId != null) {
                Boolean liked = finalLikedBy.get(userId);
                if (liked != null && liked) {
                    finalLikedBy.put(userId, false);
                    holder.favorites.setImageResource(R.drawable.ic_favorite);
                    holder.dislike.setImageResource(R.drawable.ic_fill_dislike);
                    if (model.getLike() > 0) model.setLike(model.getLike() - 1);
                    model.setDislike(model.getDislike() + 1);
                } else {
                    finalLikedBy.put(userId, true);
                    holder.favorites.setImageResource(R.drawable.ic_fill_favorite);
                    holder.dislike.setImageResource(R.drawable.ic_dislike);
                    model.setLike(model.getLike() + 1);
                    if (model.getDislike() > 0) model.setDislike(model.getDislike() - 1);
                }
                model.setLikedBy(finalLikedBy);
                holder.likeCount.setText(String.valueOf(model.getLike()));
                holder.dislikeCount.setText(String.valueOf(model.getDislike()));
                tempChanges.put(keyVideo, model);
            }
        });

        // Gỡ like/dislike
        holder.favorites.setOnLongClickListener(v -> {
            if (userId != null && finalLikedBy.containsKey(userId)) {
                boolean status = finalLikedBy.get(userId);
                if (status && model.getLike() > 0) model.setLike(model.getLike() - 1);
                else if (!status && model.getDislike() > 0) model.setDislike(model.getDislike() - 1);
                finalLikedBy.remove(userId);

                holder.favorites.setImageResource(R.drawable.ic_favorite);
                holder.dislike.setImageResource(R.drawable.ic_dislike);
                holder.likeCount.setText(String.valueOf(model.getLike()));
                holder.dislikeCount.setText(String.valueOf(model.getDislike()));

                model.setLikedBy(finalLikedBy);
                tempChanges.put(keyVideo, model);
            }
            return true;
        });
    }


    public class MyHolder extends RecyclerView.ViewHolder {

        // Video
        public VideoView videoView;
        public ProgressBar videoProgressBar;

        // Thông tin video
        public TextView textVideoTitle;
        public TextView textVideoDescription;

        // Thông tin người dùng
        public ImageView imgUser;
        public TextView emailUser;

        // Tương tác
        public ImageView favorites;
        public ImageView dislike;
        public TextView likeCount;
        public TextView dislikeCount;

        // Hành động khác
        public ImageView imShare;
        public ImageView imMore;

        // Key của video
        public String videoKey;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Khởi tạo view từ layout
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);

            imgUser = itemView.findViewById(R.id.imgUser);
            emailUser = itemView.findViewById(R.id.emailUser);

            favorites = itemView.findViewById(R.id.favorites);
            dislike = itemView.findViewById(R.id.dislike);
            likeCount = itemView.findViewById(R.id.likeCount);
            dislikeCount = itemView.findViewById(R.id.dislikeCount);

            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
        }
    }


}
