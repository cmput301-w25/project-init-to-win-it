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
/**
 * Fragment for displaying a journal view of private mood events.
 * Includes a RecyclerView for mood events, handles navigation, and displays the user's profile picture.
 * Interacts with LocalStorage for local data and Firebase Firestore for backend operations.
 */
public class JournalFragment extends Fragment {

    private JournalFragmentBinding binding;
    private RecyclerView journalRecyclerView;
    private MoodCardAdapter moodCardAdapter;
    FirebaseFirestore db;
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
        fetchProfileImageUrl(globalStorage.getCurrentUserId());

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



    /**
     * Displays or hides the empty state view depending on whether the RecyclerView has data.
     *
     * @param show True to show the empty state view, false to hide it.
     */
    private void showEmptyState(boolean show) {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (journalRecyclerView != null) {
            journalRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Fetches private moods for the current user from local storage and updates the RecyclerView.
     */
    private void fetchPrivateMoods() {
        List<MoodEvent> privateMoods = globalStorage.getPrivList();

        // Add null/empty checks
        if (privateMoods == null || privateMoods.isEmpty()) {
            showEmptyState(true);
            updateMoodAdapter(new ArrayList<>()); // Pass empty list
        } else {
            showEmptyState(false);
            updateMoodAdapter(privateMoods);
        }
    }

    /**
     * Updates the RecyclerView adapter with a new list of mood events.
     *
     * @param moodEvents The list of MoodEvent objects to display in the RecyclerView.
     */
    private void updateMoodAdapter(List<MoodEvent> moodEvents) {
        if (moodEvents == null) {
            moodEvents = new ArrayList<>();
        }

        if (moodCardAdapter == null) {
            moodCardAdapter = new MoodCardAdapter(moodEvents);
            journalRecyclerView.setAdapter(moodCardAdapter);
        } else {
            moodCardAdapter.updateMoodEvents(moodEvents); // Ensure your adapter has this method
        }
    }

    /**
     * Fetches and loads the profile image URL for a given user ID.
     *
     * @param userId The ID of the user whose profile image URL needs to be fetched.
     */
    private void fetchProfileImageUrl(String userId) {
        loadProfileImage(globalStorage.getUserFromUName(userId).getPfpUrl());
    }

    /**
     * Loads a profile image into an ImageView using Glide.
     *
     * @param imageUrl The URL of the profile image to load.
     */
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

    /**
     * Handles navigation button clicks and sets up navigation actions for various buttons in the layout.
     *
     * @param view The root view of the fragment where navigation buttons are located.
     */
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

        view.findViewById(R.id.history_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_moodHistoryFragment);
        });
        view.findViewById(R.id.diary_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_JournalFragment_to_JournalFragment);
        });
    }
}
