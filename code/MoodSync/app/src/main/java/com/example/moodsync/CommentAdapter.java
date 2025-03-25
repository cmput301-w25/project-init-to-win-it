package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        private final TextView userTextView;
        private final TextView timeTextView;
        private final TextView contentTextView;

        // This holds the Comment currently bound to this ViewHolder
        private Comment boundComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.comment_user);
            timeTextView = itemView.findViewById(R.id.comment_time);
            contentTextView = itemView.findViewById(R.id.comment_text);
        }

        // Bind a single Comment object to the ViewHolder
        void bind(Comment comment) {
            this.boundComment = comment;
            if (comment == null) {
                // If null, clear any existing text
                contentTextView.setText("");
                userTextView.setText("");
                timeTextView.setText("");
                return;
            }
            // Set the comment text
            contentTextView.setText(comment.getText());
            // Display userId
            userTextView.setText(comment.getUserId());
            // Format timestamp
            String formattedTime = formatTimestamp(comment.getTimestamp());
            timeTextView.setText(formattedTime);
        }

        public Comment getBoundComment() {
            return boundComment;
        }

        private String formatTimestamp(long timeMillis) {
            // Example: 24 Mar 2025, 10:00 PM
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }
}
