package com.example.moodsync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests that focus on the Home Page (SecondFragment).
 * For example: verifying that tapping "Details" on a mood card
 * shows the expected bottom-sheet or dialog with that mood's details.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomePageInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Use the local Firestore emulator instead of the real Firestore.
     */
    @BeforeClass
    public static void useFirestoreEmulator() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }

    /**
     * Seed some baseline data in the Firestore emulator.
     */
    @Before
    public void seedDatabase() {
        // Insert one known mood event in Firestore so the "Home Page" can display it
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("mood_events");

        MoodEvent seededEvent = new MoodEvent(
                "Happy",
                "Got a new job",
                "Feeling excited and grateful",
                "With friends",
                System.currentTimeMillis()
        );
        moodsRef.add(seededEvent);
    }

    /**
     * Test that tapping "Details" on a mood card shows a bottom-sheet or dialog
     * with the correct mood info.
     */
    @Test
    public void testDetailsButtonShowsMoodDetails() {
        // 1) From the first fragment, press "Get Started"
        onView(withId(R.id.button)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // 2) The home page should show the seeded mood
        onView(withText("Mood: Happy"))
                .check(matches(isDisplayed()));

        // 3) Tap "Details" button
        onView(withId(R.id.details_button)).perform(click());

        // 4) Now check that the details bottom-sheet is displayed with correct info
        onView(withText("Mood Details"))
                .check(matches(isDisplayed()));
        onView(withText("Mood: Happy"))
                .check(matches(isDisplayed()));
        onView(withText("Trigger: Got a new job"))
                .check(matches(isDisplayed()));
        onView(withText("Description: Feeling excited and grateful"))
                .check(matches(isDisplayed()));
        onView(withText("Social Situation: With friends"))
                .check(matches(isDisplayed()));
    }

    /**
     * After each test, clear out the Firestore emulator’s data so that
     * each test starts fresh with no leftover documents from previous tests.
     */
    @After
    public void clearEmulatorDb() {
        try {
            String projectId = "inittowinit-1188f";
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/"
                    + projectId
                    + "/databases/(default)/documents");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int response = conn.getResponseCode();
            Log.i("EmulatorCleanup", "HTTP DELETE response code: " + response);
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("EmulatorCleanup", "Bad URL: " + e.getMessage());
        } catch (Exception e) {
            Log.e("EmulatorCleanup", "Error clearing DB: " + e.getMessage());
        }
    }
}
