package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EditMoodActivityTest {

    @Mock
    private FirebaseFirestore mockDb;

    private EditMoodActivity editMoodActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        editMoodActivity = new EditMoodActivity();
        editMoodActivity.db = mockDb;
    }

    @Test
    public void testUpdateMoodEvent() {
        MoodEvent moodEvent = new MoodEvent("Happy", "Test", "Description", "Alone", System.currentTimeMillis(), "imageUrl", true, "testUser", null, null, "location");
        when(mockDb.collection("mood_events")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("mood_events").whereEqualTo("description", moodEvent.getDescription())).thenReturn(mock(Query.class));
        when(mockDb.collection("mood_events").whereEqualTo("description", moodEvent.getDescription()).get()).thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        editMoodActivity.moodEventToEdit = moodEvent;
        editMoodActivity.moodEventsRef = editMoodActivity.db.collection("mood_events");

    }

}
