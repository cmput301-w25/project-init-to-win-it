package com.example.moodsync;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.AddmoodfragmentBinding;
import com.example.moodsync.databinding.Addmoodfragment2Binding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AddMoodActivity extends Fragment {
    private AddmoodfragmentBinding binding1;
    private Addmoodfragment2Binding binding2;
    private boolean isSecondLayout = false;

    // Firebase Database reference
    private DatabaseReference database;

    // Data to be passed between fragments
    private String moodDescription;
    private String moodIntensity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance().getReference("mood_events");

        if (isSecondLayout) {
            if (getArguments() != null) {
                moodDescription = getArguments().getString("description", "");
                moodIntensity = getArguments().getString("intensity", "");
            }
            setupSecondLayout();
        } else {
            setupFirstLayout();
        }
    }

    private void setupFirstLayout() {
        // Get UI elements from binding
        EditText moodDescriptionInput = binding1.editDescription;
        Spinner moodIntensitySpinner = binding1.mainCard;

        binding1.cancel.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_addMoodActivityFragment_to_SecondFragment));

        binding1.next.setOnClickListener(v -> {
            // Get values from user input
            String description = moodDescriptionInput.getText().toString();
            String intensity = moodIntensitySpinner.getSelectedItem().toString();

            // Pass data to second fragment
            Bundle args = new Bundle();
            args.putBoolean("isSecondLayout", true);
            args.putString("description", description);
            args.putString("intensity", intensity);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_addMoodActivityFragment_to_addMoodActivityFragment2, args);
        });
    }

    private void setupSecondLayout() {
        binding2.backbutton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding2.createmood.setOnClickListener(v -> {
            // Send the mood event to Firebase
            MoodEvent moodEvent = new MoodEvent(moodIntensity, moodDescription);
            addMoodEventToFirebase(moodEvent);
        });
    }

    private void addMoodEventToFirebase(MoodEvent moodEvent) {
        String uniqueID = UUID.randomUUID().toString();

        database.child(uniqueID).setValue(moodEvent)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(getContext(), "Failed to create mood event.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded);
        View customView = getLayoutInflater().inflate(R.layout.custom_success_dialog, null);

        builder.setView(customView)
                .setTitle("Upload Success")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_addMoodActivityFragment2_to_SecondFragment);
                    }, 2000);
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(Color.WHITE);
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}