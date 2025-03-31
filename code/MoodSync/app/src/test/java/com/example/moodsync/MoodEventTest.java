package com.example.moodsync;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MoodEventTest {

    @Test
    public void testDefaultConstructor() {
        MoodEvent moodEvent = new MoodEvent();
        assertNotNull(moodEvent);
    }

    @Test
    public void testParameterizedConstructor() {
        String mood = "Happy";
        String trigger = "Good News";
        String description = "Feeling great";
        String socialSituation = "Alone";
        long date = System.currentTimeMillis();
        String imageUrl = "https://example.com/image.jpg";
        boolean isPublic = true;
        String id = "12345";
        String songUrl = "https://example.com/song.mp3";
        String songTitle = "Happy Song";
        String location = "Home";

        MoodEvent moodEvent = new MoodEvent(mood, trigger, description, socialSituation, date, imageUrl, isPublic, id, songUrl, songTitle, location);

        assertEquals(mood, moodEvent.getMood());
        assertEquals(trigger, moodEvent.getTrigger());
        assertEquals(description, moodEvent.getDescription());
        assertEquals(socialSituation, moodEvent.getSocialSituation());
        assertEquals(date, moodEvent.getDate());
        assertEquals(imageUrl, moodEvent.getImageUrl());
        assertEquals(isPublic, moodEvent.isPublic());
        assertEquals(id, moodEvent.getId());
        assertEquals(songUrl, moodEvent.getSongUrl());
        assertEquals(songTitle, moodEvent.getSongTitle());
        assertEquals(location, moodEvent.getLocation());
    }

    @Test
    public void testParcelable() {
        String mood = "Happy";
        String trigger = "Good News";
        String description = "Feeling great";
        String socialSituation = "Alone";
        long date = System.currentTimeMillis();
        String imageUrl = "https://example.com/image.jpg";
        boolean isPublic = true;
        String id = "12345";
        String songUrl = "https://example.com/song.mp3";
        String songTitle = "Happy Song";
        String location = "Home";

        MoodEvent originalMoodEvent = new MoodEvent(mood, trigger, description, socialSituation, date, imageUrl, isPublic, id, songUrl, songTitle, location);

        MoodEvent recreatedMoodEvent = new MoodEvent(
                originalMoodEvent.getMood(),
                originalMoodEvent.getTrigger(),
                originalMoodEvent.getDescription(),
                originalMoodEvent.getSocialSituation(),
                originalMoodEvent.getDate(),
                originalMoodEvent.getImageUrl(),
                originalMoodEvent.isPublic(),
                originalMoodEvent.getId(),
                originalMoodEvent.getSongUrl(),
                originalMoodEvent.getSongTitle(),
                originalMoodEvent.getLocation()
        );

        assertEquals(originalMoodEvent.getMood(), recreatedMoodEvent.getMood());
        assertEquals(originalMoodEvent.getTrigger(), recreatedMoodEvent.getTrigger());
        assertEquals(originalMoodEvent.getDescription(), recreatedMoodEvent.getDescription());
        assertEquals(originalMoodEvent.getSocialSituation(), recreatedMoodEvent.getSocialSituation());
        assertEquals(originalMoodEvent.getDate(), recreatedMoodEvent.getDate());
        assertEquals(originalMoodEvent.getImageUrl(), recreatedMoodEvent.getImageUrl());
        assertEquals(originalMoodEvent.isPublic(), recreatedMoodEvent.isPublic());
        assertEquals(originalMoodEvent.getId(), recreatedMoodEvent.getId());
        assertEquals(originalMoodEvent.getSongUrl(), recreatedMoodEvent.getSongUrl());
        assertEquals(originalMoodEvent.getSongTitle(), recreatedMoodEvent.getSongTitle());
        assertEquals(originalMoodEvent.getLocation(), recreatedMoodEvent.getLocation());
    }

    @Test
    public void testGettersAndSetters() {
        MoodEvent moodEvent = new MoodEvent();
        String mood = "Sad";
        String trigger = "Bad News";
        String description = "Feeling down";
        String socialSituation = "With friends";
        long date = System.currentTimeMillis();
        String imageUrl = "https://example.com/image2.jpg";
        boolean isPublic = false;
        String id = "67890";
        String songUrl = "https://example.com/song2.mp3";
        String songTitle = "Sad Song";
        String location = "Park";

        moodEvent.setMood(mood);
        moodEvent.setTrigger(trigger);
        moodEvent.setDescription(description);
        moodEvent.setSocialSituation(socialSituation);
        moodEvent.setDate(date);
        moodEvent.setImageUrl(imageUrl);
        moodEvent.setPublic(isPublic);
        moodEvent.setId(id);
        moodEvent.setSongUrl(songUrl);
        moodEvent.setSongTitle(songTitle);
        moodEvent.setLocation(location);

        assertEquals(mood, moodEvent.getMood());
        assertEquals(trigger, moodEvent.getTrigger());
        assertEquals(description, moodEvent.getDescription());
        assertEquals(socialSituation, moodEvent.getSocialSituation());
        assertEquals(date, moodEvent.getDate());
        assertEquals(imageUrl, moodEvent.getImageUrl());
        assertEquals(isPublic, moodEvent.isPublic());
        assertEquals(id, moodEvent.getId());
        assertEquals(songUrl, moodEvent.getSongUrl());
        assertEquals(songTitle, moodEvent.getSongTitle());
        assertEquals(location, moodEvent.getLocation());
    }
}
