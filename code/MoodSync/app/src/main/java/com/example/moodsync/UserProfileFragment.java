package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button followButton;
    private String currentUserId;
    private String userIdToFollow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Assuming currentUserId and userIdToFollow are set somewhere else in your app
        // For demonstration, let's assume they are passed as arguments
        Bundle args = getArguments();
        if (args != null) {
            currentUserId = args.getString("currentUserId");
            userIdToFollow = args.getString("userIdToFollow");
        }

        // Find the follow button
        followButton = view.findViewById(R.id.follow_user);

        // Set up the follow button click listener
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFollowRequest();
            }
        });

        return view;
    }

    private void handleFollowRequest() {
        // Check if the user is already following
        checkFollowStatus(new FollowStatusCallback() {
            @Override
            public void isFollowing(boolean isFollowing) {
                if (isFollowing) {
                    // Unfollow logic
                    unfollowUser();
                } else {
                    // Follow logic
                    followUser();
                }
            }
        });
    }

    private void checkFollowStatus(FollowStatusCallback callback) {
        CollectionReference followRef = db.collection("followreq");
        followRef.whereEqualTo("followerId", currentUserId).whereEqualTo("followedId", userIdToFollow)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isFollowing = !task.getResult().isEmpty();
                            callback.isFollowing(isFollowing);
                        } else {
                            Toast.makeText(getActivity(), "Error checking follow status", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void followUser() {
        Map<String, Object> followRequest = new HashMap<>();
        followRequest.put("followerId", currentUserId);
        followRequest.put("followedId", userIdToFollow);

        db.collection("followreq").add(followRequest)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), "Followed successfully", Toast.LENGTH_SHORT).show();
                        // Update the button text or state here
                        followButton.setText("Unfollow");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to follow", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unfollowUser() {
        CollectionReference followRef = db.collection("followreq");
        followRef.whereEqualTo("followerId", currentUserId).whereEqualTo("followedId", userIdToFollow)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getActivity(), "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                                                // Update the button text or state here
                                                followButton.setText("Follow");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity(), "Failed to unfollow", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error unfollowing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private interface FollowStatusCallback {
        void isFollowing(boolean isFollowing);
    }
}
