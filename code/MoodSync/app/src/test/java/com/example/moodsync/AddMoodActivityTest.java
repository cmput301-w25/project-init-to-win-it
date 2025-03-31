package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class AddMoodActivityTest {

    @Mock
    FirebaseFirestore mockFirestore;
    @Mock
    CollectionReference mockCollection;
    @Mock
    StorageReference mockStorageRef;
    @Mock
    UploadTask mockUploadTask;
    @Mock
    Context mockContext;

    private AddMoodActivity fragment = new AddMoodActivity();

    @Before
    public void setup() {
        fragment.db = mockFirestore;
        fragment.moodEventsRef = mockCollection;
    }

    @Test
    public void testInitMoodGradients() {
        fragment.initMoodGradients();
        assertTrue(fragment.moodGradients.containsKey("Happy"));
        assertEquals(8, fragment.moodGradients.size());
    }

    @Test
    public void testSaveMoodEventToFirestore() {
        MoodEvent testEvent = new MoodEvent(
                "Happy", "Test", "Desc", "Alone",
                System.currentTimeMillis(), "url",
                false, "user", "song", "title", "loc"
        );
    }
}