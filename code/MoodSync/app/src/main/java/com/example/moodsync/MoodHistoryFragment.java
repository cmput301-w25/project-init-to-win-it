package com.example.moodsync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.MoodHistoryFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryFragment extends Fragment {

    private MoodHistoryFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodHistoryItem> moodHistoryItems = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "MoodHistoryFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MoodHistoryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moodHistoryAdapter = new MoodHistoryAdapter(moodHistoryItems);
        moodRecyclerView.setAdapter(moodHistoryAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore
        fetchMoodEvents();

        binding.addButton.setOnClickListener(v ->
                NavHostFragment.findNavController(MoodHistoryFragment.this)
                        .navigate(R.id.action_moodHistoryFragment_to_addMoodActivityFragment)
        );
    }

    private void fetchMoodEvents() {
        db.collection("mood_events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodHistoryItems.clear(); // Clear the previous list

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract data from the document
                            String mood = document.getString("mood");
                            String description = document.getString("description");

                            // Determine emoji based on mood (you might want a more robust mapping)
                            String emoji = getEmojiForMood(mood);

                            // Create MoodHistoryItem
                            MoodHistoryItem item = new MoodHistoryItem(mood, emoji, description);
                            moodHistoryItems.add(item);
                        }

                        moodHistoryAdapter.notifyDataSetChanged(); // Notify the adapter about the new data
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private String getEmojiForMood(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return "😊";
            case "sad":
                return "😢";
            case "excited":
                return "😃";
            case "angry":
                return "😠";
            case "confused":
                return "😕";
            case "surprised":
                return "😲";
            case "ashamed":
                return "😳";
            case "scared":
                return "😨";
            case "disgusted":
                return "🤢";
            default:
                return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}