package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.AddmoodfragmentBinding;
import com.example.moodsync.databinding.FragmentFirstBinding;

public class AddMoodActivity extends Fragment {
    private AddmoodfragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState 
    ) {
        binding = AddmoodfragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set click listener for the button to go back to SecondFragment
        binding.cancel.setOnClickListener(v ->
                NavHostFragment.findNavController(AddMoodActivity.this)
                        .navigate(R.id.action_addMoodActivityFragment_to_SecondFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
