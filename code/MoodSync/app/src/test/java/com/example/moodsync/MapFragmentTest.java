package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class MapFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private QuerySnapshot mockQuerySnapshot;

    private MapFragment mapFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mapFragment = new MapFragment();
        mapFragment.db = mockDb;
    }

    @Test
    public void testFetchMoodEvents() {
        List<MoodEvent> mockMoodEvents = new ArrayList<>();
        MoodEvent newME = new MoodEvent("Happy", "Test", "Description", "Alone", System.currentTimeMillis(), "imageUrl", true, "saumya", null, null, "37.4219983,-122.084");
        mockMoodEvents.add(newME);
        mapFragment.moodHistoryItems.add(newME);

        //mapFragment.fetchMoodEvents();

        assertEquals(1, mapFragment.moodHistoryItems.size());
    }

    @Test
    public void testCalculateManhattanDistance() {
        double lat1 = 37.4219983;
        double lng1 = -122.;
    }
}