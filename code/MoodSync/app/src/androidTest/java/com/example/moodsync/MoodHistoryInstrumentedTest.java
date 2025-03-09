package com.example.moodsync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
 * Tests the "Mood History" feature: verifying that we can
 * navigate to the history tab and delete a mood entry.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryInstrumentedTest {

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
        // Insert a mood that we can delete
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("mood_events");

        MoodEvent seed = new MoodEvent(
                "Happy",
                "Test Trigger",
                "We'll delete this one",
                "None",
                System.currentTimeMillis()
        );
        moodsRef.add(seed);
    }

    @Test
    public void testDeleteMoodInHistory() {
        // 1) From the first fragment, press "Get Started"
        onView(withId(R.id.button)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // Step 2: Now on home_page_fragment (“SecondFragment”), tap the History button
        onView(withId(R.id.history_button)).perform(click());
        SystemClock.sleep(1000);

        // 3) Expect to see "Happy" in the history.
        onView(withText("Happy")).check(matches(isDisplayed()));

        // 4) Press "Delete" next to "Happy"
        onView(withId(R.id.delete_button)).perform(click());

        // 5) A delete confirmation dialog appears. Press "Delete"
        onView(withId(R.id.delete_button)).perform(click());

        // 6) Now confirm that "Happy" no longer appears in the list
        onView(withText("Happy")).check(doesNotExist());
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
