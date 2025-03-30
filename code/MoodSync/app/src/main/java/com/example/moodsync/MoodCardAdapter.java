package com.example.moodsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MoodCardAdapter extends RecyclerView.Adapter<MoodCardAdapter.MoodCardViewHolder> {

    private final List<MoodEvent> moodEvents;
    private FirebaseFirestore db;
    public LocalStorage globalStorage = LocalStorage.getInstance();

    public MoodCardAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MoodCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_mood_card_item, parent, false);
        return new MoodCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodCardViewHolder holder, int position) {
        MoodEvent moodEvent = moodEvents.get(position);
//         MoodEvent moodEvent = moodEvents.get(position);
        Log.d("ADAPTER", "onBindViewHolder: " + moodEvent.getId());
        // Set the username from the mood event's ID (which is the username)
        String username = moodEvent.getId();
        holder.nameTextView.setText(username);
        setMoodEmoji(holder.moodEmoji, moodEvent.getMood());
        User currentUser =globalStorage.getUserFromUName(username);
        Log.d("ADAPTER", "name: " +username);
        String fullName = currentUser.getName();
        if (fullName != null && !fullName.isEmpty()) {
            holder.nameTextView.setText(fullName);
        }
        // Load profile image if available
        String profileImageUrl = currentUser.getPfpUrl();
        Log.d("PROFILE IMAGE", "onBindViewHolder: "+profileImageUrl);
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(holder.profileImageView);
        }
        // Format and set timestamp
        long timestamp = moodEvent.getDate();
        holder.timeStampTextView.setText(formatTimestamp(timestamp));

        // Load post image if available
        String imageUrl = moodEvent.getImageUrl();
        Log.d("THING", "onBindViewHolder: "+ moodEvent.getImageUrl());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }

        // Set mood text and color
        holder.moodTextView.setText(moodEvent.getMood());
        int moodColor = getMoodColor(moodEvent.getMood());
        holder.moodBanner.setBackgroundColor(moodColor);

        // Set description
        holder.statusTextView.setText(moodEvent.getDescription());

        // Set trigger
        String trigger = moodEvent.getTrigger();
        if (trigger != null && !trigger.isEmpty()) {
            holder.triggerTextView.setText(trigger);
        } else {
            holder.triggerTextView.setText("None");
        }

        // Handle "View Details" button click
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(holder.itemView.getContext(), moodEvent));

        // Set comment count
        String docId = moodEvent.getDocumentId();
        if (docId != null && !docId.isEmpty()) {
            db.collection("mood_events")
                    .document(docId)
                    .collection("comments")
                    .get()
                    .addOnSuccessListener(snap -> {
                                for (DocumentSnapshot document : snap.getDocuments()) {
                                    // Convert the document to Comment object
                                    Comment comment = document.toObject(Comment.class);

                                    // Add the comment to  local list for offline access
                                    globalStorage.insertComment(comment);
                                }
                        // Set the count based on how many comments are in this doc
                        holder.commentCount.setText(String.valueOf(snap.size()));

                    })
                    .addOnFailureListener(e -> {
                        // If something fails, just show 0 or do nothing
                        holder.commentCount.setText(String.valueOf(globalStorage.getComments().size()));
                    });
        }

        // Handle "Comments" button click
        holder.commentButton.setOnClickListener(v -> {
            showCommentsDialog(holder.itemView.getContext(), moodEvent, holder.commentCount);
        });
    }

    private String formatTimestamp(long timestamp) {
        // Simple timestamp formatting - you can enhance this as needed
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - timestamp;

        long seconds = diffTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    private void setMoodEmoji(ImageView imageView, String mood) {
        if (mood == null) {
            imageView.setImageResource(R.drawable.ic_mood_black_24dp);
            return;
        }

        switch (mood.toLowerCase()) {
            case "happy":
                imageView.setImageResource(R.drawable.ic_mood_happy);
                break;
            case "sad":
                imageView.setImageResource(R.drawable.ic_mood_sad);
                break;
            case "angry":
                imageView.setImageResource(R.drawable.ic_mood_angry);
                break;
            case "confused":
                imageView.setImageResource(R.drawable.ic_mood_confused);
                break;
            case "surprised":
                imageView.setImageResource(R.drawable.ic_mood_surprised);
                break;
            case "ashamed":
                imageView.setImageResource(R.drawable.ic_mood_ashamed);
                break;
            case "scared":
                imageView.setImageResource(R.drawable.ic_mood_scared);
                break;
            case "disgusted":
                imageView.setImageResource(R.drawable.ic_mood_disgusted);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_mood_default);
                break;
        }

    }

    private int getMoodColor(String mood) {
        if (mood == null) {
            return 0xFFFFFFFF; // Default white if mood is null
        }

        switch (mood.toLowerCase()) {
            case "happy":
                return 0xFFFFF8E1; // Soft Yellow
            case "sad":
                return 0xFFE3F2FD; // Soft Blue
            case "angry":
                return 0xFFFFEBEE; // Soft Red
            case "confused":
                return 0xFFF3E5F5; // Soft Purple
            case "surprised":
                return 0xFFE0F7FA; // Soft Cyan
            case "ashamed":
                return 0xFFEFEBE9; // Soft Brown
            case "scared":
                return 0xFFECEFF1; // Soft Blue Grey
            case "disgusted":
                return 0xFFE8F5E9; // Soft Green
            default:
                return 0xFFFFFFFF; // Pure White
        }
    }

    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0;
    }

    public void updateMoodEvents(List<MoodEvent> newMoodEvents) {
        this.moodEvents.clear();
        this.moodEvents.addAll(newMoodEvents);
        notifyDataSetChanged();
    }

    private void showDetailsDialog(Context context, MoodEvent moodEvent) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_details2, null);
        TextView detailsMood = dialogView.findViewById(R.id.details_mood);
        TextView detailsTrigger = dialogView.findViewById(R.id.details_trigger);
        TextView detailsDescription = dialogView.findViewById(R.id.details_description);
        TextView detailsSocialSituation = dialogView.findViewById(R.id.details_social_situation);
        TextView detailsLocation = dialogView.findViewById(R.id.details_location);
        LinearLayout headerLayout = dialogView.findViewById(R.id.dialog_header);
        TextView emojiView = dialogView.findViewById(R.id.details_emoji);
        headerLayout.setBackgroundColor(getMoodColor(moodEvent.getMood()));

        // Set emoji text based on mood
        if (emojiView != null) {
            switch (moodEvent.getMood().toLowerCase()) {
                case "happy":
                    emojiView.setText("😄");
                    break;
                case "sad":
                    emojiView.setText("😞");
                    break;
                case "angry":
                    emojiView.setText("😠");
                    break;
                case "confused":
                    emojiView.setText("😕");
                    break;
                case "surprised":
                    emojiView.setText("😲");
                    break;
                case "ashamed":
                    emojiView.setText("😳");
                    break;
                case "scared":
                    emojiView.setText("😨");
                    break;
                case "disgusted":
                    emojiView.setText("🤢");
                    break;
                default:
                    emojiView.setText("😐");
                    break;
            }
        }

        detailsMood.setText("Mood: " + moodEvent.getMood());
        detailsTrigger.setText("Trigger: " + (moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "N/A"));
        detailsDescription.setText("Description: " + (moodEvent.getDescription() != null ? moodEvent.getDescription() : "N/A"));
        detailsSocialSituation.setText("Social Situation: " + (moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "N/A"));
        detailsLocation.setText("Location: " + (moodEvent.getLocation() != null ? moodEvent.getLocation() : "N/A"));

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BottomSheetDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        dialog.show();
    }

    private void showCommentsDialog(Context context, MoodEvent moodEvent, TextView commentCount) {
        // Inflate the layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comments, null);

        RecyclerView commentRecyclerView = dialogView.findViewById(R.id.comment_recycler_view);
        TextInputEditText commentInputEditText = dialogView.findViewById(R.id.comment_input_edittext);
        Button sendButton = dialogView.findViewById(R.id.comment_send_button);

        // Build the adapter
        CommentAdapter commentAdapter = new CommentAdapter(null);
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BottomSheetDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Appear as a "bottom sheet" style
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        // Load existing comments from Firestore
        String moodDocId = moodEvent.getDocumentId();
        db.collection("mood_events")
                .document(moodDocId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> allComments = new ArrayList<>();
                    for (DocumentSnapshot docSnap : queryDocumentSnapshots) {
                        Comment c = docSnap.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(docSnap.getId());
                            allComments.add(c);
                        }
                    }
                    commentAdapter.setCommentList(allComments);

                    // Scroll to the latest comment
                    int lastPosition = commentAdapter.getItemCount() - 1;
                    if (lastPosition >= 0) {
                        commentRecyclerView.scrollToPosition(lastPosition);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodCardAdapter", "Failed to load comments", e);
                });

        // Handle adding new comment
        sendButton.setOnClickListener(view -> {
            String newCommentText = "";
            if (commentInputEditText.getText() != null) {
                newCommentText = commentInputEditText.getText().toString().trim();
            }
            if (newCommentText.isEmpty()) {
                Toast.makeText(context, "Please enter a comment.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch current user
            MyApplication myApp = (MyApplication) ((Activity)context).getApplication();
            String currentUser = myApp.getLoggedInUsername();

            // Build the comment object
            long currentTime = System.currentTimeMillis();
            Comment commentObj = new Comment(null, currentUser, newCommentText, currentTime);

            // Save it to Firestore in subcollection
            db.collection("mood_events")
                    .document(moodDocId)
                    .collection("comments")
                    .add(commentObj)
                    .addOnSuccessListener(documentReference -> {
                        // Clear input
                        commentInputEditText.setText("");

                        // Update the RecyclerView immediately by appending
                        commentObj.setCommentId(documentReference.getId());

                        List<Comment> currentList = new ArrayList<>(commentAdapter.getItemCount());
                        for (int i = 0; i < commentAdapter.getItemCount(); i++) {
                            RecyclerView.ViewHolder vh =
                                    commentRecyclerView.findViewHolderForAdapterPosition(i);
                            if (vh != null && vh instanceof CommentAdapter.CommentViewHolder) {
                                CommentAdapter.CommentViewHolder holder = (CommentAdapter.CommentViewHolder) vh;
                                currentList.add(holder.getBoundComment());
                            }
                        }
                        currentList.add(commentObj);
                        commentAdapter.setCommentList(currentList);

                        // Scroll to the latest comment
                        int lastPosition = commentAdapter.getItemCount() - 1;
                        if (lastPosition >= 0) {
                            commentRecyclerView.scrollToPosition(lastPosition);
                        }

                        // Re-fetch the total count and update the TextView
                        db.collection("mood_events")
                                .document(moodDocId)
                                .collection("comments")
                                .get()
                                .addOnSuccessListener(snap -> {
                                    int newCount = snap.size();
                                    commentCount.setText(String.valueOf(newCount));
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MoodCardAdapter", "Failed to post comment", e);
                        Toast.makeText(context, "Failed to post comment.", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }


    static class MoodCardViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView timeStampTextView;
        TextView moodTextView;
        TextView statusTextView;
        TextView triggerTextView;
        ImageView profileImageView;
        ImageView postImageView;
        ImageView moodEmoji;
        Button detailsButton;
        ImageButton commentButton;
        LinearLayout moodBanner;
        TextView commentCount;

        public MoodCardViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            timeStampTextView = itemView.findViewById(R.id.time_stamp);
            moodTextView = itemView.findViewById(R.id.mood_text_view);
            statusTextView = itemView.findViewById(R.id.status);
            triggerTextView = itemView.findViewById(R.id.trigger_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_edit);
            postImageView = itemView.findViewById(R.id.post_image);
            moodEmoji = itemView.findViewById(R.id.mood_emoji);
            detailsButton = itemView.findViewById(R.id.details_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            commentCount = itemView.findViewById(R.id.comment_count);
            moodBanner = itemView.findViewById(R.id.mood_banner);
        }
    }
}
