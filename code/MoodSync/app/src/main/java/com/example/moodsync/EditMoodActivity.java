package com.example.moodsync;

import android.animation.ObjectAnimator;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.EditMoodFragmentBinding;
import com.example.moodsync.databinding.EditMoodFragment2Binding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMoodActivity extends Fragment {
    private String moodDescription;
    private String selectedMood;
    private EditMoodFragmentBinding binding1;
    private EditMoodFragment2Binding binding2;
    private boolean isSecondLayout = false;
    private RelativeLayout mainLayout;

    private final Map<String, Integer> moodGradients = new HashMap<>();

    private ImageView happyImage, sadImage, angryImage, confusedImage, surprisedImage, ashamedImage, scaredImage, disgustedImage;
    private ImageView lastSelectedImageView = null;
    private Button selectedSocialSituationButton = null;

    private static final int ANIMATION_DURATION = 300; // Animation duration in milliseconds

    private FirebaseFirestore db;
    private CollectionReference moodEventsRef;
    private MoodEvent moodEventToEdit;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();
        moodEventsRef = db.collection("mood_events");

        if (getArguments() != null && getArguments().getBoolean("isSecondLayout", false)) {
            isSecondLayout = true;
            binding2 = EditMoodFragment2Binding.inflate(inflater, container, false);
            return binding2.getRoot();
        } else {
            binding1 = EditMoodFragmentBinding.inflate(inflater, container, false);
            return binding1.getRoot();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        moodGradients.put("Happy", R.drawable.happy_gradient);
        moodGradients.put("Sad", R.drawable.sad_gradient);
        moodGradients.put("Angry", R.drawable.angry_gradient);
        moodGradients.put("Confused", R.drawable.confused_gradient);
        moodGradients.put("Surprised", R.drawable.surprised_gradient);
        moodGradients.put("Ashamed", R.drawable.ashamed_gradient);
        moodGradients.put("Scared", R.drawable.scared_gradient);
        moodGradients.put("Disgusted", R.drawable.disgusted_gradient);

        if (getArguments() != null) {
            moodEventToEdit = getArguments().getParcelable("moodEvent");
        }

        if (moodEventToEdit == null) {
            // Handle error:  Maybe navigate back or show an error message.
            Toast.makeText(requireContext(), "Error: No mood event to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSecondLayout) {
            setupSecondLayout();
        } else {
            setupFirstLayout(view);
        }
    }

    private void setupFirstLayout(View view) {
        mainLayout = view.findViewById(R.id.main_layout);

        happyImage = view.findViewById(R.id.happy_image);
        sadImage = view.findViewById(R.id.sad_image);
        angryImage = view.findViewById(R.id.angry_image);
        confusedImage = view.findViewById(R.id.confused_image);
        surprisedImage = view.findViewById(R.id.surprised_image);
        ashamedImage = view.findViewById(R.id.ashamed_image);
        scaredImage = view.findViewById(R.id.scared_image);
        disgustedImage = view.findViewById(R.id.disgusted_image);


        happyImage.setOnClickListener(v -> selectMood("Happy", happyImage));
        sadImage.setOnClickListener(v -> selectMood("Sad", sadImage));
        angryImage.setOnClickListener(v -> selectMood("Angry", angryImage));
        confusedImage.setOnClickListener(v -> selectMood("Confused", confusedImage));
        surprisedImage.setOnClickListener(v -> selectMood("Surprised", surprisedImage));
        ashamedImage.setOnClickListener(v -> selectMood("Ashamed", ashamedImage));
        scaredImage.setOnClickListener(v -> selectMood("Scared", scaredImage));
        disgustedImage.setOnClickListener(v -> selectMood("Disgusted", disgustedImage));

        Spinner moodSpinner = binding1.mainCard;
        EditText descriptionInput = binding1.editDescription;

        String moodToSelect = moodEventToEdit.getMood();
        if (moodToSelect != null && !moodToSelect.isEmpty()) {
            int spinnerPosition = getPositionOfValue(moodSpinner, moodToSelect);
            if (spinnerPosition != -1) {
                moodSpinner.setSelection(spinnerPosition);
            }
        }

        descriptionInput.setText(moodEventToEdit.getDescription());

        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = parent.getItemAtPosition(position).toString();
                updateBackgroundColor(selectedMood);
                Toast.makeText(getContext(), "Selected: " + selectedMood, Toast.LENGTH_SHORT).show();

                switch (selectedMood) {
                    case "Happy":
                        selectMood("Happy", happyImage);
                        break;
                    case "Sad":
                        selectMood("Sad", sadImage);
                        break;
                    case "Angry":
                        selectMood("Angry", angryImage);
                        break;
                    case "Confused":
                        selectMood("Confused", confusedImage);
                        break;
                    case "Surprised":
                        selectMood("Surprised", surprisedImage);
                        break;
                    case "Ashamed":
                        selectMood("Ashamed", ashamedImage);
                        break;
                    case "Scared":
                        selectMood("Scared", scaredImage);
                        break;
                    case "Disgusted":
                        selectMood("Disgusted", disgustedImage);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        binding1.cancel.setOnClickListener(v -> NavHostFragment.findNavController(EditMoodActivity.this)
                .navigate(R.id.action_editMoodActivityFragment_to_moodHistoryFragment));

        // In setupFirstLayout()
        binding1.next.setOnClickListener(v -> {
            // Get current values from UI components
            this.selectedMood = binding1.mainCard.getSelectedItem().toString();
            this.moodDescription = binding1.editDescription.getText().toString();

            Bundle args = new Bundle();
            args.putBoolean("isSecondLayout", true);
            args.putString("selectedMood", this.selectedMood);
            args.putString("description", this.moodDescription);
            args.putParcelable("moodEvent", (Parcelable) moodEventToEdit); //Pass the object to the fragment.

            NavHostFragment.findNavController(EditMoodActivity.this)
                    .navigate(R.id.action_editMoodActivityFragment_to_editMoodActivityFragment2, args);
        });

    }

    private void resetAllEmojis() {
        happyImage.setBackgroundResource(android.R.color.transparent);
        sadImage.setBackgroundResource(android.R.color.transparent);
        angryImage.setBackgroundResource(android.R.color.transparent);
        confusedImage.setBackgroundResource(android.R.color.transparent);
        surprisedImage.setBackgroundResource(android.R.color.transparent);
        ashamedImage.setBackgroundResource(android.R.color.transparent);
        scaredImage.setBackgroundResource(android.R.color.transparent);
        disgustedImage.setBackgroundResource(android.R.color.transparent);
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }


    private void setupSecondLayout() {
        Log.d("LIFECYCLE", "setupSecondLayout called");
        if (getArguments() != null) {
            this.selectedMood = getArguments().getString("selectedMood", "");
            this.moodDescription = getArguments().getString("description", "");
            moodEventToEdit = getArguments().getParcelable("moodEvent"); //Get moodEvent from parameters
        }

        EditText triggerInput = binding2.triggerInput;
        triggerInput.setText(moodEventToEdit.getTrigger());

        String socialSituation = moodEventToEdit.getSocialSituation();
        if (socialSituation != null && !socialSituation.isEmpty()) {
            if (socialSituation.equals(binding2.ss1.getText().toString())) {
                selectSocialSituation(binding2.ss1);
            } else if (socialSituation.equals(binding2.ss2.getText().toString())) {
                selectSocialSituation(binding2.ss2);
            } else if (socialSituation.equals(binding2.ss3.getText().toString())) {
                selectSocialSituation(binding2.ss3);
            } else if (socialSituation.equals(binding2.ss4.getText().toString())) {
                selectSocialSituation(binding2.ss4);
            }
        }

        binding2.editmood.setOnClickListener(v -> {

            String trigger = triggerInput.getText().toString();

            String socialSituation1 = (selectedSocialSituationButton != null) ?
                    selectedSocialSituationButton.getText().toString() : "None";

            // Get current timestamp
            long currentTimestamp = System.currentTimeMillis();



            MoodEvent moodEvent = new MoodEvent(
                    this.selectedMood,
                    trigger,
                    this.moodDescription,
                    socialSituation,
                    currentTimestamp // Pass the timestamp to the MoodEvent
            );

            Log.d("FIREBASE", "Saving: " + moodEvent);

            updateMoodEvent(moodEvent);

            showSuccessDialogUI();
        });


        binding2.backbutton.setOnClickListener(v -> NavHostFragment.findNavController(EditMoodActivity.this)
                .navigateUp());

        binding2.ss1.setOnClickListener(v -> selectSocialSituation(binding2.ss1));
        binding2.ss2.setOnClickListener(v -> selectSocialSituation(binding2.ss2));
        binding2.ss3.setOnClickListener(v -> selectSocialSituation(binding2.ss3));
        binding2.ss4.setOnClickListener(v -> selectSocialSituation(binding2.ss4));
    }


    private void selectSocialSituation(Button button) {
        if (selectedSocialSituationButton != null) {
            animateButtonDeselection(selectedSocialSituationButton);
        }

        animateButtonSelection(button);
        selectedSocialSituationButton = button;
    }


    private void animateButtonSelection(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_selected));
    }

    private void animateButtonDeselection(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.1f, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
    }


    private void refreshMoodEventsList() {
        moodEventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<MoodEvent> moodEventsList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MoodEvent moodEvent = document.toObject(MoodEvent.class);
                    moodEventsList.add(moodEvent);
                }

            } else {
                Log.e("FIRESTORE", "Error getting documents: ", task.getException());
            }
        });
    }

    private void updateMoodEvent(MoodEvent moodEvent) {
        moodEventsRef.whereEqualTo("description", moodEventToEdit.getDescription()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            moodEventsRef.document(document.getId()).set(moodEvent)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FIRESTORE", "Mood event updated successfully!");
                                        refreshMoodEventsList();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Update failed", e));
                        }
                    } else {
                        Log.e("FIRESTORE", "Mood event not found in Firestore");
                    }
                });
    }


    private void deleteMoodEvent(MoodEvent moodEvent) {
        moodEventsRef.whereEqualTo("date", moodEvent.getDate()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            moodEventsRef.document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FIRESTORE", "Mood event deleted successfully");
                                        refreshMoodEventsList();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Failed to delete mood event", e));
                        }
                    } else {
                        Log.e("FIRESTORE", "Mood event not found for deletion");
                    }
                });
    }
    // yo, this method is our debug function for firestore writes, don't fuck it up

    private void showSuccessDialogUI() {
        if (isAdded() && !isDetached() && !isRemoving()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);

            View customView = getLayoutInflater().inflate(R.layout.custom_success_dialog, null);
            builder.setView(customView);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                navigateToMoodHistory();
            }, 2000);
        }
    }

    private void navigateToMoodHistory() {
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination().getId() == R.id.editMoodActivityFragment ||
                navController.getCurrentDestination().getId() == R.id.editMoodActivityFragment2) {
            try {
                navController.navigate(R.id.action_editMoodActivityFragment_to_moodHistoryFragment);
            } catch (IllegalArgumentException e) {
                Log.e("Navigation", "Failed to navigate: " + e.getMessage());
                // Fallback navigation if needed
                navController.navigate(R.id.moodHistoryFragment);
            }
        }
    }

    private void showErrorToast(Exception e) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void selectMood(String mood, ImageView selectedImageView) {
        if (lastSelectedImageView != null) {
            lastSelectedImageView.setColorFilter(null);
            animateImageDeselection(lastSelectedImageView);
        }

        selectedImageView.setColorFilter(createColorFilter());
        animateImageSelection(selectedImageView);
        lastSelectedImageView = selectedImageView;

        Spinner moodSpinner = binding1.mainCard;
        int position = getPositionForMood(mood);
        moodSpinner.setSelection(position);

        updateBackgroundColor(mood);

        Toast.makeText(getContext(), "Selected: " + mood, Toast.LENGTH_SHORT).show();
    }


    private int getPositionForMood(String mood) {
        String[] moodArray = getResources().getStringArray(R.array.spinner_items);
        for (int i = 0; i < moodArray.length; i++) {
            if (moodArray[i].equals(mood)) {
                return i;
            }
        }
        return 0;
    }
    private ColorMatrixColorFilter createColorFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        matrix.setScale(0.8f, 0.8f, 0.8f, 1.0f);
        return new ColorMatrixColorFilter(matrix);
    }

    private void animateImageSelection(ImageView imageView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.2f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void animateImageDeselection(ImageView imageView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.2f, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }


    private void updateBackgroundColor(String mood) {
        Integer gradientResId = moodGradients.get(mood);
        if (gradientResId != null) {
            mainLayout.setBackgroundResource(gradientResId);
        }
//         else {
//            mainLayout.setBackgroundResource(R.drawable.default_gradient); // Default gradient
//        }
    }

    private void setImageColor(ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.selected_mood_color));
    }

    private void resetImageColor(ImageView imageView) {
        imageView.clearColorFilter();
    }

    private int getPositionOfValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}
