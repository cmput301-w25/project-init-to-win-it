package com.example.moodsync;

import android.os.Bundle;
import android.os.Parcelable;
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

        // Set up RecyclerView
        binding.moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryAdapter = new MoodHistoryAdapter(moodHistoryItems, getContext());
        binding.moodRecyclerView.setAdapter(moodHistoryAdapter);

        db = FirebaseFirestore.getInstance();

        moodHistoryAdapter.setOnItemClickListener(item -> {
            fetchMoodEventAndNavigate(item);
        });


        fetchMoodEvents();

        binding.addButton.setOnClickListener(v ->
                NavHostFragment.findNavController(MoodHistoryFragment.this)
                        .navigate(R.id.action_moodHistoryFragment_to_SecondFragment)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMoodEvents();
    }

    private void fetchMoodEventAndNavigate(MoodHistoryItem selectedItem) {
        db.collection("mood_events")
                .whereEqualTo("description", selectedItem.getDescription())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvent.setId(document.getId()); // Store the document ID in MoodEvent

                            Bundle args = new Bundle();
                            args.putParcelable("moodEvent", (Parcelable) moodEvent);

                            NavHostFragment.findNavController(MoodHistoryFragment.this)
                                    .navigate(R.id.action_moodHistoryFragment_to_editMoodFragment, args);
                            break; // Assuming only one MoodEvent will match the description
                        }
                    } else {
                        Log.d(TAG, "Error getting MoodEvent: ", task.getException());
                    }
                });
    }

    private void fetchMoodEvents() {
        db.collection("mood_events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Fetch task successful");
                        moodHistoryItems.clear(); // Clear the previous list

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Document ID: " + document.getId()); // Log document ID
                            String id = document.getId(); // Get document ID
                            String mood = document.getString("mood");
                            String description = document.getString("description");

                            // Determine emoji based on mood (you might want a more robust mapping)
                            String emoji = getEmojiForMood(mood);

                            // Create MoodHistoryItem
                            MoodHistoryItem item = new MoodHistoryItem(mood, emoji, description);
                            item.setId(id); // Set the Firestore document ID
                            moodHistoryItems.add(item);
                        }

                        Log.d(TAG, "Number of items fetched: " + moodHistoryItems.size()); // Log item count
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
