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
 * Tests the "Search Profile" feature: verifying that we can
 * search for a user and view their profile.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchProfileInstrumentedTest {

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
        // Insert users in Firestore so the "Search Profile" can display them
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        Map<String, Object> userData1 = new HashMap<>();
        userData1.put("fullName", "Test User 1");
        userData1.put("userName", "testuser1");
        userData1.put("password", "password123");
        userData1.put("profileImageUrl", "");
        userData1.put("location", "");
        userData1.put("bio", "");
        userData1.put("followerList", new ArrayList<>());
        userData1.put("followingList", new ArrayList<>());
        userData1.put("commentList", new ArrayList<>());
        usersRef.add(userData1);

        Map<String, Object> userData2 = new HashMap<>();
        userData2.put("fullName", "Test User 2");
        userData2.put("userName", "testuser2");
        userData2.put("password", "password123");
        userData2.put("profileImageUrl", "");
        userData2.put("location", "");
        userData2.put("bio", "");
        userData2.put("followerList", new ArrayList<>());
        userData2.put("followingList", new ArrayList<>());
        userData2.put("commentList", new ArrayList<>());
        usersRef.add(userData2);

        Map<String, Object> userData3 = new HashMap<>();
        userData3.put("fullName", "Test User 3");
        userData3.put("userName", "testuser3");
        userData3.put("password", "password123");
        userData3.put("profileImageUrl", "");
        userData3.put("location", "");
        userData3.put("bio", "");
        userData3.put("followerList", new ArrayList<>());
        userData3.put("followingList", new ArrayList<>());
        userData3.put("commentList", new ArrayList<>());
        usersRef.add(userData3);

        // Update user1 to follow user2
        db.collection("users").whereEqualTo("userName", "testuser1").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> updatedData = document.getData();
                            ((ArrayList<String>) updatedData.get("followingList")).add("testuser2");
                            document.getReference().set(updatedData);
                        }
                    }
                });
    }

    /**
     * Test that searching for a user and clicking on them displays their profile.
     */
    @Test
    public void testSearchAndViewProfile() {
        SystemClock.sleep(4000);

        Log.d("bitch1", "testSearchAndViewProfile: ");
        // 1) Click on the login button from the first page
        onView(withId(R.id.loginButton)).perform(click());
        Log.d("bitch2", "testSearchAndViewProfile: ");

        // Wait for the login page to load
        SystemClock.sleep(8000);

        // 2) Enter username and password
        onView(withId(R.id.usernameLogin)).perform(typeText("testuser1"));

        SystemClock.sleep(8000);
        onView(withId(R.id.passwordLogin)).perform(typeText("password123"));

        SystemClock.sleep(8000);

        // 3) Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the login to complete and navigate to the next page
        SystemClock.sleep(8000);

        // 4) Click on the search bar
        onView(withId(R.id.search_bar)).perform(click());
        SystemClock.sleep(8000);

        // 5) Type in the username of user2
        onView(withId(R.id.search_bar)).perform(typeText("testuser2"));
        SystemClock.sleep(8000);

        // 6) Click on the search result
//        onView(withId("testuser2")).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("testuser2")))
                .inAdapterView(withId(R.id.search_results_listview))
                .perform(click());

        SystemClock.sleep(10000);

        // 7) Check if the profile fragment is displayed
        onView(withId(R.id.usernameofuser)).check(matches(withText("@testuser2")));
    }

    /**
     * Test that searching for a user not in the following list displays a private account message.
     */
    @Test
    public void testSearchPrivateAccount() {
        SystemClock.sleep(4000);

        Log.d("bitch1", "testSearchPrivateAccount: ");
        // 1) Click on the login button from the first page
        onView(withId(R.id.loginButton)).perform(click());
        Log.d("bitch2", "testSearchPrivateAccount: ");

        // Wait for the login page to load
        SystemClock.sleep(4000);

        // 2) Enter username and password
        onView(withId(R.id.usernameLogin)).perform(typeText("testuser1"));
        SystemClock.sleep(4000);
        onView(withId(R.id.passwordLogin)).perform(typeText("password123"));

        SystemClock.sleep(4000);

        // 3) Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the login to complete and navigate to the next page
        SystemClock.sleep(4000);

        // 4) Click on the search bar
        onView(withId(R.id.search_bar)).perform(click());
        SystemClock.sleep(6000);

        // 5) Type in the username of user3
        onView(withId(R.id.search_bar)).perform(typeText("testuser3"));
        SystemClock.sleep(6000);

        // 6) Click on the search result
        onData(allOf(is(instanceOf(String.class)), is("testuser3")))
                .inAdapterView(withId(R.id.search_results_listview))
                .perform(click());
        SystemClock.sleep(10000);

        // 7) Check if the private account message is displayed
        onView(withId(R.id.private_account_text)).check(matches(isDisplayed()));
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
