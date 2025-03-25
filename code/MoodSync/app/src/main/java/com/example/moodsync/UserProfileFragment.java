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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button followButton;
    private TextView nameTextView, usernameTextView, followersCountTextView, followingCountTextView;
    private String currentUserId;
    private String selectedUserId;
    private ImageView profileImageView;
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
        profileImageView = view.findViewById(R.id.profile_image_edit);

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
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            nameTextView.setText(fullName);
                            usernameTextView.setText("@" + userName);
                            followersCountTextView.setText(String.valueOf(followerList != null ? followerList.size() : 0));
                            followingCountTextView.setText(String.valueOf(followingList != null ? followingList.size() : 0));

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadProfileImage(profileImageUrl);
                            }

                            checkFollowStatus();
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
        if (isAdded() && getActivity() != null) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(profileImageView);
        } else {
            Log.w("UserProfileFragment", "Fragment not attached, skipping image load");
        }
    }

    private void checkFollowStatus() {
        // First check if current user is already following the selected user
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> followingList = (List<String>) documentSnapshot.get("followingList");

                            if (followingList != null && followingList.contains(selectedUserId)) {
                                // User is already following this person
                                followButton.setText("Following");
                            } else {
                                // Check if there's a pending follow request
                                updateFollowButtonState();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UserProfile", "Error checking follow status", e);
                    }
                });
    }

    private void handleFollowRequest() {
        // First check if user is already following
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> followingList = (List<String>) documentSnapshot.get("followingList");

                            if (followingList != null && followingList.contains(selectedUserId)) {
                                // User is already following, handle unfollow
                                unfollowUser();
                            } else {
                                // Check pending requests
                                checkPendingRequestAndHandle();
                            }
                        }
                    }
                });
    }

    private void unfollowUser() {
        // Remove from current user's following list
        db.collection("users").document(currentUserId)
                .update("followingList", com.google.firebase.firestore.FieldValue.arrayRemove(selectedUserId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Remove from selected user's follower list
                        db.collection("users").document(selectedUserId)
                                .update("followerList", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                                        followButton.setText("Follow");
                                        loadUserProfile(selectedUserId); // Refresh counts
                                    }
                                });
                    }
                });
    }

    private void checkPendingRequestAndHandle() {
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
                                                followButton.setText("Cancel Request");
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
                                                followButton.setText("Follow");
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
