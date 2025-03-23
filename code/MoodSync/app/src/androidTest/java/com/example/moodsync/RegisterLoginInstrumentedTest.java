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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegisterLoginInstrumentedTest {

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
        // Insert one known user in Firestore so the "Login" can be tested
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", "John Doe");
        userData.put("userName", "johndoe");
        userData.put("password", "password123");
        userData.put("followerList", new ArrayList<String>());
        userData.put("followingList", new ArrayList<String>());
        userData.put("commentList", new ArrayList<Integer>());

        usersRef.document("johndoe")
                .set(userData);
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

    /**
     * Test that unsuccessful login shows an error message.
     */
    @Test
    public void testUnsuccessfulLoginShowsErrorMessage() {
        SystemClock.sleep(3000);
        // 1) From the first fragment, press "Login"
        onView(withId(R.id.loginButton)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // 2) Enter invalid login credentials
        onView(withId(R.id.usernameLogin)).perform(typeText("wronguser"));
        onView(withId(R.id.passwordLogin)).perform(typeText("wrongpassword"));
        closeSoftKeyboard();

        // 3) Press the login button
        onView(withId(R.id.loginButton)).perform(click());

        // 4) An error message should be displayed
        onView(withText("Invalid username or password")).check(matches(isDisplayed()));
    }


    /**
     * Test that unsuccessful registration due to duplicate username shows an error message.
     */
    @Test
    public void testUnsuccessfulRegistrationDueToDuplicateUsername() {
        SystemClock.sleep(3000);
        // 1) From the first fragment, press "Register"
        onView(withId(R.id.button)).perform(click());
        // Wait a little to account for the navigation delay
        SystemClock.sleep(3000);

        // 2) Enter registration data with a duplicate username
        onView(withId(R.id.fullnameInput)).perform(typeText("Jane Doe"));
        onView(withId(R.id.usernameInput)).perform(typeText("johndoe"));
        onView(withId(R.id.passwordInput)).perform(typeText("password123"));
        onView(withId(R.id.passwordInput2)).perform(typeText("password123"));
        closeSoftKeyboard();

        // 3) Press the register button
        onView(withId(R.id.signupButton)).perform(click());

        // 4) An error message should be displayed
        onView(withText("This username is taken. Try another one")).check(matches(isDisplayed()));
    }
}
