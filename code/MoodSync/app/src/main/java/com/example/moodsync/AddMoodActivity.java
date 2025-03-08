package com.example.moodsync;

import static android.app.Activity.RESULT_OK;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Button;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.AddMoodFragmentBinding;
import com.example.moodsync.databinding.AddMoodFragment2Binding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A Fragment class for adding and managing mood entries.
 * This class allows users to select their current mood, provide a description,
 * and specify the social situation associated with their mood. It also includes
 * functionality to add a photo related to the mood.
 */
public class AddMoodActivity extends Fragment {
    private String moodDescription;
    private String selectedMood;
    private AddMoodFragmentBinding binding1;
    private AddMoodFragment2Binding binding2;
    private boolean isSecondLayout = false;
    private RelativeLayout mainLayout;
    private Uri photoUri;

    private final Map<String, Integer> moodGradients = new HashMap<>();

    private ImageView happyImage, sadImage, angryImage, confusedImage, surprisedImage, ashamedImage, scaredImage, disgustedImage;
    private ImageView lastSelectedImageView = null;
    private Button selectedSocialSituationButton = null;

    private static final int ANIMATION_DURATION = 300; // Animation duration in milliseconds

    private FirebaseFirestore db;
    private CollectionReference moodEventsRef;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize Firestore
        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();
        moodEventsRef = db.collection("mood_events");

        if (getArguments() != null && getArguments().getBoolean("isSecondLayout", false)) {
            isSecondLayout = true;
            binding2 = AddMoodFragment2Binding.inflate(inflater, container, false);
            return binding2.getRoot();
        } else {
            binding1 = AddMoodFragmentBinding.inflate(inflater, container, false);
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

        if (isSecondLayout) {
            setupSecondLayout();
        } else {
            setupFirstLayout(view);
        }
    }

    /**
     * Sets up the first layout which includes mood selection and description input.
     *
     * @param view
     *     The root view of the fragment.
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

        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = parent.getItemAtPosition(position).toString();
                updateBackgroundColor(selectedMood);
                Toast.makeText(getContext(), "Selected: " + selectedMood, Toast.LENGTH_SHORT).show();

                switch (selectedMood) {
                    case "Happy": selectMood("Happy", happyImage); break;
                    case "Sad": selectMood("Sad", sadImage); break;
                    case "Angry": selectMood("Angry", angryImage); break;
                    case "Confused": selectMood("Confused", confusedImage); break;
                    case "Surprised": selectMood("Surprised", surprisedImage); break;
                    case "Ashamed": selectMood("Ashamed", ashamedImage); break;
                    case "Scared": selectMood("Scared", scaredImage); break;
                    case "Disgusted": selectMood("Disgusted", disgustedImage); break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding1.cancel.setOnClickListener(v -> NavHostFragment.findNavController(AddMoodActivity.this)
                .navigate(R.id.action_addMoodActivityFragment_to_SecondFragment));

        binding1.next.setOnClickListener(v -> {
            this.selectedMood = binding1.mainCard.getSelectedItem().toString();
            this.moodDescription = binding1.editDescription.getText().toString();

            Bundle args = new Bundle();
            args.putBoolean("isSecondLayout", true);
            args.putString("selectedMood", this.selectedMood);
            args.putString("description", this.moodDescription);

            NavHostFragment.findNavController(AddMoodActivity.this)
                    .navigate(R.id.action_addMoodActivityFragment_to_addMoodActivityFragment2, args);
        });

        setupRectangleClickListener();
    }

    /**
     * Sets up a click listener for the rectangle view to allow users to add a photo.
     */
    private void setupRectangleClickListener() {
        View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
        rectangle2.setOnClickListener(v -> showPhotoOptionsDialog());
    }

