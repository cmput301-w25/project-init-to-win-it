package com.example.moodsync;

import android.animation.ObjectAnimator;

import android.graphics.Color;
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
import android.widget.TextView;
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
    private String imageUrl = "";

    private final Map<String, Integer> moodGradients = new HashMap<>();

    private ImageView happyImage, sadImage, angryImage, confusedImage, surprisedImage, ashamedImage, scaredImage, disgustedImage;
    private ImageView lastSelectedImageView = null;
    private Button selectedSocialSituationButton = null;

    private static final int ANIMATION_DURATION = 300; // Animation duration in milliseconds

    private FirebaseFirestore db;
    private CollectionReference moodEventsRef;
    private MoodEvent moodEventToEdit;

    /**
     * Creates the view for the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The root view of the fragment's layout.
     */
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


    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
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


    /**
     * Sets up the first layout of the fragment.
     *
     * @param view The root view of the layout.
     */
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
            String selectedMood = binding1.mainCard.getSelectedItem().toString();

            if (selectedMood.equals("None")) {
                Toast.makeText(requireContext(), "Please select a mood other than 'None'", Toast.LENGTH_SHORT).show();
            } else {
                this.selectedMood = selectedMood;
                this.moodDescription = binding1.editDescription.getText().toString();

                Bundle args = new Bundle();
                args.putBoolean("isSecondLayout", true);
                args.putString("selectedMood", this.selectedMood);
                args.putString("description", this.moodDescription);
                args.putParcelable("moodEvent", (Parcelable) moodEventToEdit); //Pass the object to the fragment.

                NavHostFragment.findNavController(EditMoodActivity.this)
                        .navigate(R.id.action_editMoodActivityFragment_to_editMoodActivityFragment2, args);
            }
        });

    }

    /**
     * Resets the background of all mood-related emoji image views to transparent.
     * This ensures that no emoji is highlighted or selected visually.
     */
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

    /**
     * Sets the given value on the spinner based on a string value.
     *
     * @param spinner The spinner to update.
     * @param value The value to select in the spinner.
     */
    private void setSpinnerToValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Sets up the second layout, initializing the fields, mood descriptions,
     * and handling the creation or update of a mood event.
     */
    private void setupSecondLayout() {
        Log.d("LIFECYCLE", "setupSecondLayout called");
        if (getArguments() != null) {
            this.selectedMood = getArguments().getString("selectedMood", "");
            this.moodDescription = getArguments().getString("description", "");
            moodEventToEdit = getArguments().getParcelable("moodEvent"); // Get moodEvent from parameters
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

        binding2.createmood.setOnClickListener(v -> {

            String trigger = triggerInput.getText().toString();

            String socialSituation1 = (selectedSocialSituationButton != null) ?
                    selectedSocialSituationButton.getText().toString() : "None";

            long currentTimestamp = System.currentTimeMillis();
            MoodEvent moodEvent = new MoodEvent(
                    this.selectedMood,
                    trigger,
                    this.moodDescription,
                    socialSituation,
                    currentTimestamp, // Pass the timestamp to the MoodEvent
                    imageUrl
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

    /**
     * Selects the social situation button and animates the selection.
     *
     * @param button The button to select.
     */
    private void selectSocialSituation(Button button) {
        if (selectedSocialSituationButton != null) {
            animateButtonDeselection(selectedSocialSituationButton);
        }

        animateButtonSelection(button);
        selectedSocialSituationButton = button;
    }

    /**
     * Animates the selection of a button by scaling it up and changing its background tint.
     *
     * @param button The button to animate.
     */
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

    /**
     * Animates the deselection of a button by scaling it back to the normal size and changing its background tint.
     *
     * @param button The button to animate.
     */
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

    /**
     * Refreshes the list of mood events from Firestore.
     */
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

    /**
     * Updates the mood event in Firestore with new data.
     *
     * @param moodEvent The mood event to update.
     */
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
    /**
     * Deletes a mood event from the Firestore database.
     *
     * @param moodEvent The mood event to be deleted.
     */
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

    /**
     * Displays a success dialog UI for a mood event action and navigates to the mood history after a delay.
     */
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

    /**
     * Navigates to the Mood History fragment.
     */
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

    /**
     * Shows a toast message indicating an error during a save operation.
     *
     * @param e The exception that occurred during the save operation.
     */
    private void showErrorToast(Exception e) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Selects a mood, updates the corresponding UI elements, and provides feedback to the user.
     *
     * @param mood The selected mood.
     * @param selectedImageView The ImageView associated with the selected mood.
     */
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

    /**
     * Gets the position of the selected mood in the mood array.
     *
     * @param mood The mood whose position is to be found.
     * @return The position of the mood in the mood array.
     */
    private int getPositionForMood(String mood) {
        String[] moodArray = getResources().getStringArray(R.array.spinner_items);
        for (int i = 0; i < moodArray.length; i++) {
            if (moodArray[i].equals(mood)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Creates a color filter to adjust the image saturation and scale.
     *
     * @return The created color filter.
     */
    private ColorMatrixColorFilter createColorFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        matrix.setScale(0.8f, 0.8f, 0.8f, 1.0f);
        return new ColorMatrixColorFilter(matrix);
    }

    /**
     * Animates the selection of an image (scaling effect).
     *
     * @param imageView The ImageView to be animated.
     */
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

    /**
     * Animates the deselection of an image (scaling effect).
     *
     * @param imageView The ImageView to be animated.
     */
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

    /**
     * Updates the background color of the main layout based on the selected mood.
     *
     * @param mood The selected mood.
     */
    private void updateBackgroundColor(String mood) {
        Integer gradientResId = moodGradients.get(mood);
        if (gradientResId != null) {
            mainLayout.setBackgroundResource(gradientResId);
        }
        else {
            mainLayout.setBackgroundColor(Color.parseColor("#203434")); // Default gradient
        }
        EditText editDescription = binding1.editDescription;
        Spinner spinnerStuff = binding1.mainCard;
        View rectangleViewOrSum = binding1.rectangle2;
        TextView textView = binding1.stepIndicator;
        Button button = binding1.next;

        switch (mood) {
            case "None":
                editDescription.setBackgroundResource(R.drawable.edit_text_background);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_background);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_background);
                button.setBackgroundResource(R.drawable.edit_text_background);
                textView.setBackgroundResource(R.drawable.edit_text_background);
                textView.setTextColor(R.drawable.blackmamba);
                editDescription.setTextColor(Color.parseColor("#5A4A33"));
                break;
            case "Happy":
                editDescription.setBackgroundResource(R.drawable.edit_text_happy);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_happy);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_happy);
                button.setBackgroundResource(R.drawable.edit_text_happy);
                textView.setBackgroundResource(R.drawable.edit_text_happy);
                editDescription.setTextColor(Color.parseColor("#5A4A33"));
                break;
            case "Sad":
                editDescription.setBackgroundResource(R.drawable.edit_text_sad);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_sad);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_sad);
                button.setBackgroundResource(R.drawable.edit_text_sad);
                textView.setBackgroundResource(R.drawable.edit_text_sad);
                editDescription.setTextColor(Color.parseColor("#2C3E50"));
                break;
            case "Angry":
                editDescription.setBackgroundResource(R.drawable.edit_text_angry);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_angry);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_angry);
                button.setBackgroundResource(R.drawable.edit_text_angry);
                textView.setBackgroundResource(R.drawable.edit_text_angry);
                editDescription.setTextColor(Color.parseColor("#4D1A1A"));
                break;
            case "Confused":
                editDescription.setBackgroundResource(R.drawable.edit_text_confused);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_confused);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_confused);
                button.setBackgroundResource(R.drawable.edit_text_confused);
                textView.setBackgroundResource(R.drawable.edit_text_confused);
                editDescription.setTextColor(Color.parseColor("#3A2D58"));
                break;
            case "Surprised":
                editDescription.setBackgroundResource(R.drawable.edit_text_surprised);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_surprised);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_surprised);
                button.setBackgroundResource(R.drawable.edit_text_surprised);
                textView.setBackgroundResource(R.drawable.edit_text_surprised);
                editDescription.setTextColor(Color.parseColor("#5D2B3E"));
                break;
            case "Ashamed":
                editDescription.setBackgroundResource(R.drawable.edit_text_ashamed);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_ashamed);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_ashamed);
                textView.setBackgroundResource(R.drawable.edit_text_ashamed);
                button.setBackgroundResource(R.drawable.edit_text_ashamed);

                editDescription.setTextColor(Color.parseColor("#5C3A21"));
                break;
            case "Scared":
                editDescription.setBackgroundResource(R.drawable.edit_text_scared);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_scared);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_scared);
                textView.setBackgroundResource(R.drawable.edit_text_scared);
                button.setBackgroundResource(R.drawable.edit_text_scared);

                editDescription.setTextColor(Color.parseColor("#2B3F5D"));
                break;
            case "Disgusted":
                editDescription.setBackgroundResource(R.drawable.edit_text_disgusted);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_disgusted);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_disgusted);
                textView.setBackgroundResource(R.drawable.edit_text_disgusted);
                button.setBackgroundResource(R.drawable.edit_text_disgusted);

                editDescription.setTextColor(Color.parseColor("#264D33"));
                break;

            default:
                // Default textbox background if no mood matches
                editDescription.setBackgroundResource(R.drawable.edit_text_default);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_default);
                button.setBackgroundResource(R.drawable.edit_text_default);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_default);
                editDescription.setTextColor(Color.parseColor("#204343"));
        }
    }

    /**
     * Sets the color of an image view to the selected mood's color.
     *
     * @param imageView The ImageView to change the color of.
     */
    private void setImageColor(ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.selected_mood_color));
    }

    /**
     * Resets the color of the image view to its original color.
     *
     * @param imageView The ImageView to reset the color of.
     */
    private void resetImageColor(ImageView imageView) {
        imageView.clearColorFilter();
    }

    /**
     * Finds the position of a value in a spinner.
     *
     * @param spinner The spinner to search through.
     * @param value The value to find in the spinner.
     * @return The position of the value in the spinner, or -1 if not found.
     */
    private int getPositionOfValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Cleans up the view bindings when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}
