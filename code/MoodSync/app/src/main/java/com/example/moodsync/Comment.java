package com.example.moodsync;

public class Comment {
    private String commentId;
    private String userId;
    private String text;
    private long timestamp;

    /**
     * Default constructor required by Firestore for deserialization.
     * Initializes an empty `Comment` object.
     */
    public Comment() {
        // Needed by Firestore
    }

    /**
     * Constructs a new `Comment` object with the specified details.
     *
     * @param commentId The unique ID of the comment.
     * @param userId    The ID of the user who posted the comment.
     * @param text      The text content of the comment.
     * @param timestamp The timestamp (in milliseconds since epoch) when the comment was created.
     */
    public Comment(String commentId, String userId, String text, long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

    /**
     * Retrieves the unique ID of the comment.
     *
     * @return The comment's unique ID as a `String`.
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * Sets the unique ID of the comment.
     *
     * @param commentId The unique ID to assign to this comment.
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Retrieves the ID of the user who posted the comment.
     *
     * @return The user ID as a `String`.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who posted the comment.
     *
     * @param userId The user ID to assign to this comment.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the text content of the comment.
     *
     * @return The text content of the comment as a `String`.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the comment.
     *
     * @param text The text content to assign to this comment.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Retrieves the timestamp when the comment was created.
     *
     * @return The timestamp in milliseconds since epoch as a `long`.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the comment was created.
     *
     * @param timestamp The timestamp in milliseconds since epoch to assign to this comment.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
