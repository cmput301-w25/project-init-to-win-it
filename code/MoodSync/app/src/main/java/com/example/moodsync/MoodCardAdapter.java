package com.example.moodsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.Image;
import android.media.MediaPlayer;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter class for displaying mood cards in a RecyclerView.
 * This class binds data from a list of MoodEvent objects to the corresponding views.
 */

public class MoodCardAdapter extends RecyclerView.Adapter<MoodCardAdapter.MoodCardViewHolder> {
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private Boolean isPlaying = false;
    private ImageButton currentPlayButton = null;
    private final List<MoodEvent> moodEvents;
    private FirebaseFirestore db;
    public LocalStorage globalStorage = LocalStorage.getInstance();


    /**
     * Constructor for MoodCardAdapter.
     *
     * @param moodEvents List of mood events to be displayed.
     */
    public MoodCardAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
        this.db = FirebaseFirestore.getInstance();
        this.mediaPlayer = new MediaPlayer(); // Initialize MediaPlayer
    }


    /**
     * Constructor for MoodCardAdapter with a custom MediaPlayer.
     *
     * @param moodEvents  List of mood events to be displayed.
     * @param mediaPlayer Custom MediaPlayer instance.
     */
    public MoodCardAdapter(List<MoodEvent> moodEvents, MediaPlayer mediaPlayer) {
        this.moodEvents = moodEvents;
        this.db = FirebaseFirestore.getInstance();
        this.mediaPlayer = mediaPlayer;
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
        holder.songTitle.setText(moodEvent.getSongTitle());
        Boolean isPublic = moodEvent.isPublic();

        // If isPublic is null, default to true for backward compatibility
        if (isPublic == null) {
            isPublic = true;
        }

        // Set visibility of comment-related views based on public status
        if (!isPublic) {
            holder.commentButton.setVisibility(View.GONE);
            holder.commentCount.setVisibility(View.GONE);
        } else {
            holder.commentButton.setVisibility(View.VISIBLE);
            holder.commentCount.setVisibility(View.VISIBLE);

            // Set comment count (only if public)
            String docId = moodEvent.getDocumentId();
            if (docId != null && !docId.isEmpty()) {
                db.collection("mood_events")
                        .document(docId)
                        .collection("comments")
                        .get()
                        .addOnSuccessListener(snap -> {
                            for (DocumentSnapshot document : snap.getDocuments()) {
                                Comment comment = document.toObject(Comment.class);
                                globalStorage.getComments().add(comment);
                            }
                            holder.commentCount.setText(String.valueOf(snap.size()));
                        })
                        .addOnFailureListener(e -> {
                            holder.commentCount.setText(String.valueOf(globalStorage.getComments().size()));
                        });
            }

            // Handle "Comments" button click (only if public)
            holder.commentButton.setOnClickListener(v -> {
                showCommentsDialog(holder.itemView.getContext(), moodEvent, holder.commentCount);
            });
        }
        String songUrl1 = moodEvent.getSongUrl();
        if (songUrl1 == null || songUrl1.isEmpty()) {
            holder.playButton.setVisibility(View.INVISIBLE);
            holder.songTitle.setVisibility(View.GONE);
        } else {
            holder.playButton.setVisibility(View.VISIBLE);
            holder.songTitle.setVisibility(View.VISIBLE);
            holder.songTitle.setText(moodEvent.getSongTitle());
        }
        // Setup play button click listener
        holder.playButton.setOnClickListener(v -> {
            String songUrl = moodEvent.getSongUrl();
            if (songUrl != null && !songUrl.isEmpty()) {
                playSong(moodEvent, holder.playButton);
            }
        });

        // Update button state based on current playing status
        if (currentSong != null && moodEvent.getSongUrl() != null
                && moodEvent.getSongUrl().equals(currentSong.getUrl().trim()) && isPlaying) {
            holder.playButton.setImageResource(R.drawable.sound_up);
        } else {
            holder.playButton.setImageResource(R.drawable.sound_down);
        }

        // Set the username from the mood event's ID (which is the username)
        String username = moodEvent.getId();
        holder.nameTextView.setText(username);
        holder.songTitle.setText(moodEvent.getSongTitle());
        setMoodEmoji(holder.moodEmoji, moodEvent.getMood());
        User currentUser = globalStorage.getUserFromUName(username);
        String fullName = currentUser.getName();
        if (fullName != null && !fullName.isEmpty()) {
            holder.nameTextView.setText(fullName);
        }
        // Load profile image if available
        String profileImageUrl = currentUser.getPfpUrl();
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
        holder.songTitle.setText(moodEvent.getSongTitle());
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


    /**
     * Plays or pauses the song associated with a MoodEvent when the play button is clicked.
     *
     * @param moodEvent  The MoodEvent containing the song information.
     * @param playButton The play button clicked by the user.
     */
    private void playSong(MoodEvent moodEvent, ImageButton playButton) {
        Log.d("song", "Entering playSong method");

        try {
            String songUrl = moodEvent.getSongUrl().trim();
            String songTitle = moodEvent.getSongTitle();

            if (songUrl == null || songUrl.isEmpty()) {
                Toast.makeText(playButton.getContext(), "Invalid song URL", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the same song is already playing
            if (currentSong != null && songUrl.equals(currentSong.getUrl()) && mediaPlayer != null) {
                // Toggle play/pause for current song
                if (mediaPlayer.isPlaying()) {
                    // Pause the song
                    mediaPlayer.pause();
                    isPlaying = false;
                    playButton.setImageResource(R.drawable.sound_down);
                    Log.d("MoodCardAdapter", "MediaPlayer paused");
                } else {
                    // Resume the song
                    mediaPlayer.start();
                    isPlaying = true;
                    playButton.setImageResource(R.drawable.sound_up);
                    Log.d("MoodCardAdapter", "MediaPlayer resumed");
                }
                return;
            }

            // If we're here, we're playing a new song
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            // If a song is already playing, stop and reset the MediaPlayer
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(songUrl);

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                playButton.setImageResource(R.drawable.sound_up);
                Log.d("MoodCardAdapter", "MediaPlayer started playing");
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playButton.setImageResource(R.drawable.sound_up);
                Log.d("MoodCardAdapter", "MediaPlayer completed playing");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MoodCardAdapter", "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(playButton.getContext(), "Error playing song", Toast.LENGTH_SHORT).show();
                return false;
            });

            mediaPlayer.prepareAsync();
            Log.d("MoodCardAdapter", "MediaPlayer preparing async");

            currentSong = new Song();
            currentSong.setUrl(songUrl);
            currentSong.setTitle(songTitle);
            currentPlayButton = playButton;

        } catch (IOException e) {
            Log.e("MoodCardAdapter", "Error setting data source: ", e);
            Toast.makeText(playButton.getContext(), "Error playing song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Formats a timestamp into a human-readable string indicating how long ago it occurred.
     *
     * @param timestamp The timestamp in milliseconds since epoch.
     * @return A formatted string such as "X days ago", "X hours ago", or "Just now".
     */
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
    /**
     * Sets the mood emoji in the provided ImageView based on the given mood.
     *
     * @param imageView The ImageView where the emoji will be displayed.
     * @param mood      The mood string (e.g., "happy", "sad").
     */
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

    /**
     * Returns a color associated with the given mood.
     *
     * @param mood The mood string (e.g., "happy", "sad").
     * @return The color as an integer value.
     */
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

    /**
     * Updates the list of mood events displayed by the adapter and notifies changes.
     *
     * @param newMoodEvents The new list of MoodEvent objects to display.
     */
    public void updateMoodEvents(List<MoodEvent> newMoodEvents) {
        // Clear existing items first
        moodEvents.clear();
        // Add all new items
        moodEvents.addAll(newMoodEvents);
        // Notify adapter of changes
        notifyDataSetChanged();
    }

    /**
     * Displays a dialog with detailed information about a specific MoodEvent.
     *
     * @param context   The context in which to show the dialog.
     * @param moodEvent The MoodEvent whose details will be displayed.
     */
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

    /**
     * Displays a dialog for viewing and adding comments on a specific MoodEvent.
     *
     * @param context         The context in which to show the dialog.
     * @param moodEvent       The MoodEvent for which comments are displayed.
     * @param commentCountView A TextView to update with the number of comments.
     */
    private void showCommentsDialog(Context context, MoodEvent moodEvent, TextView commentCountView) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comments, null);

        RecyclerView commentRecyclerView = dialogView.findViewById(R.id.comment_recycler_view);
        TextInputEditText commentInput = dialogView.findViewById(R.id.comment_input_edittext);
        Button sendButton = dialogView.findViewById(R.id.comment_send_button);

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.BottomSheetDialogTheme)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.gravity = Gravity.BOTTOM;
            lp.width   = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height  = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        String moodDocId = moodEvent.getDocumentId();
        // Build top-level comment adapter, passing docId so it can load replies
        CommentAdapter adapter = new CommentAdapter(null, context, moodDocId);
        commentRecyclerView.setAdapter(adapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // 1) Load all top-level comments
        db.collection("mood_events")
                .document(moodDocId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Comment> topComments = new ArrayList<>();
                    for (DocumentSnapshot doc : snap) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(doc.getId());
                            topComments.add(c);
                        }
                    }
                    adapter.setCommentList(topComments);
                    commentCountView.setText(String.valueOf(topComments.size()));
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodCardAdapter", "Failed to load top-level comments", e);
                });

        // 2) track if user is replying to a top-level comment or not
        final Comment[] replyingTo = { null };

        adapter.setOnReplyClickListener((topLevelComment, pos) -> {
            replyingTo[0] = topLevelComment;
            if (topLevelComment == null) {
                commentInput.setHint("Write a comment...");
            } else {
                commentInput.setHint("Replying to " + topLevelComment.getUserId());
            }
        });

        // 3) Send => new top-level or subcollection
        sendButton.setOnClickListener(view -> {
            String text = (commentInput.getText() != null)
                    ? commentInput.getText().toString().trim()
                    : "";
            if (text.isEmpty()) {
                Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            MyApplication myApp = (MyApplication)((Activity)context).getApplication();
            String currentUser = myApp.getLoggedInUsername();

            Comment newC = new Comment();
            newC.setUserId(currentUser);
            newC.setText(text);
            newC.setTimestamp(System.currentTimeMillis());

            if (replyingTo[0] == null) {
                // top-level
                db.collection("mood_events")
                        .document(moodDocId)
                        .collection("comments")
                        .add(newC)
                        .addOnSuccessListener(ref -> {
                            commentInput.setText("");
                            commentInput.setHint("Write a comment...");
                            reloadTopLevelComments(moodDocId, adapter, commentCountView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // subcollection
                String parentId = replyingTo[0].getCommentId();
                db.collection("mood_events")
                        .document(moodDocId)
                        .collection("comments")
                        .document(parentId)
                        .collection("replies")
                        .add(newC)
                        .addOnSuccessListener(ref -> {
                            replyingTo[0] = null;
                            commentInput.setText("");
                            commentInput.setHint("Write a comment...");
                            reloadTopLevelComments(moodDocId, adapter, commentCountView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to post reply", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }
    /**
     * Reloads and updates the top-level comments for a specific MoodEvent.
     *
     * @param docId      The document ID of the MoodEvent in Firestore.
     * @param adapter    The CommentAdapter used to display comments.
     * @param countView  A TextView to update with the number of comments.
     */
    private void reloadTopLevelComments(String docId,
                                        CommentAdapter adapter,
                                        TextView countView) {
        db.collection("mood_events")
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
                    countView.setText(String.valueOf(updated.size()));
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodCardAdapter", "Failed to reload top-level comments", e);
                });
    }

    /**
     * ViewHolder class for displaying individual items in the RecyclerView.
     */
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
        TextView songTitle;
        ImageButton playButton;

        /**
         * Constructor for initializing view components in a RecyclerView item layout.
         *
         * @param itemView The root view of the item layout.
         */
        public MoodCardViewHolder(@     NonNull View itemView) {
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
            songTitle = itemView.findViewById(R.id.song_title);
            playButton = itemView.findViewById(R.id.playButton);
        }
    }
}