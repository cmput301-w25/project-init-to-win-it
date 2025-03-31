package com.example.moodsync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/* This performs a second set of tests in AddMoodActivity.
It tests the following User Stories:
1. US 02.01.01
2. US 02.02.01
3. US 02.03.01
4. US 02.04.01
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SecondTestSet {
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

//        // Ensure the view is displayed before interacting with it
//        onView(withId(R.id.button)).check(matches(isDisplayed()));
//        onView(withId(R.id.button)).perform(click());
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        onView(withId(R.id.add_circle_button)).check(matches(isDisplayed()));
        onView(withId(R.id.add_circle_button)).perform(click());
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

        onView(withId(R.id.trigger_text_view)).check(matches(isDisplayed()));
        onView(withId(R.id.trigger_text_view)).perform(click());
        try {
            Thread.sleep(3000); // Avoid using Thread.sleep() if possible
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
            Log.e("InterruptedException", "Thread was interrupted");
        }

        onView(withId(R.id.trigger_text_view)).perform(typeText("123456789012345678901"));

        onView(withId(R.id.trigger_text_view)).check(matches(withText("12345678901234567890")));
    }

    @Test
    public void US020101Word() {
//        onView(withId(R.id.button)).check(matches(isDisplayed()));
//        onView(withId(R.id.button)).perform(click());

        onView(withId(R.id.add_circle_button)).check(matches(isDisplayed()));
        onView(withId(R.id.add_circle_button)).perform(click());

        onView(withId(R.id.next)).check(matches(isDisplayed()));
        onView(withId(R.id.next)).perform(click());

        onView(withId(R.id.trigger_text_view)).check(matches(isDisplayed()));
        onView(withId(R.id.trigger_text_view)).perform(click());
        onView(withId(R.id.trigger_text_view)).perform(replaceText("word1 second three NOTFOUR"));

        onView(withId(R.id.trigger_text_view)).check(matches(withText("word1 second three")));
    }

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
