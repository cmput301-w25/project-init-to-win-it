package com.example.moodsync;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that displays a user's profile, including their name, bio, location,
 * followers, following, and posts. It also allows the current user to follow or
 * unfollow the selected user.
 *
 * <p>
 * This fragment interacts with Firebase Firestore to fetch user data and mood events.
 * It includes functionality for sending follow requests, viewing posts, and handling
 * privacy settings for user profiles.
 *
 */
public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button followButton;
    private GridView photos_listview;
    private TextView nameTextView, usernameTextView, followersCountTextView, followingCountTextView;
    private String currentUserId;    //logged in user
    private String selectedUserId;   //selected user from search
    private ImageView profileImageView;
    private View view;
    private TextView locationTextView;
    private TextView bioTextView;
    private ImageView profileImageEdit;
    private ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        // Initialize back button
        backButton = view.findViewById(R.id.back_button);

        backButton.setOnClickListener(view1 -> {
            NavController navController = Navigation.findNavController(view1);
            navController.navigate(R.id.action_userProfileFragment_to_SecondFragment);
        });

        db = FirebaseFirestore.getInstance();

        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        locationTextView = view.findViewById(R.id.locationofuser);
        bioTextView = view.findViewById(R.id.bioofuser);
        followersCountTextView = view.findViewById(R.id.followers_count);
        followingCountTextView = view.findViewById(R.id.following_count);
        followButton = view.findViewById(R.id.follow_user);
        photos_listview = view.findViewById(R.id.photos_listview);

        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();

        Bundle args = getArguments();
        if (args != null) {
            selectedUserId = args.getString("selectedUserId");
            loadUserProfile(selectedUserId);
        }

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFollowRequest();
            }
        });

        return view;
    }

    /**
     * Loads the profile data of a user from Firestore and updates the UI components
     * with the retrieved information. This method fetches user details such as name,
     * username, profile image URL, follower count, following count, location, and bio.
     *
     * <p>
     * If the user exists in Firestore, their profile details are displayed. If the user
     * is private and not followed by the current user, a message indicating a private account
     * is shown instead of their posts.
     * </p>
     *
     * @param userId The unique ID of the user whose profile is being loaded.
     */
    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String userName = documentSnapshot.getString("userName");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            List<String> followerList = (List<String>) documentSnapshot.get("followerList");
                            List<String> followingList = (List<String>) documentSnapshot.get("followingList");

                            nameTextView.setText(fullName);
                            usernameTextView.setText("@" + userName);
                            followersCountTextView.setText(String.valueOf(followerList != null ? followerList.size() : 0));
                            followingCountTextView.setText(String.valueOf(followingList != null ? followingList.size() : 0));

                            locationTextView.setText(documentSnapshot.getString("location") != null ?
                                    documentSnapshot.getString("location") : "Location not set");

                            bioTextView.setText(documentSnapshot.getString("bio") != null ?
                                    documentSnapshot.getString("bio") : "No bio available");

                            if (profileImageUrl != null) {
                                Glide.with(UserProfileFragment.this) // Use the outer class's context
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_person_black_24dp)
                                        .into(profileImageEdit);
                            }
                                updateFollowButtonStateBasedOnFollowers();

                            // Check if the current user follows the selected user
                            if (followerList != null && followerList.contains(currentUserId)) {
                                fetchMoodEvents(true); // Fetch public posts
                            } else {
                                showPrivateAccountMessage();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error loading user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetches mood events for the selected user based on their privacy settings.
     *
     * <p>
     * This method queries Firestore for mood events associated with the selected user's ID.
     * If the user's account is public or followed by the current user, their posts are fetched
     * and displayed in a GridView.
     * </p>
     *
     * @param isPublic A boolean indicating whether to fetch public mood events or not.
     */
    private void fetchMoodEvents(boolean isPublic) {
        db.collection("mood_events")
                .whereEqualTo("id", selectedUserId)
                .whereEqualTo("public", isPublic)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Map<String, Object>> moodList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> moodData = new HashMap<>();
                            moodData.put("imageUrl", document.getString("imageUrl"));
                            moodData.put("description", document.getString("description"));
                            moodData.put("mood", document.getString("mood"));
                            moodData.put("trigger", document.getString("trigger"));

                            moodList.add(moodData);
                        }

                        loadPhotosListView(moodList);
                    }
                });
    }

    /**
     * Populates the GridView with mood events retrieved from Firestore. Each item in the GridView
     * represents an individual mood event containing details such as an image, description,
     * mood type, and trigger.
     *
     * @param moodList A list of maps containing mood event data retrieved from Firestore.
     */
    private void loadPhotosListView(List<Map<String, Object>> moodList) {
        MoodImageAdapter adapter = new MoodImageAdapter(requireContext(), moodList);
        photos_listview.setAdapter(adapter);

        photos_listview.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selectedMood = moodList.get(position);
            showPostDetailDialog(selectedMood); // Pass the selected mood data to the dialog
        });
    }

    /**
     * Displays a message indicating that the selected user's account is private. This method hides
     * the GridView containing posts and shows an alternative message in its place.
     */
    private void showPrivateAccountMessage() {
        photos_listview.setVisibility(View.GONE); // Hide the GridView
        View privateAccountMessage = view.findViewById(R.id.private_account_message); // Add this to your XML layout
        privateAccountMessage.setVisibility(View.VISIBLE);
    }


    /**
     * Displays a detailed dialog for a selected post. This dialog includes information about
     * the post such as its image, description, mood type, trigger, and comments count.
     *
     * <p>
     * The dialog is styled with animations and appears at the bottom of the screen. It allows
     * users to interact with post details or leave comments on the post.
     * </p>
     *
     * @param moodData A map containing details about the selected mood event (e.g., image URL,
     *                 description, mood type).
     */
    private void showPostDetailDialog(Map<String, Object> moodData) {
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
        TextView commentCount = dialog.findViewById(R.id.comment_count);


        // Set data from moodData map
        Glide.with(requireContext())
                .load(moodData.get("imageUrl"))
                .into(postImage);

        nameText.setText(selectedUserId);
        statusText.setText((String) moodData.get("description"));
        moodTextView.setText((String) moodData.get("mood"));
        triggerTextView.setText((String) moodData.get("trigger"));


        ImageButton commentButton = dialog.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Comment clicked", Toast.LENGTH_SHORT).show();
        });


        // Show dialog
        dialog.show();
    }

    /**
     * Handles sending or canceling a follow request for the selected user. If no follow request exists,
     * this method creates one; otherwise, it cancels an existing request by deleting it from Firestore.
     *
     * <p>
     * Updates the UI to reflect whether a follow request has been sent or canceled successfully.
     * </p>
     */
    private void handleFollowRequest() {
        db.collection("pendingFollowerRequests")
                .whereEqualTo("follower", currentUserId)
                .whereEqualTo("followee", selectedUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // No pending request, create one
                                Map<String, Object> request = new HashMap<>();
                                request.put("follower", currentUserId);
                                request.put("followee", selectedUserId);
                                db.collection("pendingFollowerRequests").add(request)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(getActivity(), "Follow request sent", Toast.LENGTH_SHORT).show();
                                                updateFollowButtonState();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity(), "Failed to send follow request", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Request already exists, cancel it
                                task.getResult().getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getActivity(), "Follow request cancelled", Toast.LENGTH_SHORT).show();
                                                updateFollowButtonState();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity(), "Failed to cancel follow request", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    /**
     * Updates the text displayed on the follow button based on whether there is a pending follow request
     * between the current user and the selected user. This method queries Firestore to check for pending requests.
     */
    private void updateFollowButtonState() {
        db.collection("pendingFollowerRequests")
                .whereEqualTo("follower", currentUserId)
                .whereEqualTo("followee", selectedUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                followButton.setText("Follow");
                            } else {
                                followButton.setText("Cancel Request");
                            }
                        }
                    }
                });
    }

    /**
     * Updates the state of the follow button based on whether the current user follows
     * the selected user. If they are already following, disables further interaction with
     * the button; otherwise, checks for pending follow requests to update its text accordingly.
     */
    private void updateFollowButtonStateBasedOnFollowers() {
        db.collection("users").document(selectedUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        List<String> followerList = (List<String>) documentSnapshot.get("followerList");
                        Log.d("damn me", "updateFollowButtonStateBasedOnFollowers: " + followerList);
                        if (followerList != null && followerList.contains(currentUserId)) {
                            followButton.setText("Following");
                            followButton.setEnabled(false); // Optional: Disable the button
                        } else {
                            updateFollowButtonState();
                            Log.d("DONT HAPPEN", "updateFollowButtonStateBasedOnFollowers: ");// Call the original method for pending requests
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error checking followers", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
