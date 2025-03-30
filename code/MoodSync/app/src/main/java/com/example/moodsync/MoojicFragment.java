package com.example.moodsync;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.FragmentMoojicBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoojicFragment extends Fragment {
    private MediaPlayer mediaPlayer;
    private FragmentMoojicBinding binding;
    private FirebaseFirestore db;

    private List<Song> songsList = new ArrayList<>();
    private SongsAdapter songAdapter;
    private static final String TAG = "MoojicFragment";
    private Song currentSong;
    private boolean isPlaying = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMoojicBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();
        mediaPlayer = new MediaPlayer();

        setupRecyclerView();
        loadSongsFromFirestore();
        setupplay_button();

        return binding.getRoot();
    }
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    private void setupplay_button() {
        // The play button should be in the now playing section, not on individual items
        // Add a play button to your fragment layout first, then:
        binding.playButton.setOnClickListener(v -> {
            if (currentSong != null) {
                if (isPlaying) {
                    pauseSong();
                    binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    resumeSong();
                    binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                }
                isPlaying = !isPlaying;
            } else if (!songsList.isEmpty()) {
                // Play the first song if none is selected
                playSong(songsList.get(0));
                binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                isPlaying = true;
            }
        });
    }


    private void setupRecyclerView() {
        songAdapter = new SongsAdapter(songsList, this::playSong);
        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSongs.setAdapter(songAdapter);
    }

    private void loadSongsFromFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("allsongs")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        songsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Song song = new Song();
                                song.setTitle(document.getString("title"));
                                song.setSinger(document.getString("singer"));
                                song.setUrl(document.getString("url"));

                                // Handle the id field as a Long
                                Object idValue = document.get("id");
                                if (idValue instanceof Long) {
                                    song.setId((Long) idValue);
                                } else if (idValue instanceof Number) {
                                    song.setId(((Number) idValue).longValue());
                                }

                                songsList.add(song);
                                Log.d(TAG, "Song loaded: " + song.getTitle() + " by " + song.getSinger());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing song document: ", e);
                            }
                        }
                        songAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting songs: ", task.getException());
                        Toast.makeText(requireContext(), "Failed to load songs: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void playSong(Song song) {
        try {
            currentSong = song;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(song.getUrl().trim());

            // Set listeners before preparing
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                Log.d("MoojicFragment", "MediaPlayer is prepared, starting playback");
                updateNowPlayingUI(song);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                binding.playButton.setImageResource(android.R.drawable.ic_media_play);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MoojicFragment", "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(requireContext(), "Error playing song: " + what, Toast.LENGTH_SHORT).show();
                isPlaying = false;
                binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                return false;
            });

            // Call prepareAsync only once
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "Error playing song: ", e);
            Toast.makeText(requireContext(), "Error playing song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isPlaying = false;
            binding.playButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void updateNowPlayingUI(Song song) {
        // Make sure you're using the correct view ID from your binding
        binding.titleTextView.setText(getString(R.string.now_playing, song.getTitle(), song.getSinger()));
        binding.titleTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isPlaying) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public void playExternalSong(Song song) {
        playSong(song);
    }
}