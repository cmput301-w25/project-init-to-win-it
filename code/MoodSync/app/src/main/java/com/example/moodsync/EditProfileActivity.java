package com.example.moodsync;

import static android.app.Activity.RESULT_OK;

import static com.example.moodsync.BitmapUtils.compressImageFromUri;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditProfileActivity extends Fragment {

    private ImageView profileImageEdit;
    private EditText fullName, bio;
    private Button confirmButton, backButtonMaterial;
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
        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        fullName = view.findViewById(R.id.fullName);
        bio = view.findViewById(R.id.bio);
        confirmButton = view.findViewById(R.id.confirmbutton);
        backButtonMaterial = view.findViewById(R.id.backbutton);

        backButtonMaterial.setOnClickListener(v -> navigateBack());
        confirmButton.setOnClickListener(v -> saveChanges());
        profileImageEdit.setOnClickListener(v -> showPhotoOptionsDialog());

        String userId = globalStorage.getCurrentUserId();
        fetchProfileInformation(userId);

        return view;
    }

    private void fetchProfileInformation(String userId) {
        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String name = document.getString("fullName");
                                String biography = document.getString("bio");
                                String profileImageUrl = document.getString("profileImageUrl");

                                // Update UI only if the fragment is still attached
                                if (isAdded() && getActivity() != null) {
                                    fullName.setText(name);
                                    bio.setText(biography);

                                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                        loadProfileImage(profileImageUrl);
                                    }
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    });
        } else {
            Log.e("Firestore", "User not authenticated");
        }
    }


    private void navigateBack() {
        // Navigate back or close the fragment
        requireActivity().onBackPressed();
    }

    private void loadProfileImage(String imageUrl) {
        if (isAdded() && getActivity() != null) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(profileImageEdit);
        }
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
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = (requestCode == 1) ? photoUri : data.getData();
            if (imageUri != null) {
                handleImageUpload(imageUri);
            }
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

    private void updateImagePreview(Uri imageUri) {
        if (isAdded() && getActivity() != null) {
            Glide.with(requireContext())
                    .load(imageUri)
                    .into(profileImageEdit);
        }
    }

    public static class RotateTransformation extends BitmapTransformation {
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
        try {
            File imageFile = new File(imageUri.getPath()); // Get the image as a File
            long imageSizeInKB = checkImageSize(imageUri); // Get image size in KB
            File compressedFile = null;
            Uri uploadUri = imageUri;

            if (imageSizeInKB > 64) {
                compressedFile = compressImageFromUri(this.getContext(), imageUri); // Compress the image
                if (compressedFile != null) {
                    uploadUri = Uri.fromFile(compressedFile);
                    Log.d("COMPRESSION", "Image compressed and be rotated");
                    // Handle 90-degree rotation for compressed images
                }
            } else {
                Log.d("COMPRESSION", "no compression needed");
            }
            if (uploadUri != null) {
                if(compressedFile != null){
                    uploadUri = rotateImage(this.getContext(), uploadUri, 90); // rotate image by 90 degrees
                }

                String path = "mood_images/" + UUID.randomUUID().toString();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

                UploadTask uploadTask = storageRef.putFile(uploadUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        imageUrl = downloadUrl.toString();
                        Log.d("FirebaseStorage", "Image URL: " + imageUrl);
                        listener.onImageUploaded(imageUrl);
                    }).addOnFailureListener(exception -> {
                        Log.e("FirebaseStorage", "Failed to get download URL: " + exception.getMessage());
                        listener.onUploadFailed(exception);
                    });
                }).addOnFailureListener(exception -> {
                    Log.e("FirebaseStorage", "Upload failed: " + exception.getMessage());
                    listener.onUploadFailed(exception);
                });
            } else {
                Log.e("FirebaseStorage", "No image available for upload");
                listener.onUploadFailed(new Exception("No image available for upload"));
            }
        } catch (Exception e) {
            Log.e("FirebaseStorage", "Error during image processing", e);
            listener.onUploadFailed(e);
        }
    }


    private Uri rotateImage(Context context, Uri imageUri, float angle) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        File file = new File(context.getCacheDir(), "rotated_image.jpg");
        FileOutputStream fos = new FileOutputStream(file);
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();

        return Uri.fromFile(file);
    }
    private void saveChanges() {
        String name = fullName.getText().toString().trim();
        String biography = bio.getText().toString().trim();

        if (name.isEmpty() || biography.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = globalStorage.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Unable to retrieve current user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUserId)
                .update(
                        "fullName", name,
                        "bio", biography,
                        "profileImageUrl", imageUrl
                )
                .addOnSuccessListener(aVoid -> {
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating document", e);
                    Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
