package com.example.moodsync;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private TextView likesCountTextView;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        locationTextView = view.findViewById(R.id.locationofuser);
        bioTextView = view.findViewById(R.id.bioofuser);
        backButton = view.findViewById(R.id.back_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        photosListView = view.findViewById(R.id.photos_listview);

        followersCountTextView = view.findViewById(R.id.followers_count);
        followingCountTextView = view.findViewById(R.id.following_count);
        likesCountTextView = view.findViewById(R.id.likes_count);


        loadUserData();
        loadPhotosListView();


        profileImageEdit.setOnClickListener(v -> changeProfileImage());
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_editProfileFragment_to_editProfileActivity);
            }
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadUserData() {
        // get logged in username from our MyApplication class
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        String loggedInUsername = myApp.getLoggedInUsername();

        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            loadDummyData();
            return;
        }

        // query firestore for the user data
        db.collection("users")
                .whereEqualTo("userName", loggedInUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String fullName = document.getString("fullName");
                        String username = document.getString("userName");

                        List<String> followerList = (List<String>) document.get("followerList");
                        List<String> followingList = (List<String>) document.get("followingList");

                        nameTextView.setText(fullName);
                        usernameTextView.setText("@" + username);


                        followersCountTextView.setText(followerList != null ? String.valueOf(followerList.size()) : "0");
                        followingCountTextView.setText(followingList != null ? String.valueOf(followingList.size()) : "0");

                        locationTextView.setText(document.getString("location") != null ?
                                document.getString("location") : "Location not set");

                        bioTextView.setText(document.getString("bio") != null ?
                                document.getString("bio") : "No bio available");

                        Glide.with(this)
                                .load(document.getString("profileImageUrl"))
                                .circleCrop()
                                .transform(new EditProfileActivity.RotateTransformation(90))
                                .placeholder(R.drawable.ic_person_black_24dp)
                                .into(profileImageEdit);

                    } else {
                        loadDummyData();
                        Log.e("EditProfileFragment", "Error getting user data: ", task.getException());
                    }
                });
    }

    private void loadDummyData() {
        // dummy shit for profile pic and texts
        profileImageEdit.setImageResource(R.drawable.arijitsingh);
        nameTextView.setText("John Doe");
        usernameTextView.setText("@" + "johndoe");
        locationTextView.setText("New York, USA");
        bioTextView.setText("Photographer | Travel Enthusiast | Coffee Lover\nCapturing moments and sharing stories through my lens. Always on the lookout for the next adventure.");

        if (followersCountTextView != null) followersCountTextView.setText("0");
        if (followingCountTextView != null) followingCountTextView.setText("0");
        if (likesCountTextView != null) likesCountTextView.setText("0");
    }

    private void loadPhotosListView() {
        List<Map<String, Object>> photosList = new ArrayList<>();

        // add sample photos, just for show
        for (int i = 0; i < 6; i++) {
            Map<String, Object> photo = new HashMap<>();
            photo.put("image", R.drawable.arijitsingh);
            photosList.add(photo);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                photosList,
                R.layout.photo_item,
                new String[]{"image"},
                new int[]{R.id.photo_image}
        );

        photosListView.setAdapter(adapter);

        photosListView.setOnItemClickListener((parent, view, position, id) -> {
            showPostDetailDialog();
        });

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
        // animate the view like it's on a sugar rush
        ObjectAnimator upAnimator = ObjectAnimator.ofFloat(view, "translationZ", 0f, 8f); // init elevation
        upAnimator.setDuration(150);
        upAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f, -8f); // move it up a bit
        moveAnimator.setDuration(150);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        upAnimator.start();
        moveAnimator.start();

        view.postDelayed(() -> {
            ObjectAnimator downAnimator = ObjectAnimator.ofFloat(view, "translationZ", 8f, 0f); // bring it back down
            downAnimator.setDuration(150);
            downAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator resetMoveAnimator = ObjectAnimator.ofFloat(view, "translationY", -8f, 0f); // reset position
            resetMoveAnimator.setDuration(150);
            resetMoveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            downAnimator.start();
            resetMoveAnimator.start();
        }, 500);
    }

    private void showPostDetailDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_detail_dialog);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 1.0);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        ShapeableImageView profileImage = dialog.findViewById(R.id.profile_image_edit);
        TextView nameText = dialog.findViewById(R.id.name);
        TextView timeStampText = dialog.findViewById(R.id.time_stamp);
        TextView moodTextView = dialog.findViewById(R.id.mood_text_view);
        ImageView postImage = dialog.findViewById(R.id.post_image);
        TextView statusText = dialog.findViewById(R.id.status);
        TextView triggerTextView = dialog.findViewById(R.id.trigger_text_view);
        TextView likeCount = dialog.findViewById(R.id.like_count);
        TextView commentCount = dialog.findViewById(R.id.comment_count);

        profileImage.setImageResource(R.drawable.arijitsingh);
        nameText.setText(nameTextView.getText().toString());
        timeStampText.setText("2 hours ago");
        moodTextView.setText("Happy");
        postImage.setImageResource(R.drawable.arijitsingh);
        statusText.setText("This is a sample post caption that describes how I'm feeling today. #MoodSync");
        triggerTextView.setText("None");
        likeCount.setText("24");
        commentCount.setText("8");

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

        dialog.show();
    }

    private void changeProfileImage() {
        // toggle profile image for demo, shit simple
        if (profileImageEdit.getTag() != null && (boolean) profileImageEdit.getTag()) {
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(false);
        } else {
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(true);
        }

        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
        loadPhotosListView();
    }

    private void saveProfile() {
        // get and trim the input fields, no bullshit
        String name = nameTextView.getText().toString().trim();
        String username = usernameTextView.getText().toString().trim();
        // if the username starts with @, chop it off
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        String location = locationTextView.getText().toString().trim();
        String bio = bioTextView.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Name and username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // get logged in username from our app context
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        String loggedInUsername = myApp.getLoggedInUsername();

        // create a new final variable for username to satisfy lambda capture rules
        final String finalUsername = username;

        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            db.collection("users")
                    .whereEqualTo("userName", loggedInUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String docId = task.getResult().getDocuments().get(0).getId();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("fullName", name);
                            updates.put("userName", finalUsername);
                            updates.put("location", location);
                            updates.put("bio", bio);

                            db.collection("users").document(docId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                        // update the logged in username if it changed
                                        if (!finalUsername.equals(loggedInUsername)) {
                                            myApp.setLoggedInUsername(finalUsername);
                                        }

                                        requireActivity().onBackPressed();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }
}
