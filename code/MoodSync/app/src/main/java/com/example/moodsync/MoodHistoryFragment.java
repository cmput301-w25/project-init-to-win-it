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

import com.example.moodsync.databinding.MoodHistoryFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryFragment extends Fragment {

    private MoodHistoryFragmentBinding binding;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodHistoryItem> moodHistoryItems = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "MoodHistoryFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MoodHistoryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        binding.moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryAdapter = new MoodHistoryAdapter(moodHistoryItems, getContext());
        binding.moodRecyclerView.setAdapter(moodHistoryAdapter);

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
                            String id = document.getId(); // Get document ID
                            String mood = document.getString("mood");
                            String description = document.getString("description");

                            // Determine emoji based on mood
                            String emoji = getEmojiForMood(mood);

                            // Create MoodHistoryItem and set its ID
                            MoodHistoryItem item = new MoodHistoryItem(mood, emoji, description);
                            item.setId(id); // Set the Firestore document ID
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
        binding = null; // Prevent memory leaks
    }
}
