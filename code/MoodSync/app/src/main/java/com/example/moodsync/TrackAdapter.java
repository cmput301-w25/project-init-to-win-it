package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<SpotifySearchFragment.Track> tracks;
    private OnTrackSelectedListener listener;

    public interface OnTrackSelectedListener {
        void onTrackSelected(SpotifySearchFragment.Track track);
    }

    public TrackAdapter(List<SpotifySearchFragment.Track> tracks, OnTrackSelectedListener listener) {
        this.tracks = tracks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        SpotifySearchFragment.Track track = tracks.get(position);
        holder.trackTitle.setText(track.name);

        if (track.artists != null && !track.artists.isEmpty()) {
            holder.trackArtist.setText(track.artists.get(0).name);
        }

        // Load album art if available
        if (track.album != null && track.album.images != null && !track.album.images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(track.album.images.get(0).url)
                    .placeholder(R.drawable.album_placeholder)
                    .into(holder.trackImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackSelected(track);
            }
        });

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackSelected(track);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    public void updateTracks(List<SpotifySearchFragment.Track> newTracks) {
        this.tracks = newTracks;
        notifyDataSetChanged();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView trackImage;
        TextView trackTitle;
        TextView trackArtist;
        ImageButton addButton;

        TrackViewHolder(View itemView) {
            super(itemView);
            trackImage = itemView.findViewById(R.id.track_image);
            trackTitle = itemView.findViewById(R.id.track_title);
            trackArtist = itemView.findViewById(R.id.track_artist);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }
}
