package com.example.moodsync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.TextView;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

public class LoginFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private QuerySnapshot mockQuerySnapshot;

    private LoginFragment loginFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginFragment = new LoginFragment();
        loginFragment.db = mockDb;
    }

    @Test
    public void testLogin_Failure() {
        String username = "testUser";
        String password = "wrongPass";
        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").get()).thenReturn(Tasks.forResult(mockQuerySnapshot));
        when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        try{
            loginFragment.login();
        }
        catch (NullPointerException e){
            //Should occur since should be null
        }
    }

    private DocumentSnapshot createMockDocument(String username, String password) {
        DocumentSnapshot mockDoc = mock(DocumentSnapshot.class);
        when(mockDoc.getString("userName")).thenReturn(username);
        when(mockDoc.getString("password")).thenReturn(password);
        return mockDoc;
    }
}