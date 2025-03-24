package com.example.moodsync;

import static com.example.moodsync.BitmapUtils.compressImageFromUri;
import static android.app.Activity.RESULT_OK;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.graphics.Matrix;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditProfileActivity extends Fragment {

    private ImageView backButton, profileImageEdit;
    private EditText fullName, bio;
    private Button confirmButton, backButtonMaterial; // MaterialButton can be cast to Button
    private TextView editProfileTitle;
    static Uri photoUri;
    static String imageUrl;
    public LocalStorage globalStorage = LocalStorage.getInstance();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_page, container, false);
        db = FirebaseFirestore.getInstance();
        // Initialize views using findViewById
        backButton = view.findViewById(R.id.back_button);
        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        fullName = view.findViewById(R.id.fullName);
        bio = view.findViewById(R.id.bio);
        confirmButton = view.findViewById(R.id.confirmbutton);
        backButtonMaterial = view.findViewById(R.id.backbutton);

        fullName.setHint(globalStorage.getCurrentUser().getName());
        bio.setHint(globalStorage.getCurrentUser().getBio());


        backButton.setOnClickListener(v -> navigateBack());
        backButtonMaterial.setOnClickListener(v -> navigateBack());
        confirmButton.setOnClickListener(v -> saveChanges());
        profileImageEdit.setOnClickListener(v -> {
            showPhotoOptionsDialog();

        });


        return view;
    }

    private void navigateBack() {
        // Navigate back or close the fragment
        requireActivity().onBackPressed();
    }
    private void showPhotoOptionsDialog() {
        String[] options = {"Add from Camera", "Add from Photos"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                })
                .show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri imageUri = (requestCode == 1) ? photoUri : data.getData();
            handleImageUpload(imageUri);
        }
    }

    private void handleImageUpload(Uri imageUri) {
        uploadImageToFirebase(imageUri, new OnImageUploadedListener() {
            @Override
            public void onImageUploaded(String imageUrl) {
                photoUri = Uri.parse(imageUrl);
                updateImagePreview(photoUri);
            }

            @Override
            public void onUploadFailed(Exception e) {
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public class RotateTransformation extends BitmapTransformation {
        private float rotateRotationAngle = 0f;

        public RotateTransformation(float rotateRotationAngle) {
            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotateRotationAngle);
            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(("rotate" + rotateRotationAngle).getBytes());
        }
    }


    private void updateImagePreview(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .transform(new RotateTransformation(90))
                .into(profileImageEdit);
    }



    /**
     * Checks the size of an image.
     *
     * @param imageUri The URI of the image to check.
     * @return The size of the image in kilobytes.
     */
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
    /**
     * Opens the camera to capture an image.
     */
    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            createImageFile();
            if (photoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, 1);

            }
        }
    }

    /**
     * Creates a new image file.
     */
    private void createImageFile() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/");

        photoUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    /**
     * Opens the gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
    }
    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageUri The URI of the image to upload.
     * @param listener The listener to notify when the upload is complete.
     */
    private void uploadImageToFirebase(Uri imageUri, OnImageUploadedListener listener) {
        File compressedFile = compressImageFromUri(this.getContext(), imageUri);
        Log.d("COMPRESSION","REACHED HERE");
        Uri compressedUri = Uri.fromFile(compressedFile);
        Log.d("COMRPESSION", String.valueOf(checkImageSize(compressedUri)));
        String path = "mood_images/" + UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        UploadTask uploadTask = storageRef.putFile(compressedUri);

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


    private void saveChanges() {
        // Save changes logic (e.g., update database or show a success message)
        String name = fullName.getText().toString().trim();
        String biography = bio.getText().toString().trim();

        if (name.isEmpty() || biography.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the current user's ID (assuming you have a way to retrieve it)
        String currentUserId = globalStorage.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Unable to retrieve current user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the Firestore document for the user
        db.collection("users").document(currentUserId)
                .update(
                        "fullName", name,
                        "bio", biography,
                        "profileImageUrl",imageUrl
                )
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(getContext(), "Changes saved to Firestore", Toast.LENGTH_SHORT).show();
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating document", e);
                    Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
