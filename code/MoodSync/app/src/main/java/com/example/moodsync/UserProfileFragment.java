package com.example.moodsync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button followButton;
    private TextView nameTextView, usernameTextView, followersCountTextView, followingCountTextView;
    private String currentUserId;
    private String selectedUserId;
    private ImageView profileImageView; // Use a better name
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        db = FirebaseFirestore.getInstance();

        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        followersCountTextView = view.findViewById(R.id.followers_count);
        followingCountTextView = view.findViewById(R.id.following_count);
        followButton = view.findViewById(R.id.follow_user);
        profileImageView = view.findViewById(R.id.profile_image_edit); // Initialize

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
                            List<String> followerList = (List<String>) documentSnapshot.get("followerList");
                            List<String> followingList = (List<String>) documentSnapshot.get("followingList");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl"); // Get the image URL

                            nameTextView.setText(fullName);
                            usernameTextView.setText("@" + userName);
                            followersCountTextView.setText(String.valueOf(followerList != null ? followerList.size() : 0));
                            followingCountTextView.setText(String.valueOf(followingList != null ? followingList.size() : 0));

                            // Load the image using Glide (or Picasso, etc.)
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadProfileImage(profileImageUrl);
                            }

                            updateFollowButtonState();
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

    private void loadProfileImage(String imageUrl) {
        // Use Glide to load the image.  Make sure Glide is added to your dependencies.
        if (isAdded() && getActivity() != null) { // Check if fragment is attached
            Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop() // Optional: Crop into a circle
                    .placeholder(R.drawable.ic_person_black_24dp) // Optional: Placeholder image
                    .into(profileImageView);
        } else {
            Log.w("UserProfileFragment", "Fragment not attached, skipping image load");
        }
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
}
