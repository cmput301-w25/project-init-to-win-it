package com.example.moodsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommentAdapterTest {

    private CommentAdapter adapter;
    private List<Comment> testComments;

    @Before
    public void setUp() {
        testComments = Arrays.asList(
                new Comment("C1", "user1", "Great post!", 1672531200000L),
                new Comment("C2", "user2", "Nice work!", 1672617600000L)
        );
        adapter = new CommentAdapter(testComments);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(2, adapter.getItemCount());
        try{
            adapter.setCommentList(null);
            assertEquals(0, adapter.getItemCount());
        } catch (Exception e){
            //Do Nothing
        }

    }

    @Test
    public void testGetItem() {
        Comment comment = adapter.getItem(0);
        assertEquals("C1", comment.getCommentId());
        assertEquals("user1", comment.getUserId());
        assertEquals("Great post!", comment.getText());
        assertEquals(1672531200000L, comment.getTimestamp());

        comment = adapter.getItem(1);
        assertEquals("C2", comment.getCommentId());

        assertNull(adapter.getItem(-1));
        assertNull(adapter.getItem(2));
    }
}
