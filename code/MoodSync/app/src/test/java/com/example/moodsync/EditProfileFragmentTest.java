package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EditProfileFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    private EditProfileFragment editProfileFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        editProfileFragment = new EditProfileFragment();
        editProfileFragment.db = mockDb;
    }

    @Test
    public void testLoadUserData() {
        String loggedInUsername = "saumya";
        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").whereEqualTo("userName", loggedInUsername)).thenReturn(mock(Query.class));
        when(mockDb.collection("users").whereEqualTo("userName", loggedInUsername).get()).thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        when(mockDocumentSnapshot.getString("fullName")).thenReturn("Saumya N Patel");
        when(mockDocumentSnapshot.getString("userName")).thenReturn(loggedInUsername);
    }

}