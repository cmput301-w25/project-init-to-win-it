package com.example.moodsync;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodImageAdapterTest {

    private MoodImageAdapter adapter;
    private List<Map<String, Object>> moodList;

    @Mock
    private android.content.Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test mood data
        moodList = new ArrayList<>();

        Map<String, Object> mood1 = new HashMap<>();
        mood1.put("mood", "Happy");
        mood1.put("imageUrl", "https://www.ualberta.ca/media-library/ualberta/homepage/university-of-alberta-logo.jpg");

        Map<String, Object> mood2 = new HashMap<>();
        mood2.put("mood", "Sad");
        mood2.put("imageUrl", "");

        moodList.add(mood1);
        moodList.add(mood2);

        adapter = new MoodImageAdapter(mockContext, moodList);
    }

    @Test
    public void testGetCount() {
        assertEquals(2, adapter.getCount());

        moodList.clear();
        assertEquals(0, adapter.getCount());
    }

    @Test
    public void testGetItem() {
        Map<String, Object> item = (Map<String, Object>) adapter.getItem(0);
        assertEquals("Happy", item.get("mood"));
        assertEquals("https://www.ualberta.ca/media-library/ualberta/homepage/university-of-alberta-logo.jpg", item.get("imageUrl"));
    }

    @Test
    public void testGetItemId() {
        assertEquals(0, adapter.getItemId(0));
        assertEquals(1, adapter.getItemId(1));
    }

    @Test
    public void testSetters() {
        List<Map<String, Object>> newList = new ArrayList<>();
        adapter.setMoodList(newList);
        assertEquals(0, adapter.getCount());

        adapter.setContext(null);
    }
}
