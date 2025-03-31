package com.example.moodsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays top-level comments from mood_events/{docId}/comments
 * and fetches subcollection {commentId}/replies for each when user clicks 'Show more.'
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context;
    private String moodDocId;  // The doc ID for the mood event

    /**
     * When user clicks 'Reply' or 'Cancel', we tell the parent (like MoodCardAdapter)
     * which comment is being replied to (or null if canceled).
     */
    public interface OnReplyClickListener {
        // If topLevelComment = null => user canceled
        // else user is replying to 'topLevelComment' at 'position'
        void onReplyClicked(Comment topLevelComment, int position);
    }

    private OnReplyClickListener replyClickListener;

    public void setOnReplyClickListener(OnReplyClickListener listener) {
        this.replyClickListener = listener;
    }

    // We'll pass 'moodDocId' in so this adapter can load subcollection replies
    public CommentAdapter(List<Comment> initialComments, Context context, String moodDocId) {
        this.commentList = initialComments;
        this.context     = context;
        this.moodDocId   = moodDocId;
    }

    public void setCommentList(List<Comment> newList) {
        this.commentList = newList;
        notifyDataSetChanged();
    }

    public Comment getItem(int position) {
        if (commentList != null && position >= 0 && position < commentList.size()) {
            return commentList.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return (commentList == null) ? 0 : commentList.size();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);

        // Display "Show more" button if replies exist for a comment
        holder.showRepliesButton.setVisibility(View.GONE);
        holder.showRepliesButton.setText("");
        if (comment.getCommentId() == null) {
            // If we have no ID, can't query. Just keep hidden.
        } else {
            FirebaseFirestore.getInstance()
                    .collection("mood_events")
                    .document(moodDocId)
                    .collection("comments")
                    .document(comment.getCommentId())
                    .collection("replies")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            // Has at least 1 reply => show "Show more"
                            holder.showRepliesButton.setText("Show more");
                            holder.showRepliesButton.setVisibility(View.VISIBLE);
                        } // else remains hidden
                    })
                    .addOnFailureListener(e -> {
                        // In case of error, just keep hidden or log it
                        holder.showRepliesButton.setVisibility(View.GONE);
                    });
        }

        // "Reply" <-> "Cancel"
        holder.replyButton.setOnClickListener(v -> {
            // If it's currently "Reply", switch to "Cancel" and notify callback
            if (holder.replyButton.getText().equals("Reply")) {
                holder.replyButton.setText("Cancel");
                if (replyClickListener != null) {
                    replyClickListener.onReplyClicked(comment, position);
                }
            } else {
                // else it's "Cancel"
                holder.replyButton.setText("Reply");
                if (replyClickListener != null) {
                    // pass null => user canceled
                    replyClickListener.onReplyClicked(null, -1);
                }
            }
        });

        // "Show more" <-> "Show less"
        holder.showRepliesButton.setOnClickListener(v -> {
            if (holder.repliesRecyclerView.getVisibility() == View.VISIBLE) {
                // Hide
                holder.repliesRecyclerView.setVisibility(View.GONE);
                holder.showRepliesButton.setText("Show more");
            } else {
                // Show
                holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                holder.showRepliesButton.setText("Show less");
                loadRepliesForComment(comment, holder.replyAdapter);
            }
        });
    }

    /**
     * Load all replies from subcollection: mood_events/{docId}/comments/{commentId}/replies
     */
    private void loadRepliesForComment(Comment parentComment, ReplyAdapter replyAdapter) {
        if (parentComment.getCommentId() == null) {
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("mood_events")
                .document(moodDocId)
                .collection("comments")
                .document(parentComment.getCommentId())
                .collection("replies")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Comment> childReplies = new ArrayList<>();
                    for (var doc : snap) {
                        Comment r = doc.toObject(Comment.class);
                        if (r != null) {
                            r.setCommentId(doc.getId());
                            childReplies.add(r);
                        }
                    }
                    replyAdapter.setReplyList(childReplies);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load replies", Toast.LENGTH_SHORT).show();
                });
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView, timeTextView, contentTextView;
        TextView replyButton, showRepliesButton;
        RecyclerView repliesRecyclerView;
        ReplyAdapter replyAdapter; // for displaying subcollection replies

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView       = itemView.findViewById(R.id.comment_user);
            timeTextView       = itemView.findViewById(R.id.comment_time);
            contentTextView    = itemView.findViewById(R.id.comment_text);
            replyButton        = itemView.findViewById(R.id.reply_button);
            showRepliesButton  = itemView.findViewById(R.id.show_replies_button);
            repliesRecyclerView= itemView.findViewById(R.id.replies_recycler_view);

            // Setup the nested replies RecyclerView
            replyAdapter = new ReplyAdapter(null);
            repliesRecyclerView.setAdapter(replyAdapter);
            repliesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            // start hidden
            repliesRecyclerView.setVisibility(View.GONE);
        }

        void bind(Comment comment) {
            if (comment == null) {
                userTextView.setText("");
                contentTextView.setText("");
                timeTextView.setText("");
                return;
            }
            userTextView.setText(comment.getUserId());
            contentTextView.setText(comment.getText());
            timeTextView.setText(formatTimestamp(comment.getTimestamp()));

            // reset the button states each time we bind
            replyButton.setText("Reply");
            showRepliesButton.setText("Show more");
            repliesRecyclerView.setVisibility(View.GONE);
        }

        private String formatTimestamp(long timeMillis) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }
}