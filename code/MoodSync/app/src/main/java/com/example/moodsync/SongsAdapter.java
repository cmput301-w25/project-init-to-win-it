package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A RecyclerView adapter for displaying a list of songs. Each item in the list
 * shows the song title and singer. The adapter also provides click handling
 * for individual song items via a listener interface.
 *
 * <p>
 * This adapter uses a custom layout for each song item and binds the data
 * from a {@link Song} object to the corresponding views in the layout.
 *
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {
    private List<Song> songsList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    /**
     * Constructs a new SongsAdapter with the specified list of songs and a click listener.
     *
     * @param songsList The list of {@link Song} objects to display in the RecyclerView.
     * @param listener  An implementation of {@link OnSongClickListener} to handle item clicks.
     */
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

    /**
     * A ViewHolder class that holds references to views for each individual song item in the RecyclerView.
     *
     * <p>
     * This class is responsible for caching view references (e.g., TextViews for title and singer)
     * to improve performance by avoiding repeated calls to {@code findViewById}.
     * </p>
     */
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
