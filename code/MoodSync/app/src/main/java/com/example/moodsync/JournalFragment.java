package com.example.moodsync;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moodsync.MoodCardAdapter;
import com.example.moodsync.MoodEvent;
import com.example.moodsync.MyApplication;
import com.example.moodsync.R;
import com.example.moodsync.databinding.JournalFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JournalFragment extends Fragment {

    private JournalFragmentBinding binding;
    private RecyclerView journalRecyclerView;
    private JournalCardAdapter moodCardAdapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private ImageView pfp;
    public LocalStorage globalStorage = LocalStorage.getInstance();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = JournalFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        pfp = binding.profilePic;
        journalRecyclerView = binding.moodRecyclerView;
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the current user ID
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();
        fetchProfileImageUrl(globalStorage.getCurrentUserId());
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch private moods for the current user
        fetchPrivateMoods();

        // Handle navigation button clicks
        handleNavigationButtonClicked(view);
        binding.diaryButton.setTextColor(getResources().getColor(R.color.green));
        binding.diaryButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        return view;
    }

    private void fetchPrivateMoods() {
        updateMoodAdapter(globalStorage.getPrivList());
    }

    private void updateMoodAdapter(List<MoodEvent> moodEvents) {
        moodCardAdapter = new JournalCardAdapter(moodEvents);
        journalRecyclerView.setAdapter(moodCardAdapter);
    }
    private void fetchProfileImageUrl(String userId) {
        loadProfileImage(globalStorage.getUserFromUName(userId).getPfpUrl());
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(pfp);
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
