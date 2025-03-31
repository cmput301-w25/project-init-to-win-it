package com.example.moodsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private Task<DocumentSnapshot> mockDocTask;

    @Mock
    private DocumentSnapshot mockDocSnapshot;

    @Mock
    private Query mockQuery;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    private UserProfileFragment fragment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        fragment = new UserProfileFragment();
        fragment.db = mockDb;

        when(mockDb.collection(anyString())).thenReturn(mockCollectionRef);
        when(mockCollectionRef.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);
    }

    @Test
    public void testHandleFollowRequest() {
        fragment.currentUserId = "currentUser";
        fragment.selectedUserId = "selectedUser";

        when(mockCollectionRef.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);

        fragment.handleFollowRequest();

        verify(mockDb).collection("pendingFollowerRequests");
        verify(mockCollectionRef).whereEqualTo("follower", "currentUser");
        verify(mockQuery).whereEqualTo("followee", "selectedUser");
    }

    @Test
    public void testUpdateFollowButtonState() {
        fragment.currentUserId = "currentUser";
        fragment.selectedUserId = "selectedUser";

        when(mockCollectionRef.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);

        fragment.updateFollowButtonState();

        verify(mockDb).collection("pendingFollowerRequests");
        verify(mockCollectionRef).whereEqualTo("follower", "currentUser");
        verify(mockQuery).whereEqualTo("followee", "selectedUser");
    }

    @Test
    public void testUpdateFollowButtonStateBasedOnFollowers() {
        fragment.currentUserId = "currentUser";
        fragment.selectedUserId = "selectedUser";

        when(mockCollectionRef.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);

        fragment.updateFollowButtonStateBasedOnFollowers();

        verify(mockDb).collection("users");
        verify(mockCollectionRef).document("selectedUser");
    }
}
