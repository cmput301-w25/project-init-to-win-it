package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.HomePageFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private HomePageFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodCardAdapter moodCardAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = HomePageFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        // Set up navigation actions
        binding.addCircleButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_addMoodActivityFragment)
        );

        binding.historyButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_moodHistoryFragment)
        );
    }

    private void setupRecyclerView() {
        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodCardAdapter = new MoodCardAdapter(getMockMoodData());
        moodRecyclerView.setAdapter(moodCardAdapter);
    }

    private List<MoodEvent> getMockMoodData() {
        List<MoodEvent> moodEvents = new ArrayList<>();
        moodEvents.add(new MoodEvent("Happy", "Birthday Party", "Had a great time celebrating!", "Friends"));
        moodEvents.add(new MoodEvent("Excited", "Promotion", "Got promoted at work!", "Colleagues"));
        moodEvents.add(new MoodEvent("Relaxed", "Vacation", "Enjoyed the beach and sunshine.", "Family"));
        return moodEvents;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
