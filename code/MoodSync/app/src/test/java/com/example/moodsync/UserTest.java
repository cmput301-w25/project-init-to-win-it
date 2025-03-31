package com.example.moodsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setId("123");
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setPass("password123");
        user.setBio("Test bio");
        user.setLocation("Edmonton");
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals("123", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("password123", user.getPass());
        assertEquals("Test bio", user.getBio());
        assertEquals("Edmonton", user.getLocation());
    }

    @Test
    public void testAddFollower() {
        user.addFollower("follower1");
        assertTrue(user.getFollowerList().contains("follower1"));
        assertEquals(1, user.getFollowerList().size());
    }

    @Test
    public void testAddFollowing() {
        user.addFollowing("following1");
        assertTrue(user.getFollowingList().contains("following1"));
        assertEquals(1, user.getFollowingList().size());
    }

    @Test
    public void testAddComment() {
        user.addComment(123);
        assertTrue(user.getCommentList().contains(123));
        assertEquals(1, user.getCommentList().size());
    }

    @Test
    public void testSetLists() {
        ArrayList<String> followers = new ArrayList<>();
        followers.add("follower1");
        followers.add("follower2");

        ArrayList<String> following = new ArrayList<>();
        following.add("following1");

        ArrayList<Integer> comments = new ArrayList<>();
        comments.add(123);
        comments.add(456);

        user.setFollowerList(followers);
        user.setFollowingList(following);
        user.setCommentList(comments);

        assertEquals(2, user.getFollowerList().size());
        assertEquals(1, user.getFollowingList().size());
        assertEquals(2, user.getCommentList().size());
    }
}