    /**
     * Displays a dialog to allow users to choose between adding a photo from the camera or gallery.
     */
    private void showPhotoOptionsDialog() {
        String[] options = {"Add from Camera", "Add from Photos"};
        Log.d("Camera" , "i CLICKED ON THE BUTTON BITCH");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                })
                .show();
    }

    /**
     * Opens the camera application to capture a photo.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {

                photoUri = FileProvider.getUriForFile(requireContext(), "com.example.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Creates an image file to store the captured photo.
     *
     * @return The created File object.
     * @throws IOException If an error occurs during file creation.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    /**
     * Opens the gallery application to select a photo.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
            if (requestCode == REQUEST_IMAGE_CAPTURE) {

                rectangle2.setBackground(new BitmapDrawable(getResources(), getBitmapFromUri(photoUri)));
            } else if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri selectedImageUri = data.getData();

                rectangle2.setBackground(new BitmapDrawable(getResources(), getBitmapFromUri(selectedImageUri)));
            }
        }
    }

    /**
     * Retrieves a Bitmap from the given Uri.
     *
     * @param uri The Uri of the image.
     * @return The Bitmap image, or null if an error occurred.
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Sets up the second layout which includes trigger input and social situation selection.
     */
    private void setupSecondLayout() {
        Log.d("LIFECYCLE", "setupSecondLayout called");
        // Retrieve arguments safely
        if (getArguments() != null) {
            this.selectedMood = getArguments().getString("selectedMood", "");
            this.moodDescription = getArguments().getString("description", "");
        }


        EditText triggerInput = binding2.triggerInput;

        binding2.createmood.setOnClickListener(v -> {

            String trigger = triggerInput.getText().toString();


            String socialSituation = (selectedSocialSituationButton != null) ?
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

            if (photoUri != null) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("mood_images/" + UUID.randomUUID().toString());
                storageRef.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> {

                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                moodEvent.setImageUrl(uri.toString());

                                saveMoodEventToFirestore(moodEvent);
                            });
                        })
                        .addOnFailureListener(e -> showErrorToast(e));
            } else {

                saveMoodEventToFirestore(moodEvent);
            }
        });

        binding2.backbutton.setOnClickListener(v -> NavHostFragment.findNavController(AddMoodActivity.this)
                .navigateUp());

        binding2.ss1.setOnClickListener(v -> selectSocialSituation(binding2.ss1));
        binding2.ss2.setOnClickListener(v -> selectSocialSituation(binding2.ss2));
        binding2.ss3.setOnClickListener(v -> selectSocialSituation(binding2.ss3));
        binding2.ss4.setOnClickListener(v -> selectSocialSituation(binding2.ss4));
    }


    /**
     * Selects a social situation and updates the UI accordingly.
     *
     * @param button The button representing the selected social situation.
     */
    private void selectSocialSituation(Button button) {
        if (selectedSocialSituationButton != null) {
            animateButtonDeselection(selectedSocialSituationButton);
        }

        animateButtonSelection(button);
        selectedSocialSituationButton = button;
    }

    /**
     * Animates the selection of a social situation button.
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
     * Animates the deselection of a social situation button.
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
     * Displays a success dialog after successfully saving the mood event.
     */
    private void showSuccessDialog() {
        String socialSituation = selectedSocialSituationButton != null ?
                selectedSocialSituationButton.getText().toString() : "None";

        EditText triggerInput = binding2.triggerInput;
        String trigger = triggerInput.getText().toString();

        // Get current timestamp
        long currentTimestamp = System.currentTimeMillis();

        MoodEvent moodEvent = new MoodEvent(
                this.selectedMood,
                trigger,
                this.moodDescription,
                socialSituation,
                currentTimestamp // Pass the timestamp to the MoodEvent
        );

        Log.d("FIRESTORE", "Attempting to save: " + moodEvent.toString());

        moodEventsRef.add(moodEvent)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FIRESTORE", "Save successful with ID: " + documentReference.getId());
                    showSuccessDialogUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Save failed", e);
                    showErrorToast(e);
                });
    }

    /**
     * Refreshes the list of mood events from Firestore.
     * Retrieves all mood events from the "mood_events" collection in Firestore,
     * converts them into `MoodEvent` objects, and adds them to a list.
     * If the retrieval is successful, the mood events list is updated.
     * If there is an error during retrieval, an error message is logged.
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
     * Saves a mood event to Firestore.
     * Adds the provided `MoodEvent` object to the "mood_events" collection in Firestore.
     * On successful addition, it triggers the UI to show a success dialog.
     * On failure, it displays an error message using a toast.
     *
     * @param moodEvent The `MoodEvent` object to be saved to Firestore.
     */
    private void saveMoodEventToFirestore(MoodEvent moodEvent) {
        moodEventsRef.add(moodEvent)
                .addOnSuccessListener(aVoid -> showSuccessDialogUI())
                .addOnFailureListener(e -> showErrorToast(e));
    }

    /**
     * Updates an existing mood event in Firestore.
     * It first queries Firestore to find the document matching the mood event's date.
     * If a matching document is found, it updates that document with the new `MoodEvent` data.
     * On successful update, it logs a success message and refreshes the mood events list.
     * On failure, it logs an error message. If no matching mood event is found, it logs a "not found" error.
     *
     * @param moodEvent The `MoodEvent` object containing the updated data.  The date field is used to identify the event to update.
     */
    private void updateMoodEvent(MoodEvent moodEvent) {
        moodEventsRef.whereEqualTo("date", moodEvent.getDate()).get()
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
     * Deletes a mood event from Firestore.
     * It queries Firestore to find the document matching the mood event's date.
     * If a matching document is found, it deletes that document.
     * On successful deletion, it logs a success message and refreshes the mood events list.
     * On failure, it logs an error message. If no matching mood event is found, it logs a "not found" error.
     *
     * @param moodEvent The `MoodEvent` object identifying the event to delete. The date field is used to identify the event.
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
     * Displays a custom success dialog to indicate that the mood event has been saved successfully.
     * The dialog is displayed using a custom style and layout, and it automatically dismisses
     * after 2 seconds, navigating the user back to the previous fragment.
     */
    private void showSuccessDialogUI() {
        // Inside the showSuccessDialogUI() method
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);  // Use a custom style

        View customView = getLayoutInflater().inflate(R.layout.custom_success_dialog, null);
        builder.setView(customView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Make the background transparent
        dialog.show();

        // Dismiss the dialog after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            NavHostFragment.findNavController(AddMoodActivity.this)
                    .navigate(R.id.action_addMoodActivityFragment2_to_SecondFragment);
        }, 2000); // Dismiss after 2 seconds
    }

    /**
     * Displays an error message as a toast when saving the mood event fails.
     * The toast message includes the specific error message from the exception.
     *
     * @param e The exception that occurred during the mood event saving process.  The exception's message is displayed in the toast.
     */
    private void showErrorToast(Exception e) {
        Toast.makeText(requireContext(), "Failed to add mood event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates the background color of the main layout based on the selected mood.
     * It checks if the selected mood exists in the `moodGradients` map.
     * If it exists, it sets the background of the `mainLayout` to the corresponding gradient drawable.
     * If the drawable is null, or the mood doesn't exist in the map, it sets the background color to a default value.
     *
     * @param mood The selected mood to update the background color for.
     */
    private void updateBackgroundColor(String mood) {
        if (moodGradients.containsKey(mood)) {
            int gradientResId = moodGradients.get(mood);
            Drawable gradient = ContextCompat.getDrawable(requireContext(), gradientResId);

            if (gradient != null) {
                mainLayout.setBackground(gradient);
            } else {
                mainLayout.setBackgroundColor(Color.parseColor("#203434"));
            }
        } else {
            mainLayout.setBackgroundColor(Color.parseColor("#203434"));
        }
    }

    /**
     * A debug function for writing a MoodEvent object to Firestore.
     * Initializes a Firestore instance and attempts to add the provided MoodEvent
     * to the "mood_events" collection. On success, it logs the document ID and
     * displays a success toast. On failure, it logs the error and displays an
     * error toast with the exception message.
     *
     * @param moodEvent The MoodEvent object to be written to Firestore for debugging purposes.
     */
    private void debugFirestoreWrite(MoodEvent moodEvent) {
        // init the firestore db, bro, don't be a chutiya and skip this
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // try to add the mood event doc, sending that shit out
        db.collection("mood_events").add(moodEvent)
                .addOnSuccessListener(documentReference -> {
                    // success, motherfucker! we got a doc id here, log that shit
                    Log.d("DEBUG", "doc added with id: " + documentReference.getId());
                    Toast.makeText(getContext(), "success! mood event added, bro!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // shit, write failed! log the error so we know what the fuck happened
                    Log.e("DEBUG", "failed to add mood event: ", e);
                    Toast.makeText(getContext(), "fuck! write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Handles the selection of a mood, updating the UI accordingly.
     * When a mood is selected:
     * 1. It deselects the previously selected mood image, if any.
     * 2. It applies a color filter to the newly selected mood image.
     * 3. It animates the selection/deselection of the images.
     * 4. It updates the selected mood in the mood spinner.
     * 5. It updates the background color of the main layout.
     * 6. It displays a toast message indicating the selected mood.
     *
     * @param mood The selected mood.
     * @param selectedImageView The ImageView corresponding to the selected mood.
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
     * Animates the selection of an ImageView by scaling it up.
     * Uses ObjectAnimator to create a scaling effect on the X and Y axes of the ImageView.
     * The animation duration and interpolator are defined by `ANIMATION_DURATION` and
     * `AccelerateDecelerateInterpolator`, respectively.
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
     * Animates the deselection of an ImageView by scaling it down.
     * Uses ObjectAnimator to create a scaling effect on the X and Y axes of the ImageView,
     * returning it to its original size. The animation duration and interpolator are defined
     * by `ANIMATION_DURATION` and `AccelerateDecelerateInterpolator`, respectively.
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
     * Gets the position of a mood in the mood spinner's array.
     * Iterates through the mood array defined in `R.array.spinner_items` and returns the index
     * of the mood that matches the provided `mood` parameter. If the mood is not found, it returns 0.
     *
     * @param mood The mood to find the position for.
     * @return The position of the mood in the spinner's array, or 0 if not found.
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
     * Creates a color filter to apply to mood images.
     * Creates a `ColorMatrixColorFilter` that desaturates the image and reduces its brightness.
     * This filter is used to visually distinguish the selected mood image.
     *
     * @return A `ColorMatrixColorFilter` for desaturating and dimming an image.
     */
    private ColorMatrixColorFilter createColorFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        matrix.setScale(0.8f, 0.8f, 0.8f, 1.0f);
        return new ColorMatrixColorFilter(matrix);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}