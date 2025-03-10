package com.example.moodsync;

import static android.app.Activity.RESULT_OK;
import static android.text.method.Touch.scrollTo;

import android.content.Intent;
<<<<<<< Updated upstream
=======
import android.os.SystemClock;
>>>>>>> Stashed changes
import android.util.Log;
import android.widget.TextView;

import static androidx.test.espresso.Espresso.onView;

//import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
//import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.action.ViewActions;

//import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/* This performs a second set of tests in AddMoodActivity.
It tests the following User Stories:
1. US 02.01.01
2. US 02.02.01
3. US 02.03.01
4. US 02.04.01
5. US 04.01.01
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SecondTestSet {
    private FirebaseFirestore db;
    private CollectionReference moodsRef;
    private CountDownLatch latch;

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }



    @Test
    public void US020101Char() {
        try {
            Thread.sleep(5000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        // Ensure the view is displayed before interacting with it
        onView(withId(R.id.button)).check(matches(isDisplayed()));
        onView(withId(R.id.button)).perform(click());
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        onView(withId(R.id.add_circle_button)).check(matches(isDisplayed()));
        onView(withId(R.id.add_circle_button)).perform(click());
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(3000);
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.main_card)).perform(click());
        try {
            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withText("Happy")).perform(click());
        try {
            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.next)).check(matches(isDisplayed()));
        onView(withId(R.id.next)).perform(click());
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        onView(withId(R.id.triggerInput)).check(matches(isDisplayed()));
        onView(withId(R.id.triggerInput)).perform(click());
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        onView(withId(R.id.triggerInput)).perform(typeText("123456789012345678901"));

        onView(withId(R.id.triggerInput)).check(matches(withText("12345678901234567890")));
    }

    @Test
    public void US020101Word() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.add_circle_button)).perform(click());
<<<<<<< Updated upstream
=======
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(3000);
>>>>>>> Stashed changes
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.next)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.triggerInput)).perform(click());
        onView(withId(R.id.triggerInput)).perform(replaceText("word1 second three NOTFOUR")); //No error message supposed to be returned, would simply not allow typing beyond 3 words
        onView(withId(R.id.triggerInput)).check(matches(withText("word1 second three")));
    }

    @Test
    public void US020201() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.add_circle_button)).perform(click());
<<<<<<< Updated upstream
=======
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(3000);
>>>>>>> Stashed changes
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.next)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.photos)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withText("Add from Photos")).check(matches(isDisplayed()));
    }

    @Test
    public void US020401() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.add_circle_button)).perform(click());
<<<<<<< Updated upstream
=======
        onView(withId(R.id.main_card)).perform(click());
        onView(withText("Happy")).perform(click());
        SystemClock.sleep(3000);
>>>>>>> Stashed changes
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.main_card)).perform(click());
        try {
            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withText("Happy")).perform(click());
        try {
            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }
        onView(withId(R.id.next)).perform(click());
        //Checks all buttons being displayed properly post being clicked
        onView(withId(R.id.ss1)).perform(click());
        onView(withId(R.id.ss1)).check(matches(isDisplayed()));

        onView(withId(R.id.ss2)).perform(click());
        onView(withId(R.id.ss2)).check(matches(isDisplayed()));

        onView(withId(R.id.ss3)).perform(click());
        onView(withId(R.id.ss3)).check(matches(isDisplayed()));

        onView(withId(R.id.ss4)).perform(click());
        onView(withId(R.id.ss4)).check(matches(isDisplayed()));
    }
<<<<<<< Updated upstream
//    @Test
//    public void US040401(){
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.button)).perform(click());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.add_circle_button)).perform(click());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.main_card)).perform(click());
//        try {
//            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Restore the interrupt flag
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withText("Happy")).perform(click());
//        try {
//            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Restore the interrupt flag
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.next)).perform(click());
//        onView(withId(R.id.triggerInput)).perform(click());
//        onView(withId(R.id.triggerInput)).perform(typeText("word1 second three NOTFOUR"));
//        onView(withId(R.id.ss1)).perform(click());
//        onView(withId(R.id.createmood)).perform(click());
//        try {
//            Thread.sleep(5000); // Avoid using Thread.sleep() if possible
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Restore the interrupt flag
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//
//
//
//
//        onView(withId(R.id.button)).perform(click());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.add_circle_button)).perform(click());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.main_card)).perform(click());
//        try {
//            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Restore the interrupt flag
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withText("Sad")).perform(click());
//        try {
//            Thread.sleep(1000); // Avoid using Thread.sleep() if possible
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Restore the interrupt flag
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//        onView(withId(R.id.next)).perform(click());
//        onView(withId(R.id.triggerInput)).perform(click());
//        onView(withId(R.id.triggerInput)).perform(typeText("Mood"));
//        onView(withId(R.id.ss1)).perform(click());
//        onView(withId(R.id.createmood)).perform(click());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            Log.e("InterruptedException", "Thread was interrupted");
//        }
//
//
//
//        onView(withId(R.id.moodRecyclerView))
//                .perform(RecyclerViewActions.scrollToPosition(0))
//                .check(matches(hasDescendant(withText("Sad"))));
//
//        onView(withId(R.id.moodRecyclerView))
//                .perform(RecyclerViewActions.scrollToPosition(1))
//                .check(matches(hasDescendant(withText("Happy"))));
//    }



=======
>>>>>>> Stashed changes

    @After
    public void tearDown() {
        String projectId = "inittowinit-1188f";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}