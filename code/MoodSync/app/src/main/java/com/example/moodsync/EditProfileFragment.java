package com.example.moodsync;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private MaterialButton pendingRequestsButton;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private TextView likesCountTextView;

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
        likesCountTextView = view.findViewById(R.id.likes_count);

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

        return view;
    }

    private void loadUserData() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            loadDummyData();
            return;
        }

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
                                .placeholder(R.drawable.ic_person_black_24dp)
                                .into(profileImageEdit);

                    } else {
                        loadDummyData();
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
