package com.example.moodsync;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.AddmoodfragmentBinding;
import com.example.moodsync.databinding.Addmoodfragment2Binding;

public class AddMoodActivity extends Fragment {
    private AddmoodfragmentBinding binding1;
    private Addmoodfragment2Binding binding2;
    private boolean isSecondLayout = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (getArguments() != null && getArguments().getBoolean("isSecondLayout", false)) {
            isSecondLayout = true;
            binding2 = Addmoodfragment2Binding.inflate(inflater, container, false);
            return binding2.getRoot();
        } else {
            binding1 = AddmoodfragmentBinding.inflate(inflater, container, false);
            return binding1.getRoot();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isSecondLayout) {
            setupSecondLayout();
        } else {
            setupFirstLayout();
        }
    }

    private void setupFirstLayout() {
        // set click listener for the cancel button to go back to SecondFragment
        binding1.cancel.setOnClickListener(v ->
                NavHostFragment.findNavController(AddMoodActivity.this)
                        .navigate(R.id.action_addMoodActivityFragment_to_SecondFragment));

        // set click listener for the next button to go to addmoodfragment2
        binding1.next.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isSecondLayout", true);
            NavHostFragment.findNavController(AddMoodActivity.this)
                    .navigate(R.id.action_addMoodActivityFragment_to_addMoodActivityFragment2, args);
        });
    }

    private void setupSecondLayout() {
        // Setup listeners for the second layout
        binding2.backbutton.setOnClickListener(v ->
                NavHostFragment.findNavController(AddMoodActivity.this)
                        .navigateUp());

        binding2.createmood.setOnClickListener(v -> showSuccessDialog());
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        View customView = getLayoutInflater().inflate(R.layout.custom_success_dialog, null);

        builder.setView(customView)
                .setTitle("Upload Success")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        NavHostFragment.findNavController(AddMoodActivity.this)
                                .navigate(R.id.action_addMoodActivityFragment2_to_SecondFragment);
                    }, 2000);
                });

        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupDialogAnimation;
        dialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}
