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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A {@link Fragment} that displays the user's mood history.
 */
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

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
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

    /**
     * Fetches a specific mood event from Firestore and navigates to the edit screen.
     * @param selectedItem The selected MoodHistoryItem.
     */
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

    /**
     * Fetches mood events from Firestore and updates the RecyclerView.
     */
    private void fetchMoodEvents() {
        db.collection("mood_events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Fetch task successful");
                        moodHistoryItems.clear();
                        Log.d(TAG, "Number of documents: " + task.getResult().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Processing document: " + document.getId());
                            String id = document.getId();
                            String mood = document.getString("mood");
                            String description = document.getString("description");
                            Long datems = document.getLong("date");

                            if (mood == null || description == null || datems == null) {
                                Log.w(TAG, "Missing data in document: " + id);
                                continue;
                            }

                            Date date = new Date(datems);
                            String emoji = getEmojiForMood(mood);

                            MoodHistoryItem item = new MoodHistoryItem(mood, emoji, description, date);
                            item.setId(id);
                            moodHistoryItems.add(item);
                            Log.d(TAG, "Added item: " + item.toString());
                        }

                        Collections.sort(moodHistoryItems, (item1, item2) -> item2.getDate().compareTo(item1.getDate()));
                        Log.d(TAG, "Sorted items. First item date: " +
                                (moodHistoryItems.isEmpty() ? "N/A" : moodHistoryItems.get(0).getDate()));

                        Log.d(TAG, "Number of items fetched: " + moodHistoryItems.size());
                        moodHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }


    /**
     * Gets the appropriate emoji for a given mood.
     * @param mood The mood string.
     * @return The corresponding emoji.
     */
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
