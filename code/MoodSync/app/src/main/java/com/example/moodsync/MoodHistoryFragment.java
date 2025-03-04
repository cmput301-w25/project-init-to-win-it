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

import com.example.moodsync.databinding.MoodHistoryFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryFragment extends Fragment {

    private MoodHistoryFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodHistoryItem> moodHistoryItems = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = MoodHistoryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data (replace with your actual data source)
        moodHistoryItems.add(new MoodHistoryItem("Happy", "😊"));
        moodHistoryItems.add(new MoodHistoryItem("Sad", "😢"));
        moodHistoryItems.add(new MoodHistoryItem("Excited", "😃"));
        moodHistoryItems.add(new MoodHistoryItem("Angry", "😠"));

        moodHistoryAdapter = new MoodHistoryAdapter(moodHistoryItems);
        moodRecyclerView.setAdapter(moodHistoryAdapter);

        binding.addMoodButton.setOnClickListener(v ->
                NavHostFragment.findNavController(MoodHistoryFragment.this)
                        .navigate(R.id.action_moodHistoryFragment_to_addMoodActivityFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
