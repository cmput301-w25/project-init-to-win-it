package com.example.moodsync;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
 * Tests the "Journal" feature: verifying that we can
 * navigate to the journal tab, the tab displays the users private moods and the "details" button displays the details of the mood.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class JournalInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Use the local Firestore emulator instead of the real Firestore.
     */
    @BeforeClass
    public static void useFirestoreEmulator() {
        FirebaseFirestore.getInstance().useEmulator("127.0.0.1", 4400);
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
                "Happy", // mood
                "Got a new job", // trigger
                "Feeling excited and grateful", // description
                "With friends", // socialSituation
                System.currentTimeMillis(), // date
                "https://image-url", // imageUrl
                false, // isPublic,
                "testuser", // id
                "https://storage.googleapis.com/inittowinit-1188f.firebasestorage.app/songs1/Adam%20Dib%20-%20After%20the%20Bunker%20-%20No%20Backing%20Vocals%20.mp3?Expires=1774785984&GoogleAccessId=firebase-adminsdk-fbsvc%40inittowinit-1188f.iam.gserviceaccount.com&Signature=SbPcDm346A72dfI2Y7IfCSn8kb84RbAMNsOdWexrf7P8dKqdLdPh%2BWeEKJ8fPCWQELYpqvHhRAojX2ttFzeY9fWnFSHLel2RfnO34JdutXS526MRS%2B6x1zu0IfRGQGpt5sD%2F57l25dWRFOvvhJL2eAmeFSWhtYSIMv%2Bd%2FyJ85F0afs9VfgBQWsGIBCcFPdqY2PpooY1E4hmZEXJbYFvdugypQ0fUOlriILQe%2FpeKgt8m1yocZljYJLrTIvflJjsQ2KAX1bRa02P7qMKkgHcXYgGOt6uxjE5s4BexgyFcz0kTnFEkJ4o%2BW2r04xIeMhaJPK5MNgmccsnutcuY%2ByTsFg%3D%3D", // songUrl
                "Adam Dib - After the Bunker - No Backing Vocals", // songTitle
                "53.526264,-113.5170344" // currentLocation
        );
        moodsRef.add(seededEvent);

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
    }


    /**
     * Test that tapping "Details" on a mood card shows a bottom-sheet or dialog
     * with the correct mood info.
     */
    @Test
    public void testDetailsButtonShowsMoodDetails() {
        // Wait for the login page to load
        SystemClock.sleep(4000);

        // 2) Enter username and password
        onView(withId(R.id.usernameLogin)).perform(typeText("testuser"));
        onView(withId(R.id.passwordLogin)).perform(typeText("password123"));

        // 3) Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the login to complete and navigate to the next page
        SystemClock.sleep(5000);

        onView(withId(R.id.diary_button)).perform(click());
        SystemClock.sleep(7000);
        // 3) Tap "Details" button
        onView(withId(R.id.details_button)).perform(click());

        SystemClock.sleep(4000);
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
        onView(withText("Location: 53.526264,-113.5170344"))
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
