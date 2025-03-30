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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Just like the version from before,
 * but now with the subcollection approach for replies inside showCommentsDialog.
 */
public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button followButton;
    private GridView photos_listview;
    private TextView nameTextView, usernameTextView, followersCountTextView, followingCountTextView;
    private String currentUserId;
    private String selectedUserId;
    private View view;
    private ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(view1 -> {
            NavController navController = Navigation.findNavController(view1);
            navController.navigate(R.id.action_userProfileFragment_to_SecondFragment);
        });

        db = FirebaseFirestore.getInstance();

        nameTextView          = view.findViewById(R.id.nameofuser);
        usernameTextView      = view.findViewById(R.id.usernameofuser);
        followersCountTextView= view.findViewById(R.id.followers_count);
        followingCountTextView= view.findViewById(R.id.following_count);
        followButton          = view.findViewById(R.id.follow_user);
        photos_listview       = view.findViewById(R.id.photos_listview);

        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();

        Bundle args = getArguments();
        if (args != null) {
            selectedUserId = args.getString("selectedUserId");
            loadUserProfile(selectedUserId);
        }

        followButton.setOnClickListener(v -> handleFollowRequest());
        return view;
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get()
          .addOnSuccessListener(documentSnapshot -> {
              if (documentSnapshot.exists()) {
                  String fullName = documentSnapshot.getString("fullName");
                  String userName = documentSnapshot.getString("userName");
                  List<String> followerList = (List<String>) documentSnapshot.get("followerList");
                  List<String> followingList = (List<String>) documentSnapshot.get("followingList");

                  nameTextView.setText(fullName);
                  usernameTextView.setText("@" + userName);
                  followersCountTextView.setText(String.valueOf(
                          followerList != null ? followerList.size() : 0));
                  followingCountTextView.setText(String.valueOf(
                          followingList != null ? followingList.size() : 0));

                  updateFollowButtonStateBasedOnFollowers();

                  // If user is followed => show their public posts
                  if (followerList != null && followerList.contains(currentUserId)) {
                      fetchMoodEvents(true);
                  } else {
                      showPrivateAccountMessage();
                  }
              }
          })
          .addOnFailureListener(e -> {
              Toast.makeText(getActivity(), "Error loading user profile", Toast.LENGTH_SHORT).show();
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
                  for (DocumentSnapshot document : task.getResult()) {
                      Map<String, Object> moodData = new HashMap<>();
                      moodData.put("imageUrl", document.getString("imageUrl"));
                      moodData.put("description", document.getString("description"));
                      moodData.put("mood", document.getString("mood"));
                      moodData.put("trigger", document.getString("trigger"));
                      moodData.put("docId", document.getId());
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
            showPostDetailDialog(selectedMood);
        });
    }

    private void showPrivateAccountMessage() {
        photos_listview.setVisibility(View.GONE);
        View privateAccountMessage = view.findViewById(R.id.private_account_message);
        privateAccountMessage.setVisibility(View.VISIBLE);
    }

    private void showPostDetailDialog(Map<String, Object> moodData) {
        Dialog dialog = new Dialog(requireContext());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_detail_dialog);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.width   = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height  = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        ShapeableImageView profileImage = dialog.findViewById(R.id.profile_image_edit);
        TextView nameText      = dialog.findViewById(R.id.name);
        TextView timeStampText = dialog.findViewById(R.id.time_stamp);
        TextView moodTextView  = dialog.findViewById(R.id.mood_text_view);
        ImageView postImage    = dialog.findViewById(R.id.post_image);
        TextView statusText    = dialog.findViewById(R.id.status);
        TextView triggerTextView = dialog.findViewById(R.id.trigger_text_view);
        TextView likeCount     = dialog.findViewById(R.id.like_count);
        TextView commentCount  = dialog.findViewById(R.id.comment_count);

        String docId = (String) moodData.get("docId");
        Glide.with(requireContext())
             .load(moodData.get("imageUrl"))
             .into(postImage);

        nameText.setText(selectedUserId);
        statusText.setText((String) moodData.get("description"));
        moodTextView.setText((String) moodData.get("mood"));
        triggerTextView.setText((String) moodData.get("trigger"));

        if (docId != null && !docId.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("mood_events")
                    .document(docId)
                    .collection("comments")
                    .get()
                    .addOnSuccessListener(snap -> {
                        commentCount.setText(String.valueOf(snap.size()));
                    })
                    .addOnFailureListener(e -> {
                        commentCount.setText("0");
                    });
        }

        MaterialButton detailsButton = dialog.findViewById(R.id.details_button);
        detailsButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Details clicked", Toast.LENGTH_SHORT).show());

        ImageButton likeButton = dialog.findViewById(R.id.like_button);
        likeButton.setOnClickListener(v -> {
            int cLikes = Integer.parseInt(likeCount.getText().toString());
            likeCount.setText(String.valueOf(cLikes + 1));
            Toast.makeText(requireContext(), "Liked!", Toast.LENGTH_SHORT).show();
        });

        ImageButton commentButton = dialog.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> {
            if (docId != null && !docId.isEmpty()) {
                showCommentsDialog(docId, commentCount);
            } else {
                Toast.makeText(requireContext(), "Unable to open comments", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton shareButton = dialog.findViewById(R.id.share_button);
        shareButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show());

        ImageButton bookmarkButton = dialog.findViewById(R.id.bookmark_button);
        bookmarkButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Bookmarked!", Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    private void showCommentsDialog(String docId, TextView commentCountTextView) {
        Dialog dialog = new Dialog(requireContext(), R.style.BottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_comments);

        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.gravity = Gravity.BOTTOM;
            lp.width   = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height  = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        RecyclerView commentRecyclerView  = dialog.findViewById(R.id.comment_recycler_view);
        TextInputEditText commentInput    = dialog.findViewById(R.id.comment_input_edittext);
        Button sendButton                 = dialog.findViewById(R.id.comment_send_button);

        // build top-level adapter, pass docId
        CommentAdapter adapter = new CommentAdapter(null, getContext(), docId);
        commentRecyclerView.setAdapter(adapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseFirestore.getInstance()
                .collection("mood_events")
                .document(docId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Comment> topList = new ArrayList<>();
                    for (DocumentSnapshot ds : snap) {
                        Comment c = ds.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(ds.getId());
                            topList.add(c);
                        }
                    }
                    adapter.setCommentList(topList);
                    commentCountTextView.setText(String.valueOf(topList.size()));
                });

        final Comment[] replyingTo = {null};

        adapter.setOnReplyClickListener((topLevelComment, position) -> {
            replyingTo[0] = topLevelComment;
            if (topLevelComment == null) {
                commentInput.setHint("Write a comment...");
            } else {
                commentInput.setHint("Replying to " + topLevelComment.getUserId());
            }
        });

        sendButton.setOnClickListener(v -> {
            String text = (commentInput.getText() != null)
                        ? commentInput.getText().toString().trim()
                        : "";
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            MyApplication myApp = (MyApplication) requireActivity().getApplication();
            String currentUser   = myApp.getLoggedInUsername();

            Comment newC = new Comment();
            newC.setUserId(currentUser);
            newC.setText(text);
            newC.setTimestamp(System.currentTimeMillis());

            if (replyingTo[0] == null) {
                // top-level
                FirebaseFirestore.getInstance()
                        .collection("mood_events")
                        .document(docId)
                        .collection("comments")
                        .add(newC)
                        .addOnSuccessListener(ref -> {
                            commentInput.setText("");
                            commentInput.setHint("Write a comment...");
                            reloadTopLevelComments(docId, adapter, commentCountTextView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // subcollection
                String pid = replyingTo[0].getCommentId();
                FirebaseFirestore.getInstance()
                        .collection("mood_events")
                        .document(docId)
                        .collection("comments")
                        .document(pid)
                        .collection("replies")
                        .add(newC)
                        .addOnSuccessListener(ref -> {
                            replyingTo[0] = null;
                            commentInput.setText("");
                            commentInput.setHint("Write a comment...");
                            reloadTopLevelComments(docId, adapter, commentCountTextView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to post reply", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }

    private void reloadTopLevelComments(String docId,
                                        CommentAdapter adapter,
                                        TextView countTextView) {
        FirebaseFirestore.getInstance()
                .collection("mood_events")
                .document(docId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Comment> updated = new ArrayList<>();
                    for (DocumentSnapshot ds : snap) {
                        Comment c = ds.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(ds.getId());
                            updated.add(c);
                        }
                    }
                    adapter.setCommentList(updated);
                    countTextView.setText(String.valueOf(updated.size()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to reload comments", Toast.LENGTH_SHORT).show();
                });
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
                        Log.d("fuck me", "updateFollowButtonStateBasedOnFollowers: " + followerList);
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
