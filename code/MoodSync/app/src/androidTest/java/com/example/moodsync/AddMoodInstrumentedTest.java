package com.example.moodsync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
 * Espresso instrumented tests for MoodSync, using the Firestore emulator.
 *
 * These tests simulate a user’s journey from the first “Get Started” screen,
 * through the home (second) fragment, into the add‐mood flow,
 * and finally verifying success or blocking behavior depending on the input.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddMoodInstrumentedTest {

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
     * Seed some baseline data in the Firestore emulator before each test.
     */
    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("mood_events");

        // Add a "seed" mood event so that the home page's list shows it
        MoodEvent seed = new MoodEvent(
                "Happy",
                "Baseline Trigger",
                "Seeded baseline event",
                "With friends",
                System.currentTimeMillis(),
                "https://image-url"
        );
        moodsRef.add(seed);
    }

    /**
     * “Happy path” test for adding a new mood event with valid data:
     * 1) Click "Get Started" on the first fragment to navigate to the second fragment
     * 2) Tap the FAB (add_circle_button) to open the Add Mood flow
     * 3) In the first add_mood_fragment, type a short description and press "Next"
     * 4) In the second add_mood_fragment2, fill in the trigger and press "Create mood"
     * 5) Verify the success dialog text "Your mood has been successfully uploaded." is displayed
     */
    @Test
    public void testAddMoodEventViaUI() {
        SystemClock.sleep(3000);
        // Step 1: From the first fragment, press "Get Started"
        onView(withId(R.id.button)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // Step 2: Now on home_page_fragment (“SecondFragment”), tap the Add Mood button
        onView(withId(R.id.add_circle_button)).perform(click());

        // Step 3: Select a mood
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());

        // Step 3: Add a description
        onView(withId(R.id.edit_description))
                .perform(typeText("test mood event"), closeSoftKeyboard());
        onView(withId(R.id.next)).perform(click());

        // Step 4: Fill out the second Add Mood screen
        onView(withId(R.id.triggerInput))
                .perform(typeText("test trigger"), closeSoftKeyboard());
        onView(withId(R.id.createmood)).perform(click());

        // Step 5: Confirm that the success dialog is displayed
        onView(withText("Your mood has been successfully uploaded."))
                .check(matches(isDisplayed()));
        // Wait a little to allow the success dialog to disappear
        SystemClock.sleep(3000);
    }

    /**
     * “Unhappy path” test for an invalid description:
     * In this case, the user enters a description that is longer than 20 characters
     * or more than 3 words, which violates the input rules in AddMoodActivity.
     * We check that we remain on page 1 by verifying the second screen’s
     * unique EditText (triggerInput) does NOT exist.
     *
     * This means the app blocked navigation to the second page (and presumably
     * showed a toast or some other message).
     */
    @Test
    public void testInvalidDescriptionPreventsNext() {
        // 1) From the first fragment, press "Get Started"
        onView(withId(R.id.button)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // 2) Now in SecondFragment, tap the FAB
        onView(withId(R.id.add_circle_button)).perform(click());

        // 3) Enter a >20 char & >3 words description to trigger the validation error
        onView(withId(R.id.edit_description))
                .perform(typeText("a really long description"), closeSoftKeyboard());

        // 4) Press next; the app should NOT proceed due to invalid input
        onView(withId(R.id.next)).perform(click());

        // 5) Verify that we did NOT navigate to the second page
        //    by checking that the second screen’s 'triggerInput' does not exist
        onView(withId(R.id.triggerInput))
                .check(doesNotExist());
    }

    /**
     * After each test, clear out the Firestore emulator’s data so that
     * each test starts fresh with no leftover documents from previous tests.
     */
    @After
    public void clearEmulatorDb() {
        try {
            // Update with your actual project ID
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
