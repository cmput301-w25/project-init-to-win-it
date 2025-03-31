package com.example.moodsync;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
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
import android.widget.Button;
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
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for editing and managing the user's profile.
 * Features include updating profile details, viewing stats, managing pending requests,
 * displaying photos, and playing audio files.
 *
 * Key Components:
 * - Profile Image, Name, Username, Bio: Editable user details.
 * - Photos Grid: Displays user-uploaded photos.
 * - Pending Requests: View/manage follow requests.
 * - Followers, Following, Likes Count: Profile statistics.
 * - Media Player: Play/pause audio files.
 */
public class EditProfileFragment extends Fragment {

    private ImageView profileImageEdit;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView bioTextView;
    private GridView photosListView;
    private MaterialButton editProfileButton;
    private TextView pendingRequestsButton;

    private MaterialCardView pendingRequestView;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private TextView likesCountTextView;

    private TabLayout tabs;
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private String currentSongUrl;

    private FirebaseFirestore db;
    private String loggedInUsername;
    private List<Map<String, String>> pendingRequests = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        bioTextView = view.findViewById(R.id.bioofuser);

        editProfileButton = view.findViewById(R.id.edit_profile_button);
        pendingRequestsButton = view.findViewById(R.id.pending_button);
        pendingRequestView = view.findViewById(R.id.pending_button_request);
        followersCountTextView = view.findViewById(R.id.followers_count);
        followingCountTextView = view.findViewById(R.id.following_count);

        photosListView = view.findViewById(R.id.photos_listview);
        tabs           = view.findViewById(R.id.tabs);

        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        loggedInUsername = myApp.getLoggedInUsername();


        loadUserData();
        fetchPendingRequests();

        pendingRequestView.setOnClickListener(v -> showPendingRequestsDialog());

        editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_activity, null, navOptions);
        });


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if ("Public".equals(tab.getText().toString())) {
                    fetchMoodEvents(true);
                } else {
                    fetchMoodEvents(false);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        view.findViewById(R.id.home_button).setOnClickListener(v -> {
            // get the NavController
            NavController navController = Navigation.findNavController(v);

            // navigate with custom animations
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();

            // perform navigation
            navController.navigate(R.id.action_editProfileFragment_to_SecondFragment, null, navOptions);
        });

        view.findViewById(R.id.map_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_mapsActivity, null , navOptions);
        });

        view.findViewById(R.id.add_circle_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_addMoodActivityFragment, null , navOptions);
        });


        view.findViewById(R.id.history_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_in_right)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_moodHistoryFragment , null , navOptions);
        });
        view.findViewById(R.id.diary_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_JournalFragment,  null , navOptions);
        });

        view.findViewById(R.id.logout_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_editProfileFragment_to_loginfragment,  null , navOptions);
        });

        fetchMoodEvents(true);
        return view;
    }

    /**
     * Fetches mood events from local storage and prepares them for display.
     * Filters events by privacy status (public/private) and converts them into
     * a format suitable for the photos grid view adapter.
     *
     * @param isPublic If true, fetches public events; otherwise fetches private events
     */
    private void fetchMoodEvents(boolean isPublic) {
        db.collection("mood_events")
                .whereEqualTo("id", loggedInUsername)
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
                            moodData.put("docId", document.getId());
                            moodData.put("songUrl", document.getString("songUrl"));
                            moodData.put("date", document.getLong("date"));
                            moodList.add(moodData);
                        }
                        loadPhotosListView(moodList);
                    }
                });
    }


    /**
     * Configures the photos grid view with mood event data.
     * - Displays events in reverse chronological order (newest first)
     * - Sets click listeners to show detailed view of individual mood posts
     *
     * @param moodList Processed mood data containing image URLs, descriptions,
     *                 moods, and triggers for display in the grid
     */
    private void loadPhotosListView(List<Map<String, Object>> moodList) {
        Collections.reverse(moodList);
        MoodImageAdapter adapter = new MoodImageAdapter(requireContext(), moodList);
        photosListView.setAdapter(adapter);

        photosListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selectedMood = moodList.get(position);
            showPostDetailDialog(selectedMood);
        });
    }


    /**
     * Creates a hover animation effect for a view by elevating it and moving it upward,
     * then resets to original position after 0.5 seconds.
     *
     * @param view The UI element to animate (typically a button or interactive component)
     */
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

    /**
     * Toggles audio playback state between play/pause.
     * - Plays current song if available and paused
     * - Pauses if currently playing
     * - Shows toast if no song is available
     */
    private void togglePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pausePlayback();
        } else if (currentSongUrl != null && !currentSongUrl.isEmpty()) {
            playSong(currentSongUrl);
        } else {
            Toast.makeText(requireContext(), "No song available to play", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Pauses media playback and updates play button visual state.
     * Only affects playback if currently active.
     */
    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setImageResource(R.drawable.sound_up);
        }
    }

    /**
     * Plays an audio file from the specified URL.
     * - Initializes or resets the MediaPlayer.
     * - Handles playback preparation, start, and completion.
     * - Updates the play button's visual state during playback.
     * - Displays error messages if playback fails.
     *
     * @param songUrl The URL of the song to play. If null or empty, shows a toast message.
     */
    private void playSong(String songUrl) {
        if (songUrl == null || songUrl.isEmpty()) {
            Toast.makeText(requireContext(), "No song URL available", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSongUrl = songUrl;

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();

            // Show loading indicator or change button state here if needed

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (playButton != null) {
                    playButton.setImageResource(R.drawable.sound_up);
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                if (playButton != null) {
                    playButton.setImageResource(R.drawable.sound_down);
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT).show();
                if (playButton != null) {
                    playButton.setImageResource(R.drawable.sound_down);
                }
                return true;
            });
        } catch (IOException e) {
            Log.e("EditProfileFragment", "Error playing song", e);
            Toast.makeText(requireContext(), "Error playing song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (playButton != null) {
                playButton.setImageResource(R.drawable.sound_down);
            }
        }
    }

    /**
     * Displays a detailed dialog for a selected mood post.
     * - Shows post details including image, description, mood, trigger, and timestamp.
     * - Fetches and displays user profile image from Firestore.
     * - Displays comments count and allows navigation to comments dialog.
     * - Supports audio playback for the associated song of the post.
     *
     * @param moodData A map containing mood post data (image URL, description, mood, trigger, etc.).
     */
    private void showPostDetailDialog(Map<String, Object> moodData) {
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

        ImageView profileImage = dialog.findViewById(R.id.profile_image_edit);
        TextView nameText     = dialog.findViewById(R.id.name);
        TextView timeStampText = dialog.findViewById(R.id.time_stamp);
        ImageView postImage = dialog.findViewById(R.id.post_image);
        TextView statusText = dialog.findViewById(R.id.status);
        TextView triggerTextView = dialog.findViewById(R.id.trigger_text_view);
        TextView commentCount = dialog.findViewById(R.id.comment_count);
        TextView songTitle = dialog.findViewById(R.id.song_title);

        String docId = (String) moodData.get("docId");
        fetchSongUrl(docId);




        String imageUrl = (String) moodData.get("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            postImage.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(postImage);
        } else {
            postImage.setVisibility(View.GONE);
        }

        // Format the timestamp to "time ago" format
        Object dateObj = moodData.get("date");
        if (dateObj != null) {
            long timestamp = 0;
            if (dateObj instanceof Long) {
                timestamp = (Long) dateObj;
            } else if (dateObj instanceof String) {
                timestamp = Long.parseLong((String) dateObj);
            }

            if (timestamp > 0) {
                String timeAgo = getTimeAgo(timestamp);
                timeStampText.setText(timeAgo);
            } else {
                timeStampText.setText("Unknown time");
            }
        } else {
            timeStampText.setText("Unknown time");
        }

        // Fetch and load the current profile image from Firestore
        db.collection("users")
                .whereEqualTo("userName", loggedInUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String profileImageUrl = userDoc.getString("profileImageUrl");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_person_black_24dp)
                                    .into(profileImage);
                        }
                    }
                });

        nameText.setText(loggedInUsername);
        statusText.setText((String) moodData.get("description"));
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


        ImageButton commentButton = dialog.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> {
            if (docId != null && !docId.isEmpty()) {
                showCommentsDialog(docId, commentCount);
            } else {
                Toast.makeText(requireContext(), "No doc ID for comments", Toast.LENGTH_SHORT).show();
            }
        });
        playButton = dialog.findViewById(R.id.playButton);
        if (playButton != null) {
            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(v -> togglePlayback());
        } else {
            Log.e("EditProfileFragment", "Play button not found in layout");
        }
        if (docId != null && !docId.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("mood_events")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String title = doc.getString("songTitle"); // get the damn field directly
                            songTitle.setText(title != null ? title : "No title found");
                        } else {
                            songTitle.setText("No song found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        songTitle.setText("Error fetching song");
                    });
        }
        dialog.show();
    }

    /**
     * Fetches song URL from Firestore for a specific mood event document.
     * Updates currentSongUrl and controls play button visibility based on availability.
     *
     * @param documentId Firestore document ID of the mood event to query
     */
    private void fetchSongUrl(String documentId) {
        FirebaseFirestore.getInstance()
                .collection("mood_events")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String songUrl = documentSnapshot.getString("songUrl");
                        if (songUrl != null && !songUrl.isEmpty()) {
                            currentSongUrl = songUrl;
                            if (playButton != null) {
                                playButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (playButton != null) {
                                playButton.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfileFragment", "Error fetching song URL", e);
                    if (playButton != null) {
                        playButton.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    /**
     * Converts timestamp to human-readable relative time string (e.g., "2 hours ago").
     *
     * @param timeInMillis Timestamp in milliseconds
     * @return Formatted relative time string
     */
    private String getTimeAgo(long timeInMillis) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timeInMillis;

        // Convert to seconds
        long seconds = timeDiff / 1000;

        if (seconds < 60) {
            return "just now";
        }

        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        // Convert to days
        long days = hours / 24;
        if (days < 30) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        // Convert to months
        long months = days / 30;
        if (months < 12) {
            return months + (months == 1 ? " month ago" : " months ago");
        }

        // Convert to years
        long years = months / 12;
        return years + (years == 1 ? " year ago" : " years ago");
    }

    /**
     * Loads profile image into ImageView using Glide with circular transformation.
     * Shows placeholder during loading and handles invalid URLs gracefully.
     *
     * @param imageUrl URL of the profile image to load
     */
    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(profileImageEdit);
    }

    /**
     * Displays a dialog for viewing and posting comments on a specific mood event.
     * - Fetches and displays top-level comments from Firestore.
     * - Allows users to post new comments or replies to existing ones.
     * - Updates the comment count dynamically after successful operations.
     *
     * @param docId               The Firestore document ID of the mood event.
     * @param commentCountTextView A TextView to update the comment count dynamically.
     */
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

        RecyclerView commentRecyclerView= dialog.findViewById(R.id.comment_recycler_view);
        TextInputEditText commentInput = dialog.findViewById(R.id.comment_input_edittext);
        Button sendButton              = dialog.findViewById(R.id.comment_send_button);

        // pass docId
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
                    List<Comment> topComments = new ArrayList<>();
                    for (DocumentSnapshot ds : snap) {
                        Comment c = ds.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(ds.getId());
                            topComments.add(c);
                        }
                    }
                    adapter.setCommentList(topComments);
                    commentCountTextView.setText(String.valueOf(topComments.size()));
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
                            reloadTopComments(docId, adapter, commentCountTextView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // subcollection reply
                String parentId = replyingTo[0].getCommentId();
                FirebaseFirestore.getInstance()
                        .collection("mood_events")
                        .document(docId)
                        .collection("comments")
                        .document(parentId)
                        .collection("replies")
                        .add(newC)
                        .addOnSuccessListener(ref -> {
                            replyingTo[0] = null;
                            commentInput.setText("");
                            commentInput.setHint("Write a comment...");
                            reloadTopComments(docId, adapter, commentCountTextView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to post reply", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }

    /**
     * Reloads the top-level comments for a specific mood event from Firestore.
     * Updates the comment adapter and dynamically updates the comment count in the UI.
     *
     * @param docId          The Firestore document ID of the mood event.
     * @param adapter        The adapter for displaying comments in a RecyclerView.
     * @param countTextView  A TextView to display the updated comment count.
     */
    private void reloadTopComments(String docId, CommentAdapter adapter, TextView countTextView) {
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

    /**
     * Loads and displays user data from local storage and Firestore.
     * - Sets user details such as name, username, bio, followers, and following counts.
     * - Loads and displays the profile picture using Glide.
     * - Updates global storage with fetched user data from Firestore.
     */
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

                        // Get profile image URL from Firestore
                        String profileImageUrl = document.getString("profileImageUrl");

                        // Update the profile image using Glide
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_person_black_24dp)
                                    .into(profileImageEdit);
                        } else {
                            // Set default image if no URL is available
                            profileImageEdit.setImageResource(R.drawable.ic_person_black_24dp);
                        }

                        // Update other UI elements
                        List<String> followerList = (List<String>) document.get("followerList");
                        List<String> followingList = (List<String>) document.get("followingList");

                        nameTextView.setText(fullName);
                        usernameTextView.setText("@" + username);
                        followersCountTextView.setText(
                                followerList != null ? String.valueOf(followerList.size()) : "0");
                        followingCountTextView.setText(
                                followingList != null ? String.valueOf(followingList.size()) : "0");


                        bioTextView.setText(
                                document.getString("bio") != null ?
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

    /**
     * Fetches pending follower requests for the logged-in user from Firestore.
     * - Updates the pending requests list and dynamically sets the request count in the UI.
     * - Highlights or resets the pending request card based on the number of requests.
     */
    private void fetchPendingRequests() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            pendingRequestsButton.setText("0");
            // Reset background color if no requests
            pendingRequestView.setCardBackgroundColor(Color.parseColor("#F5F5DC"));
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

                        if (count > 0) {
                            // Highlight the card if there are pending requests
                            pendingRequestView.setCardBackgroundColor(Color.parseColor("#C6E2B5")); // Greenish highlight
                        } else {
                            // Reset background color if no requests
                            pendingRequestView.setCardBackgroundColor(Color.parseColor("#F5F5DC"));
                        }

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
                        // Reset background color if error occurs
                        pendingRequestView.setCardBackgroundColor(Color.parseColor("#F5F5DC"));
                    }
                });
    }

    /**
     * Displays a dialog showing all pending follow requests for the logged-in user.
     * - Allows the user to accept or decline all requests at once.
     * - Displays the count of pending requests and provides a RecyclerView for detailed viewing.
     *
     * @throws IllegalStateException If there are no pending requests, a toast message is shown instead.
     */
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

    /**
     * Accepts all pending follow requests for the logged-in user.
     * - Updates the follower list of the logged-in user in Firestore.
     * - Updates the following list of each follower in Firestore.
     * - Deletes the accepted requests from the "pendingFollowerRequests" collection in Firestore.
     * - Clears the local list of pending requests and updates the UI.
     *
     */
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

        /**
         * Declines all pending follow requests for the logged-in user.
         * - Deletes each request from the "pendingFollowerRequests" collection in Firestore.
         * - Clears the local list of pending requests and updates the UI.
         * - Displays a toast message confirming the action.
         */
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

        /**
         * Declines all pending follow requests for the logged-in user.
         * - Deletes each request from the "pendingFollowerRequests" collection in Firestore.
         * - Clears the local list of pending requests and updates the UI.
         * - Displays a toast message confirming the action.
         */
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
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditProfileFragment", "error deleting request", e);
                    });
        }

        /**
         * ViewHolder class for displaying individual pending follow requests in a RecyclerView.
         * - Holds references to UI components for displaying user details and action buttons.
         * - Provides functionality for accepting or declining follow requests.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView userName;
            TextView username;
            ImageButton acceptButton;
            ImageButton declineButton;
            LinearLayout buttonsLayout;

            /**
             * Constructor for initializing ViewHolder components.
             *
             * @param itemView The view representing an individual item in the RecyclerView.
             */
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userImage = itemView.findViewById(R.id.user_image);
                userName = itemView.findViewById(R.id.user_name);
                username = itemView.findViewById(R.id.username);
                acceptButton = itemView.findViewById(R.id.accept_button);
                declineButton = itemView.findViewById(R.id.decline_button);
                buttonsLayout = itemView.findViewById(R.id.buttons_layout);
            }
        }
    }
}