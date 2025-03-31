package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class JournalFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private QuerySnapshot mockQuerySnapshot;

    private JournalFragment journalFragment;


    @Before
    public void setUp() {
        try{
            MockitoAnnotations.openMocks(this);
            Fragment journalFragment = new JournalFragment();
            ((JournalFragment) journalFragment).db = mockDb;
        } catch (Exception e) {

        }
    }


    @Test
    public void testFetchPrivateMoods() {
        String currentUserId = "testUser";
        List<MoodEvent> mockMoodEvents = new ArrayList<>();
        mockMoodEvents.add(new MoodEvent("Happy", "Test", "Description", "Alone", System.currentTimeMillis(), "imageUrl", false, currentUserId, null, null, "location"));

        try{
            when(mockDb.collection("mood_events")).thenReturn(mock(CollectionReference.class));
            when(mockDb.collection("mood_events").whereEqualTo("id", currentUserId)).thenReturn(mock(Query.class));
            when(mockDb.collection("mood_events").whereEqualTo("id", currentUserId).whereEqualTo("public", false)).thenReturn(mock(Query.class));
            when(mockDb.collection("mood_events").whereEqualTo("id", currentUserId).whereEqualTo("public", false).get()).thenReturn(Tasks.forResult(mockQuerySnapshot));
            when(mockQuerySnapshot.toObjects(MoodEvent.class)).thenReturn(mockMoodEvents);
        } catch (Exception e) {

        }

    }

}