package com.example.shortvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.R;
import com.example.shortvideo.model.Video1Model;

import java.util.List;

public class MyVideoAdapter extends RecyclerView.Adapter<MyVideoAdapter.MyViewHolder> {
    private List<Video1Model> videoList;

    public MyVideoAdapter(List<Video1Model> videoList) {
        this.videoList = videoList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtVideoTitle, txtLikeCount, txtDislikeCount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVideoTitle = itemView.findViewById(R.id.txtVideoTitle);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtDislikeCount = itemView.findViewById(R.id.txtDislikeCount);
        }

        public void bind(Video1Model video) {
            txtVideoTitle.setText(video.getTitle());
            txtLikeCount.setText("👍 " + video.getLike());
            txtDislikeCount.setText("👎 " + video.getDislike());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_video, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(videoList.get(position));
    }

    @Override
    public int getItemCount() {
        return (videoList != null ? videoList.size() : 0);
    }
}
