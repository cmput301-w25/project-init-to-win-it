package com.example.moodsync;

public class Comment {
    private String commentId;
    private String userId;
    private String text;
    private long timestamp;

    public Comment() {
        // Needed by Firestore
    }

    public Comment(String commentId, String userId, String text, long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
