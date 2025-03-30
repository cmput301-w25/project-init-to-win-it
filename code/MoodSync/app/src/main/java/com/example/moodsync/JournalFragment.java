package com.example.moodsync;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moodsync.databinding.JournalFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JournalFragment extends Fragment {

    private JournalFragmentBinding binding;
    private RecyclerView journalRecyclerView;
    private MoodCardAdapter moodCardAdapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private ImageView pfp;
    private TextView emptyStateView;
    public LocalStorage globalStorage = LocalStorage.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = JournalFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize RecyclerView with the correct ID from the XML
        journalRecyclerView = binding.moodRecyclerView;
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an empty state view programmatically since it's not in the XML
        emptyStateView = new TextView(getContext());
        emptyStateView.setText("No private mood events found");
        emptyStateView.setTextSize(18);
        emptyStateView.setPadding(32, 32, 32, 32);
        emptyStateView.setGravity(android.view.Gravity.CENTER);
        emptyStateView.setTextColor(getResources().getColor(android.R.color.white));

        // Add the empty state view to the layout
        ViewGroup parentView = (ViewGroup) journalRecyclerView.getParent();
        parentView.addView(emptyStateView);
        emptyStateView.setVisibility(View.GONE);

        // Get the current user ID
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch profile image if user ID is available
        if (globalStorage.getCurrentUserId() != null) {
            fetchProfileImageUrl(globalStorage.getCurrentUserId());
        }

        // Fetch private moods for the current user
        fetchPrivateMoods();

        // Handle navigation button clicks
        handleNavigationButtonClicked(view);
        binding.diaryButton.setTextColor(getResources().getColor(R.color.green));
        binding.diaryButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        return view;
    }

    private void fetchPrivateMoods() {
        db.collection("mood_events")
                .whereEqualTo("id", currentUserId)
                .whereEqualTo("public", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> moodEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvents.add(moodEvent);
                        }

                        // Update the adapter with the fetched mood events
                        updateMoodAdapter(moodEvents);

                        // Show empty state if no mood events
                        if (moodEvents.isEmpty()) {
                            emptyStateView.setVisibility(View.VISIBLE);
                            journalRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateView.setVisibility(View.GONE);
                            journalRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("Firestore", "Error fetching mood events", task.getException());
                        // Show empty state on error
                        emptyStateView.setVisibility(View.VISIBLE);
                        journalRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void updateMoodAdapter(List<MoodEvent> moodEvents) {
        moodCardAdapter = new MoodCardAdapter(moodEvents);
        journalRecyclerView.setAdapter(moodCardAdapter);
    }

    private void fetchProfileImageUrl(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty() && pfp != null) {
                            loadProfileImage(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching profile image URL", e);
                });
    }

    private void loadProfileImage(String imageUrl) {
        if (pfp != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(pfp);
        } else {
            Log.e("JournalFragment", "Profile image view is null");
        }
    }

    private void handleNavigationButtonClicked(View view) {
        view.findViewById(R.id.home_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_SecondFragment);
        });

        view.findViewById(R.id.map_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_mapsActivity);
        });

        view.findViewById(R.id.add_circle_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_addMoodActivityFragment);
        });

        view.findViewById(R.id.diary_button).setOnClickListener(v -> {
            // Already on JournalFragment, do nothing
        });

        view.findViewById(R.id.history_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_moodHistoryFragment);
        });
    }
}
