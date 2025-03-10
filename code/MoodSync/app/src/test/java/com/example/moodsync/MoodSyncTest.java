package com.example.moodsync;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MoodSyncTest {

    private AddMoodActivity addMoodActivity;

    @Before
    public void setUp() {
        // Instantiate AddMoodActivity
        addMoodActivity = new AddMoodActivity();
        // Initialize the moodGradients map
        addMoodActivity.initMoodGradients();
    }

    /**
     * STORY:
     * "As a participant, I want the emotional states to include
     * at least anger, confusion, disgust, fear, happiness, sadness, shame, and surprise."
     */
    @Test
    public void testEmotionalStatesIncluded() {
        // The 8 required states:
        List<String> requiredMoods = Arrays.asList(
                "Angry", "Confused", "Disgusted", "Scared",
                "Happy", "Sad", "Ashamed", "Surprised"
        );

        // Access the private moodGradients map via reflection
        try {
            Field field = AddMoodActivity.class.getDeclaredField("moodGradients");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Integer> moodGradients = (Map<String, Integer>) field.get(addMoodActivity);

            for (String mood : requiredMoods) {
                Assert.assertTrue("MoodGradients map should contain " + mood,
                        moodGradients.containsKey(mood));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail("Could not access moodGradients: " + e.getMessage());
        }
    }

    /**
     * STORY: US 01.03.01 #4
     * "As a participant, I want consistent emotions and colors to depict and
     * distinguish the emotional states in any view."
     */
    @Test
    public void testConsistentEmotionColors() {
        try {
            Field field = AddMoodActivity.class.getDeclaredField("moodGradients");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Integer> moodGradients = (Map<String, Integer>) field.get(addMoodActivity);

            // Verify that each mood has a non-null gradient resource ID.
            for (String mood : moodGradients.keySet()) {
                Integer gradientId = moodGradients.get(mood);
                Assert.assertNotNull("Gradient ID for mood '" + mood + "' should not be null", gradientId);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail("Could not access moodGradients: " + e.getMessage());
        }
    }

    /**
     * STORY: US 01.04.01 #5
     * "As a participant, I want to view a given mood event and all its available details."
     */
    @Test
    public void testViewMoodEventDetails() {
        long date = System.currentTimeMillis();
        MoodEvent event = new MoodEvent("Happy", "Got a new job", "Feeling excited and grateful", "With friends", date);

        // Check that all details are set correctly.
        Assert.assertEquals("Happy", event.getMood());
        Assert.assertEquals("Got a new job", event.getTrigger());
        Assert.assertEquals("Feeling excited and grateful", event.getDescription());
        Assert.assertEquals("With friends", event.getSocialSituation());
        Assert.assertEquals(date, event.getDate());
    }

    /**
     * STORY: US 01.05.01 #7
     * "As a participant, I want to edit the details of a given mood event of mine."
     */
    @Test
    public void testEditMoodEvent() {
        MoodEvent event = new MoodEvent("Sad", "Lost my keys", "Worried about not finding them", "Alone", System.currentTimeMillis());

        // Simulate editing the event.
        event.setMood("Angry");
        event.setDescription("Now I'm really frustrated");
        event.setTrigger("House still locked");

        Assert.assertEquals("Angry", event.getMood());
        Assert.assertEquals("Now I'm really frustrated", event.getDescription());
        Assert.assertEquals("House still locked", event.getTrigger());
    }

    /**
     * STORY: US 01.06.01 #8
     * "As a participant, I want to delete a given mood event of mine."
     */
    @Test
    public void testDeleteMoodEvent() {
        // Simulate having a local mood history list.
        List<MoodEvent> moodHistory = new ArrayList<>();
        MoodEvent event1 = new MoodEvent("Sad", "Trigger1", "Description1", "None", System.currentTimeMillis());
        MoodEvent event2 = new MoodEvent("Happy", "Trigger2", "Description2", "Friends", System.currentTimeMillis());

        moodHistory.add(event1);
        moodHistory.add(event2);

        // Remove event2 and verify deletion.
        moodHistory.remove(event2);

        Assert.assertEquals("After deletion, only 1 event should remain", 1, moodHistory.size());
        Assert.assertTrue("Remaining event should be event1", moodHistory.contains(event1));
        Assert.assertFalse("Event2 should be removed", moodHistory.contains(event2));
    }

    /**
     * STORY:
     * "As a participant, I want to add a mood event to my mood history,
     * each event with the current date and time, a required emotional state,
     * optional trigger, optional social situation."
     */
    @Test
    public void testAddMoodEvent() {
        MoodEvent newEvent = new MoodEvent("Confused", null, null, null, System.currentTimeMillis());

        // Check required and optional fields.
        Assert.assertEquals("Confused", newEvent.getMood());
        Assert.assertNull("Trigger can be null", newEvent.getTrigger());
        Assert.assertNull("Description can be null", newEvent.getDescription());
        Assert.assertNull("SocialSituation can be null", newEvent.getSocialSituation());
        Assert.assertTrue("The date/time should be auto-set", newEvent.getDate() > 0);

        // Simulate adding to mood history.
        List<MoodEvent> moodHistory = new ArrayList<>();
        moodHistory.add(newEvent);
        Assert.assertEquals(1, moodHistory.size());
        Assert.assertTrue(moodHistory.contains(newEvent));
    }
}
