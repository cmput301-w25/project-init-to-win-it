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

    private void loadPhotosListView(List<Map<String, Object>> moodList) {
        MoodImageAdapter adapter = new MoodImageAdapter(requireContext(), moodList);
        photos_listview.setAdapter(adapter);

        photos_listview.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selectedMood = moodList.get(position);
            showPostDetailDialog(selectedMood); // Pass the selected mood data to the dialog
        });
    }

    private void showPrivateAccountMessage() {
        photos_listview.setVisibility(View.GONE); // Hide the GridView
        View privateAccountMessage = view.findViewById(R.id.private_account_message); // Add this to your XML layout
        privateAccountMessage.setVisibility(View.VISIBLE);
    }

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
