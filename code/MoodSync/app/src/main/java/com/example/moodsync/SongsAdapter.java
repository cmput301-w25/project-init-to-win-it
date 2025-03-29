package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// RecyclerView Adapter for Songs
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {
    private List<Song> songsList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongsAdapter(List<Song> songsList, OnSongClickListener listener) {
        this.songsList = songsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_item, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songsList.get(position);
        holder.titleTextView.setText(song.getTitle());
        holder.singerTextView.setText(song.getSinger());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView singerTextView;

        public SongViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.song_title);
            singerTextView = view.findViewById(R.id.song_singer);
        }
    }
}
