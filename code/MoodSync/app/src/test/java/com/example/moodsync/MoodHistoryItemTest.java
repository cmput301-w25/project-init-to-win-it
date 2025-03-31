package com.example.moodsync;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class MoodHistoryItemTest {

    private MoodHistoryItem moodHistoryItem;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date();
        moodHistoryItem = new MoodHistoryItem("Happy", "😊", "Feeling good today", testDate);
    }

    @Test
    public void testConstructor() {
        assertEquals("Happy", moodHistoryItem.getMood());
        assertEquals("😊", moodHistoryItem.getEmoji());
        assertEquals("Feeling good today", moodHistoryItem.getDescription());
        assertEquals(testDate, moodHistoryItem.getDate());
    }

    @Test
    public void testGettersAndSetters() {
        moodHistoryItem.setId("123");
        moodHistoryItem.setMood("Sad");
        moodHistoryItem.setEmoji("😢");
        moodHistoryItem.setDescription("Not feeling great");

        Date newDate = new Date(testDate.getTime() + 1000);
        moodHistoryItem.setDate(newDate);

        assertEquals("123", moodHistoryItem.getId());
        assertEquals("Sad", moodHistoryItem.getMood());
        assertEquals("😢", moodHistoryItem.getEmoji());
        assertEquals("Not feeling great", moodHistoryItem.getDescription());
        assertEquals(newDate, moodHistoryItem.getDate());
    }
}
