package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple adapter that displays the list of replies (subcollection).
 * Each reply is also a Comment object, but strictly a child of some top-level comment.
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private List<Comment> replyList;

    public ReplyAdapter(List<Comment> replies) {
        this.replyList = replies;
    }

    public void setReplyList(List<Comment> newReplies) {
        this.replyList = newReplies;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (replyList == null) ? 0 : replyList.size();
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Comment reply = replyList.get(position);
        holder.bind(reply);
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;
        TextView timeTextView;
        TextView contentTextView;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView   = itemView.findViewById(R.id.comment_user);
            timeTextView   = itemView.findViewById(R.id.comment_time);
            contentTextView= itemView.findViewById(R.id.comment_text);
        }

        void bind(Comment reply) {
            if (reply == null) {
                userTextView.setText("");
                contentTextView.setText("");
                timeTextView.setText("");
                return;
            }
            userTextView.setText(reply.getUserId());
            contentTextView.setText(reply.getText());
            timeTextView.setText(formatTimestamp(reply.getTimestamp()));
        }

        private String formatTimestamp(long timeMillis) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }
}
