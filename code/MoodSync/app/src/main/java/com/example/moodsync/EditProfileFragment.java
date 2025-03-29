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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
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
    private GridView photosListView;
    LocalStorage globalStorage = LocalStorage.getInstance();
    private MaterialButton editProfileButton;
    private MaterialButton pendingRequestsButton;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private TextView likesCountTextView;

    private TabLayout tabs;

    private FirebaseFirestore db;
    private String loggedInUsername;
    private List<Map<String, String>> pendingRequests = new ArrayList<>();

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
        pendingRequestsButton = view.findViewById(R.id.pending_requests_button);
        followersCountTextView = view.findViewById(R.id.followers_count);
        followingCountTextView = view.findViewById(R.id.following_count);

        photosListView = view.findViewById(R.id.photos_listview);
        tabs = view.findViewById(R.id.tabs);

        // Get logged in username
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        loggedInUsername = myApp.getLoggedInUsername();

        loadUserData();
        fetchPendingRequests();


        editProfileButton.setOnClickListener(view1 -> {
            NavController navController = Navigation.findNavController(view1);
            navController.navigate(R.id.action_editProfileFragment_to_editProfileActivity);
        });

        pendingRequestsButton.setOnClickListener(v -> showPendingRequestsDialog());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Set up tab listener
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals("Public")) {
                    fetchMoodEvents(true); // Fetch public posts
                } else {
                    fetchMoodEvents(false); // Fetch private posts
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Load initial data (default to Public tab)
        fetchMoodEvents(true);

        return view;
    }
    private void fetchMoodEvents(boolean isPublic) {
        ArrayList<MoodEvent> tempList = globalStorage.getMoodsForCurrentUser(globalStorage.getCurrentUser(), isPublic);
        List<Map<String, Object>> moodList = new ArrayList<>();
        Map<String, Object> moodData = new HashMap<>();
        if (!tempList.isEmpty()) {
            for (int i = 0; i < tempList.size(); i++) {
                moodData.put("imageUrl", tempList.get(i).getImageUrl());
                moodData.put("description", tempList.get(i).getDescription());
                moodData.put("mood", tempList.get(i).getMood());
                moodData.put("trigger", tempList.get(i).getTrigger());
                moodList.add(moodData);
            }
            loadPhotosListView(moodList);
        }
    }
    private void loadPhotosListView(List<Map<String, Object>> moodList) {
        // Create a custom adapter for the GridView
        MoodImageAdapter adapter = new MoodImageAdapter(getContext(), moodList);
        photosListView.setAdapter(adapter);
        // Add click listener to show details of a mood when clicked
        photosListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selectedMood = moodList.get(position);
            showPostDetailDialog(selectedMood); // Pass the selected mood data to the dialog
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
        TextView likeCount = dialog.findViewById(R.id.like_count);
        TextView commentCount = dialog.findViewById(R.id.comment_count);

        // Set data from moodData map
        Glide.with(requireContext())
                .load(moodData.get("imageUrl"))
                .into(postImage);

        nameText.setText(loggedInUsername);
        statusText.setText((String) moodData.get("description"));
        moodTextView.setText((String) moodData.get("mood"));
        triggerTextView.setText((String) moodData.get("trigger"));

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

    private void loadUserData() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            loadDummyData();
            return;
        }
        nameTextView.setText(globalStorage.getCurrentUser().getName());
        usernameTextView.setText("@" + globalStorage.getCurrentUser().getUsername());
        followersCountTextView.setText(globalStorage.getCurrentUser().getFollowerList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowerList().size()) : "0");
        followingCountTextView.setText(globalStorage.getCurrentUser().getFollowingList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowingList().size()) : "0");
        locationTextView.setText(globalStorage.getCurrentUser().getLocation() != null ?
                globalStorage.getCurrentUser().getLocation() : "Location not set");

        bioTextView.setText(globalStorage.getCurrentUser().getBio() != null ?
                globalStorage.getCurrentUser().getBio() : "No bio available");
        Glide.with(this)
                .load(globalStorage.getPfpUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(profileImageEdit);

        db.collection("users")
                .whereEqualTo("userName", loggedInUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String fullName = document.getString("fullName");
                        String username = document.getString("userName");
                        String location = document.getString("location");
                        String bio = document.getString("bio");

                        List<String> followerList = (List<String>) document.get("followerList");
                        List<String> followingList = (List<String>) document.get("followingList");

                        globalStorage.getCurrentUser().setName(fullName);
                        globalStorage.getCurrentUser().setUsername(username);
                        globalStorage.getCurrentUser().setFollowerList((ArrayList<String>) followerList);
                        globalStorage.getCurrentUser().setFollowingList((ArrayList<String>) followingList);
                        globalStorage.getCurrentUser().setLocation(location);
                        globalStorage.getCurrentUser().setBio(bio);
                        globalStorage.getCurrentUser().setPfpUrl(document.getString("profileImageUrl"));

                        nameTextView.setText(globalStorage.getCurrentUser().getName());
                        usernameTextView.setText("@" + globalStorage.getCurrentUser().getUsername());
                        followersCountTextView.setText(globalStorage.getCurrentUser().getFollowerList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowerList().size()) : "0");
                        followingCountTextView.setText(globalStorage.getCurrentUser().getFollowingList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowingList().size()) : "0");

                        locationTextView.setText(globalStorage.getCurrentUser().getLocation() != null ?
                                globalStorage.getCurrentUser().getLocation() : "Location not set");

                        bioTextView.setText(globalStorage.getCurrentUser().getBio() != null ?
                                globalStorage.getCurrentUser().getBio() : "No bio available");

                        Glide.with(this)
                                .load(globalStorage.getCurrentUser().getPfpUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_person_black_24dp)
                                .into(profileImageEdit);

                    } else {
                        nameTextView.setText(globalStorage.getCurrentUser().getName());
                        usernameTextView.setText("@" + globalStorage.getCurrentUser().getUsername());
                        followersCountTextView.setText(globalStorage.getCurrentUser().getFollowerList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowerList().size()) : "0");
                        followingCountTextView.setText(globalStorage.getCurrentUser().getFollowingList() != null ? String.valueOf(globalStorage.getCurrentUser().getFollowingList().size()) : "0");
                        locationTextView.setText(globalStorage.getCurrentUser().getLocation() != null ?
                                globalStorage.getCurrentUser().getLocation() : "Location not set");

                        bioTextView.setText(globalStorage.getCurrentUser().getBio() != null ?
                                globalStorage.getCurrentUser().getBio() : "No bio available");
                        Glide.with(this)
                                .load(globalStorage.getPfpUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_person_black_24dp)
                                .into(profileImageEdit);

                        Log.e("EditProfileFragment", "Error getting user data: ", task.getException());
                    }
                });
    }

    private void fetchPendingRequests() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            pendingRequestsButton.setText("0");
            return;
        }

        pendingRequests.clear();
        db.collection("pendingFollowerRequests")
                .whereEqualTo("followee", loggedInUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        pendingRequestsButton.setText(String.valueOf(count));

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> request = new HashMap<>();
                            request.put("id", document.getId());
                            request.put("follower", document.getString("follower"));
                            request.put("followee", document.getString("followee"));
                            pendingRequests.add(request);
                        }
                    } else {
                        Log.e("EditProfileFragment", "Error getting pending requests: ", task.getException());
                        pendingRequestsButton.setText("0");
                    }
                });
    }

    private void showPendingRequestsDialog() {
        if (pendingRequests.isEmpty()) {
            Toast.makeText(requireContext(), "No pending follow requests", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pending_requests);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView requestCountText = dialog.findViewById(R.id.request_count_text);
        RecyclerView requestsRecyclerView = dialog.findViewById(R.id.requests_recycler_view);
        MaterialButton acceptAllButton = dialog.findViewById(R.id.accept_all_button);
        MaterialButton declineAllButton = dialog.findViewById(R.id.decline_all_button);
        ImageButton closeButton = dialog.findViewById(R.id.close_button);

        requestCountText.setText("You have " + pendingRequests.size() + " pending follow requests");


        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        PendingRequestsAdapter adapter = new PendingRequestsAdapter(pendingRequests);
        requestsRecyclerView.setAdapter(adapter);

        acceptAllButton.setOnClickListener(v -> {
            acceptAllRequests();
            dialog.dismiss();
        });

        declineAllButton.setOnClickListener(v -> {
            declineAllRequests();
            dialog.dismiss();
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void acceptAllRequests() {
        for (Map<String, String> request : pendingRequests) {
            String requestId = request.get("id");
            String follower = request.get("follower");

            db.collection("users")
                    .whereEqualTo("userName", loggedInUsername)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String userId = userDoc.getId();

                            List<String> followerList = (List<String>) userDoc.get("followerList");
                            if (followerList == null) {
                                followerList = new ArrayList<>();
                            }

                            if (!followerList.contains(follower)) {
                                followerList.add(follower);

                                db.collection("users").document(userId)
                                        .update("followerList", followerList)
                                        .addOnSuccessListener(aVoid -> {
                                            // Delete the request
                                            db.collection("pendingFollowerRequests").document(requestId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Log.d("EditProfileFragment", "Request accepted and deleted");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("EditProfileFragment", "Error deleting request", e);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("EditProfileFragment", "Error updating follower list", e);
                                        });
                            }
                        }
                    });

            // Update follower's following list
            db.collection("users")
                    .whereEqualTo("userName", follower)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot followerDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String followerId = followerDoc.getId();

                            List<String> followingList = (List<String>) followerDoc.get("followingList");
                            if (followingList == null) {
                                followingList = new ArrayList<>();
                            }

                            if (!followingList.contains(loggedInUsername)) {
                                followingList.add(loggedInUsername);

                                db.collection("users").document(followerId)
                                        .update("followingList", followingList)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("EditProfileFragment", "Follower's following list updated");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("EditProfileFragment", "Error updating follower's following list", e);
                                        });
                            }
                        }
                    });
        }

        Toast.makeText(requireContext(), "All requests accepted", Toast.LENGTH_SHORT).show();
        pendingRequests.clear();
        pendingRequestsButton.setText("0");
        loadUserData();
    }

    private void declineAllRequests() {
        for (Map<String, String> request : pendingRequests) {
            String requestId = request.get("id");

            db.collection("pendingFollowerRequests").document(requestId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EditProfileFragment", "Request declined and deleted");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProfileFragment", "Error deleting request", e);
                    });
        }

        Toast.makeText(requireContext(), "All requests declined", Toast.LENGTH_SHORT).show();
        pendingRequests.clear();
        pendingRequestsButton.setText("0");
    }

    private void loadDummyData() {
        profileImageEdit.setImageResource(R.drawable.arijitsingh);
        nameTextView.setText("John Doe");
        usernameTextView.setText("@johndoe");
        locationTextView.setText("New York, USA");
        bioTextView.setText("Photographer | Travel Enthusiast | Coffee Lover\nCapturing moments and sharing stories through my lens. Always on the lookout for the next adventure.");

        if (followersCountTextView != null) followersCountTextView.setText("0");
        if (followingCountTextView != null) followingCountTextView.setText("0");
        if (likesCountTextView != null) likesCountTextView.setText("0");
    }


    private class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder> {
        private List<Map<String, String>> requests;

        public PendingRequestsAdapter(List<Map<String, String>> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, String> request = requests.get(position);
            String follower = request.get("follower");

            db.collection("users")
                    .whereEqualTo("userName", follower)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String fullName = userDoc.getString("fullName");

                            holder.userName.setText(fullName != null ? fullName : follower);
                            holder.username.setText("@" + follower);

                            String profileImageUrl = userDoc.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext())
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_person_black_24dp)
                                        .into(holder.userImage);
                            } else {
                                holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
                            }
                        } else {
                            holder.userName.setText(follower);
                            holder.username.setText("@" + follower);
                            holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.userName.setText(follower);
                        holder.username.setText("@" + follower);
                        holder.userImage.setImageResource(R.drawable.ic_person_black_24dp);
                    });

            holder.acceptButton.setOnClickListener(v -> {
                acceptRequest(position);
            });

            holder.declineButton.setOnClickListener(v -> {
                declineRequest(position);
            });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        private void acceptRequest(int position) {
            Map<String, String> request = requests.get(position);
            String requestId = request.get("id");
            String follower = request.get("follower");

            db.collection("users")
                    .whereEqualTo("userName", loggedInUsername)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String userId = userDoc.getId();

                            List<String> followerList = (List<String>) userDoc.get("followerList");
                            if (followerList == null) {
                                followerList = new ArrayList<>();
                            }

                            if (!followerList.contains(follower)) {
                                followerList.add(follower);

                                db.collection("users").document(userId)
                                        .update("followerList", followerList)
                                        .addOnSuccessListener(aVoid -> {
                                            // Delete the request
                                            db.collection("pendingFollowerRequests").document(requestId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        requests.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(position, requests.size());
                                                        pendingRequestsButton.setText(String.valueOf(requests.size()));
                                                        Toast.makeText(requireContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                                                        loadUserData();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("EditProfileFragment", "Error deleting request", e);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("EditProfileFragment", "Error updating follower list", e);
                                        });
                            }
                        }
                    });
            db.collection("users")
                    .whereEqualTo("userName", follower)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot followerDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String followerId = followerDoc.getId();

                            List<String> followingList = (List<String>) followerDoc.get("followingList");
                            if (followingList == null) {
                                followingList = new ArrayList<>();
                            }

                            if (!followingList.contains(loggedInUsername)) {
                                followingList.add(loggedInUsername);

                                db.collection("users").document(followerId)
                                        .update("followingList", followingList)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("EditProfileFragment", "follower's following list updated, bro");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("EditProfileFragment", "error updating follower's following list", e);
                                        });
                            }
                        }
                    });
        }
        private void declineRequest(int position) {
            Map<String, String> request = requests.get(position);
            String requestId = request.get("id");

            db.collection("pendingFollowerRequests").document(requestId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        requests.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, requests.size());
                        pendingRequestsButton.setText(String.valueOf(requests.size()));
                        Toast.makeText(requireContext(), "Request declined", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProfileFragment", "error deleting request", e);
                    });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView userName;
            TextView username;
            ImageButton acceptButton;
            ImageButton declineButton;
            LinearLayout buttonsLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userImage = itemView.findViewById(R.id.user_image);
                userName = itemView.findViewById(R.id.user_name);
                username = itemView.findViewById(R.id.username);
                acceptButton = itemView.findViewById(R.id.accept_button);
                declineButton = itemView.findViewById(R.id.decline_button);
                buttonsLayout = itemView.findViewById(R.id.buttons_layout);}}}}
