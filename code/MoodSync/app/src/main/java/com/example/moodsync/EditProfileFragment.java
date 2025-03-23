package com.example.moodsync;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ImageView profileImageEdit;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView locationTextView;
    private TextView bioTextView;
    private ImageView backButton;
    private MaterialButton editProfileButton;
    private GridView photosListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        // Initialize views
        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        locationTextView = view.findViewById(R.id.locationofuser);
        bioTextView = view.findViewById(R.id.bioofuser);
        backButton = view.findViewById(R.id.back_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        photosListView = view.findViewById(R.id.photos_listview);

        // Load dummy data
        loadDummyData();

        // Load photos in the ListView
        loadPhotosListView();

        // Set click listeners
        profileImageEdit.setOnClickListener(v -> changeProfileImage());

        editProfileButton.setOnClickListener(v -> saveProfile());

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadDummyData() {
        // Set default profile image from drawable
        profileImageEdit.setImageResource(R.drawable.arijitsingh);

        // Set dummy text data
        nameTextView.setText("John Doe");
        usernameTextView.setText("@" + "johndoe");
        locationTextView.setText("New York, USA");
        bioTextView.setText("Photographer | Travel Enthusiast | Coffee Lover\nCapturing moments and sharing stories through my lens. Always on the lookout for the next adventure.");
    }

    private void loadPhotosListView() {
        // Create a list of sample photos using arijitsingh drawable
        List<Map<String, Object>> photosList = new ArrayList<>();

        // Add multiple entries to simulate a photo gallery
        for (int i = 0; i < 6; i++) {
            Map<String, Object> photo = new HashMap<>();
            photo.put("image", R.drawable.arijitsingh);
            photosList.add(photo);
        }

        // Create an adapter for the ListView
        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                photosList,
                R.layout.photo_item,
                new String[]{"image"},
                new int[]{R.id.photo_image}
        );

        // Set the adapter to the ListView
        photosListView.setAdapter(adapter);

        // Add hover animation to grid items
        photosListView.setOnItemClickListener((parent, view, position, id) -> {
            showPostDetailDialog();
        });

        // Add touch listeners for hover effect
        photosListView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                int position = photosListView.pointToPosition((int) event.getX(), (int) event.getY());
                if (position >= 0) {
                    View itemView = getViewByPosition(position, photosListView);
                    if (itemView != null) {
                        animateHoverUp(itemView);
                    }
                }
            }
            return false;
        });
    }

    private View getViewByPosition(int position, GridView gridView) {
        final int firstListItemPosition = gridView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + gridView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition) {
            return null;
        } else {
            final int childIndex = position - firstListItemPosition;
            return gridView.getChildAt(childIndex);
        }
    }

    private void animateHoverUp(View view) {
        // Elevate view
        ObjectAnimator upAnimator = ObjectAnimator.ofFloat(view, "translationZ", 0f, 8f);
        upAnimator.setDuration(150);
        upAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // Move slightly upward
        ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f, -8f);
        moveAnimator.setDuration(150);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        upAnimator.start();
        moveAnimator.start();

        // Add listener to reset after animation
        view.postDelayed(() -> {
            ObjectAnimator downAnimator = ObjectAnimator.ofFloat(view, "translationZ", 8f, 0f);
            downAnimator.setDuration(150);
            downAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator resetMoveAnimator = ObjectAnimator.ofFloat(view, "translationY", -8f, 0f);
            resetMoveAnimator.setDuration(150);
            resetMoveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            downAnimator.start();
            resetMoveAnimator.start();
        }, 500); // Reset after 0.5 seconds
    }

    private void showPostDetailDialog() {
        // Create dialog
        Dialog dialog = new Dialog(requireContext());

        // Request feature must be called before setting content view
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_detail_dialog);

        // Set window attributes for bottom animation
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 1.0); // 95% of screen width
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        // Initialize views from the dialog layout
        ShapeableImageView profileImage = dialog.findViewById(R.id.profile_image_edit);
        TextView nameText = dialog.findViewById(R.id.name);
        TextView timeStampText = dialog.findViewById(R.id.time_stamp);
        TextView moodTextView = dialog.findViewById(R.id.mood_text_view);
        ImageView postImage = dialog.findViewById(R.id.post_image);
        TextView statusText = dialog.findViewById(R.id.status);
        TextView triggerTextView = dialog.findViewById(R.id.trigger_text_view);
        TextView likeCount = dialog.findViewById(R.id.like_count);
        TextView commentCount = dialog.findViewById(R.id.comment_count);

        // Set data
        profileImage.setImageResource(R.drawable.arijitsingh);
        nameText.setText(nameTextView.getText().toString());
        timeStampText.setText("2 hours ago");
        moodTextView.setText("Happy");
        postImage.setImageResource(R.drawable.arijitsingh);
        statusText.setText("This is a sample post caption that describes how I'm feeling today. #MoodSync");
        triggerTextView.setText("None");
        likeCount.setText("24");
        commentCount.setText("8");

        // Set click listeners for dialog buttons
        MaterialButton detailsButton = dialog.findViewById(R.id.details_button);
        detailsButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Details clicked", Toast.LENGTH_SHORT).show();
        });

        ImageButton likeButton = dialog.findViewById(R.id.like_button);
        likeButton.setOnClickListener(v -> {
            int currentLikes = Integer.parseInt(likeCount.getText().toString());
            likeCount.setText(String.valueOf(currentLikes + 1));
            Toast.makeText(requireContext(), "Liked!", Toast.LENGTH_SHORT).show();
        });

        ImageButton commentButton = dialog.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Comment clicked", Toast.LENGTH_SHORT).show();
        });

        ImageButton shareButton = dialog.findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show();
        });

        ImageButton bookmarkButton = dialog.findViewById(R.id.bookmark_button);
        bookmarkButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bookmarked!", Toast.LENGTH_SHORT).show();
        });

        // Show dialog
        dialog.show();
    }

    private void changeProfileImage() {
        // Just toggle between two drawable images for demonstration
        if (profileImageEdit.getTag() != null && (boolean) profileImageEdit.getTag()) {
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(false);
        } else {
            // Using the same drawable since we only have arijitsingh
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(true);
        }

        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();

        // Refresh the photos list to show the change
        loadPhotosListView();
    }

    private void saveProfile() {
        // Get text from fields
        String name = nameTextView.getText().toString().trim();
        String username = usernameTextView.getText().toString().trim();
        String location = locationTextView.getText().toString().trim();
        String bio = bioTextView.getText().toString().trim();

        // Validate inputs (basic validation)
        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Name and username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, you would save this data to a database or preferences
        // For this dummy implementation, just show a success message
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

        // Navigate back
        requireActivity().onBackPressed();
    }
}
