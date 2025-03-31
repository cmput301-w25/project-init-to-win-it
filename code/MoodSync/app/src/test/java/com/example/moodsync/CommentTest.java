package com.example.moodsync;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommentTest {

    @Test
    public void testDefaultConstructor() {
        Comment comment = new Comment();
        assertNotNull(comment);
        assertNull(comment.getCommentId());
        assertNull(comment.getUserId());
        assertNull(comment.getText());
        assertEquals(0, comment.getTimestamp());
    }

    @Test
    public void testParameterizedConstructor() {
        Comment comment = new Comment("C123", "U456", "Great post!", 1672531200L);

        assertEquals("C123", comment.getCommentId());
        assertEquals("U456", comment.getUserId());
        assertEquals("Great post!", comment.getText());
        assertEquals(1672531200L, comment.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        Comment comment = new Comment();

        comment.setCommentId("COM_789");
        comment.setUserId("USER_101");
        comment.setText("Updated text");
        comment.setTimestamp(1672617600L);

        assertEquals("COM_789", comment.getCommentId());
        assertEquals("USER_101", comment.getUserId());
        assertEquals("Updated text", comment.getText());
        assertEquals(1672617600L, comment.getTimestamp());
    }

    @Test
    public void testEdgeCases() {
        Comment comment = new Comment(null, null, "", 0L);

        assertNull(comment.getCommentId());
        assertNull(comment.getUserId());
        assertEquals("", comment.getText());
        assertEquals(0L, comment.getTimestamp());
    }

    @Test
    public void testFieldUpdates() {
        Comment comment = new Comment("C1", "U1", "Original", 100L);

        comment.setCommentId("C2");
        comment.setUserId("U2");
        comment.setText("Updated");
        comment.setTimestamp(200L);

        assertEquals("C2", comment.getCommentId());
        assertEquals("U2", comment.getUserId());
        assertEquals("Updated", comment.getText());
        assertEquals(200L, comment.getTimestamp());
    }
}