package com.example.moodsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PendingUploadTest {

    private Uri mockUri;
    private String testUploadPath;
    private PendingUpload pendingUpload;

    @Before
    public void setUp() {
        mockUri = mock(Uri.class);
        testUploadPath = "images/test_image.jpg";
        pendingUpload = new PendingUpload(mockUri, testUploadPath);
    }

    @Test
    public void testConstructor() {
        assertNotNull(pendingUpload);
        assertEquals(mockUri, pendingUpload.getImageUri());
        assertEquals(testUploadPath, pendingUpload.getUploadPath());
    }

    @Test
    public void testGetImageUri() {
        assertEquals(mockUri, pendingUpload.getImageUri());
    }

    @Test
    public void testGetUploadPath() {
        assertEquals(testUploadPath, pendingUpload.getUploadPath());
    }
}