package com.example.moodsync;

import static android.app.Activity.RESULT_OK;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.graphics.drawable.BitmapDrawable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.moodsync.databinding.AddMoodFragmentBinding;
import com.example.moodsync.databinding.AddMoodFragment2Binding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class AddMoodActivity extends Fragment {
    private String moodDescription;
    private String selectedMood;
    private AddMoodFragmentBinding binding1;
    private AddMoodFragment2Binding binding2;
    private boolean isSecondLayout = false;
    private RelativeLayout mainLayout;
    private Uri photoUri;
    static String imageUrl = " ";

    private ImageView happyImage, sadImage, angryImage, confusedImage, surprisedImage, ashamedImage, scaredImage, disgustedImage;
    private ImageView lastSelectedImageView = null;
    private Button selectedSocialSituationButton = null;

    private static final int ANIMATION_DURATION = 300; // Animation duration in milliseconds

    private FirebaseFirestore db;
    private long MAX_PHOTO_SIZE = 64;
    private MoodEvent exampleMood;
    private CollectionReference moodEventsRef;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private Map<String, Integer> moodGradients = new HashMap<>();

    static int imageAddedFlag =0 ;


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

    private void setupRectangleClickListener() {
        View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
        rectangle2.setOnClickListener(v -> showPhotoOptionsDialog());
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"Add from Camera", "Add from Photos"};
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
    private long checkImageSize(Uri imageUri) {
        Cursor cursor = requireContext().getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                long imageSizeInBytes = cursor.getLong(sizeIndex);
                long imageSizeInKB = imageSizeInBytes / 1024;
                Log.d("Image Size", "Size in bytes: " + imageSizeInKB);
                return imageSizeInKB;
            }
            cursor.close();
        }
        return 0;
    }

    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            createImageFile();
            if (photoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }
    private void createImageFile() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/");

        photoUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void uploadImageToFirebase(Uri imageUri, OnImageUploadedListener listener) {
        String path = "mood_images/" + UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                imageUrl = downloadUrl.toString();
                Log.d("FirebaseStorage", "Image URL: " + imageUrl);
                listener.onImageUploaded(imageUrl); // Notify listener
            }).addOnFailureListener(exception -> {
                Log.e("FirebaseStorage", "Failed to get download URL: " + exception.getMessage());
                listener.onUploadFailed(exception);
            });

        }).addOnFailureListener(exception -> {
            Log.e("FirebaseStorage", "Upload failed: " + exception.getMessage());
            listener.onUploadFailed(exception);
        });
    }


    interface OnImageUploadedListener {
        void onImageUploaded(String imageUrl);
        void onUploadFailed(Exception e);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            long size = checkImageSize(photoUri);
            if (size < MAX_PHOTO_SIZE) {
                uploadImageToFirebase(photoUri, new OnImageUploadedListener() {

                    @Override
                    public void onImageUploaded(String imageUrl) {
                        View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
                        rectangle2.setBackground(new BitmapDrawable(getResources(), getBitmapFromUri(photoUri)));
                        imageAddedFlag = 1;
                    }
                    @Override
                    public void onUploadFailed(Exception e) {
                    }
                });
            } else {
                showPhotoOptionsDialog();
            }

        } else if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            Uri selectedImageUri = data.getData();
            long size = checkImageSize(selectedImageUri);
            if (size < MAX_PHOTO_SIZE) {
                Log.d("FIREBASEEEE", "Changed BG");
                uploadImageToFirebase(selectedImageUri, new OnImageUploadedListener() {

                    @Override
                    public void onImageUploaded(String imageUrl) {
                        View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
                        rectangle2.setBackground(new BitmapDrawable(getResources(), getBitmapFromUri(selectedImageUri)));
                        TextView text = binding1.getRoot().findViewById(R.id.add_photos);
                        text.setText("");
                        text = binding1.getRoot().findViewById(R.id.upto_12mb);
                        text.setText("");
                        ImageView image = binding1.getRoot().findViewById(R.id.photos);

                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setCornerRadius(20); // Set the corner radius in pixels
                        drawable.setColor(Color.TRANSPARENT);

                        image.setBackground(drawable);
                        image.setClipToOutline(true); // Round thhe corners

                    }
                    @Override
                    public void onUploadFailed(Exception e) {
                    }
                });
            } else {
                showPhotoOptionsDialog();
            }
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void setupSecondLayout() {
        Log.d("LIFECYCLE", "setupSecondLayout called");
        // Retrieve arguments safely
        if (getArguments() != null) {
            this.selectedMood = getArguments().getString("selectedMood", "");
            this.moodDescription = getArguments().getString("description", "");
        }
            binding2.createmood.setOnClickListener(v -> {
                String trigger = binding2.triggerInput.getText().toString();
                String socialSituation = (selectedSocialSituationButton != null) ?
                        selectedSocialSituationButton.getText().toString() : "None";

                Log.d("Firebase", "Final imageUrl: " + imageUrl);
                MoodEvent moodEvent = new MoodEvent(
                        this.selectedMood,
                        trigger,
                        this.moodDescription,
                        socialSituation,
                        imageUrl
                );
                saveMoodEventToFirestore(moodEvent);
            });

        binding2.backbutton.setOnClickListener(v -> NavHostFragment.findNavController(AddMoodActivity.this)
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

    private void showSuccessDialog() {
        String socialSituation = selectedSocialSituationButton != null ?
                selectedSocialSituationButton.getText().toString() : "None";

        EditText triggerInput = binding2.triggerInput;
        String trigger = triggerInput.getText().toString();
        MoodEvent moodEvent = new MoodEvent(
                selectedMood,
                trigger,
                moodDescription,
                socialSituation,imageUrl
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
    private void saveMoodEventToFirestore(MoodEvent moodEvent) {
        moodEventsRef.add(moodEvent)
                .addOnSuccessListener(aVoid -> showSuccessDialogUI())
                .addOnFailureListener(e -> showErrorToast(e));
    }


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

    private void showErrorToast(Exception e) {
        Toast.makeText(requireContext(), "Failed to add mood event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void updateBackgroundColor(String mood) {
        int gradientResId = moodGradients.getOrDefault(mood, R.drawable.edit_text_default);
        Drawable gradient = ContextCompat.getDrawable(requireContext(), gradientResId);
        if (gradient != null) {
            mainLayout.setBackground(gradient);

        } else {
            mainLayout.setBackgroundColor(Color.parseColor("#203434"));
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

    private void debugFirestoreWrite(MoodEvent moodEvent) {
        // init the firestore db, bro, don't be a chutiya and skip this
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // try to add the mood event doc, sending that shit out
        db.collection("mood_events").add(moodEvent)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DEBUG", "doc added with id: " + documentReference.getId());
                    Toast.makeText(getContext(), "success! mood event added, bro!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Log.e("DEBUG", "failed to add mood event: ", e);
                    Toast.makeText(getContext(), "fuck! write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}