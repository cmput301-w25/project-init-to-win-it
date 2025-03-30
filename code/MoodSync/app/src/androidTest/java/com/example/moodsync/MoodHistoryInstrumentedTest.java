package com.example.moodsync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        MoodEvent seededEvent = new MoodEvent(
                "Happy", // mood
                "Got a new job", // trigger
                "Feeling excited and grateful", // description
                "With friends", // socialSituation
                System.currentTimeMillis(), // date
                "https://image-url", // imageUrl
                false, // isPublic
                "testuser", // id
                "https://storage.googleapis.com/inittowinit-1188f.firebasestorage.app/songs1/Adam%20Dib%20-%20After%20the%20Bunker%20-%20No%20Backing%20Vocals%20.mp3?Expires=1774785984&GoogleAccessId=firebase-adminsdk-fbsvc%40inittowinit-1188f.iam.gserviceaccount.com&Signature=SbPcDm346A72dfI2Y7IfCSn8kb84RbAMNsOdWexrf7P8dKqdLdPh%2BWeEKJ8fPCWQELYpqvHhRAojX2ttFzeY9fWnFSHLel2RfnO34JdutXS526MRS%2B6x1zu0IfRGQGpt5sD%2F57l25dWRFOvvhJL2eAmeFSWhtYSIMv%2Bd%2FyJ85F0afs9VfgBQWsGIBCcFPdqY2PpooY1E4hmZEXJbYFvdugypQ0fUOlriILQe%2FpeKgt8m1yocZljYJLrTIvflJjsQ2KAX1bRa02P7qMKkgHcXYgGOt6uxjE5s4BexgyFcz0kTnFEkJ4o%2BW2r04xIeMhaJPK5MNgmccsnutcuY%2ByTsFg%3D%3D", // songUrl
                "Adam Dib - After the Bunker - No Backing Vocals", // songTitle
                "53.526264,-113.5170344" // currentLocation
        );

        CollectionReference usersRef = db.collection("users");
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", "Test User");
        userData.put("userName", "testuser");
        userData.put("password", "password123");
        userData.put("profileImageUrl", "");
        userData.put("location", "");
        userData.put("bio", "");
        userData.put("followerList", new ArrayList<>());
        userData.put("followingList", new ArrayList<>());
        userData.put("commentList", new ArrayList<>());
        usersRef.add(userData);
        Log.d("bitch", "seedDatabase: ");
    }


    @Before
    public void navigateToHistory() {
        SystemClock.sleep(4000);

        Log.d("bitch1", "navigateToHistory: ");
        // 1) Click on the login button from the first page
        onView(withId(R.id.loginButton)).perform(click());
        Log.d("bitch2", "navigateToHistory: ");

        // Wait for the login page to load
        SystemClock.sleep(4000);

        // 2) Enter username and password
        onView(withId(R.id.usernameLogin)).perform(typeText("testuser"));
        onView(withId(R.id.passwordLogin)).perform(typeText("password123"));

        // 3) Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the login to complete and navigate to the next page
        SystemClock.sleep(5000);

        // 4) Click on the history button
        onView(withId(R.id.history_button)).perform(click());
        SystemClock.sleep(1000);
    }
    @Test
    public void testEditMoodInHistory() {
        // 1) Click on a mood item in the history
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(1000);

        // 2) Check if the edit fragment is displayed
        onView(withId(R.id.edit_description)).check(matches(isDisplayed()));

        // 3) Click the next button
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(1000);

        // 4) Check if the second edit fragment is displayed
        onView(withId(R.id.reasonLabel)).check(matches(isDisplayed()));

        // 5) Click the create button
        onView(withId(R.id.createmood)).perform(click());
        SystemClock.sleep(1000);
    }

    @Test
    public void testDeleteMoodInHistory() {
        SystemClock.sleep(3000);
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
        SystemClock.sleep(3000);
        // 5) A delete confirmation dialog appears. Press "Delete"
        onView(withText("Delete")).perform(click());
        SystemClock.sleep(3000);
        // 6) Now confirm that "Happy" no longer appears in the list
        onView(withText("Happy")).check(doesNotExist());
        SystemClock.sleep(3000);
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
