package com.example.moodsync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moodsync.RegisterFragment.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    private TextView goToRegister;

    private TextInputEditText usernameInput, passwordInput;
    private MaterialButton loginButton;
    private FirebaseFirestore db;
    LocalStorage globalStorage = LocalStorage.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        LocalStorage globalStorage = LocalStorage.getInstance();

        db = FirebaseFirestore.getInstance();

        usernameInput = view.findViewById(R.id.usernameLogin);
        passwordInput = view.findViewById(R.id.passwordLogin);
        loginButton = view.findViewById(R.id.loginButton);
        goToRegister = view.findViewById(R.id.go_to_signup);


        goToRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_LoginFragment_to_RegisterFragment);
        });

        loginButton.setOnClickListener(v -> login());
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User tempUser = new User();
                                tempUser.setId(document.getId());
                                tempUser.setName((String) document.get("fullName"));
                                tempUser.setPass((String) document.get("password"));
                                tempUser.setUsername((String) document.get("userName"));
                                tempUser.setPfpUrl((String) document.get("profileImageUrl"));
                                tempUser.setLocation((String) document.get("location"));
                                tempUser.setBio((String) document.get("bio"));
                                tempUser.setFollowerList((ArrayList<String>) document.get("followerList"));
                                tempUser.setFollowingList((ArrayList<String>) document.get("followingList"));
                                tempUser.setCommentList((ArrayList<Integer>) document.get("commentList"));


                                if (!globalStorage.checkIfUserExists(tempUser)){
                                    globalStorage.getUserList().add(tempUser);
                                    Log.d("ADAPTER",  " => " + tempUser.getUsername());
                                }
                                Log.d("User Data", document.getId() + " => " + tempUser);
                            }
                        } else {
                            Log.d("User Data", "Error fetching users: " + task.getException().getMessage());
                        }
                    }
                });

        return view;
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        boolean userFound = false;

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Map<String, Object> userData = document.getData();
                            String storedUsername = (String) userData.get("userName");
                            String storedPassword = (String) userData.get("password");

                            if (storedUsername.equals(username) && storedPassword.equals(password)) {
                                userFound = true;
                                globalStorage.setCurrentUserId(username);
                                break;
                            }
                        }

                        if (userFound) {
                            ((MyApplication) getActivity().getApplication()).setLoggedInUsername(username);
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            // Navigate to SecondFragment
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_LoginFragment_to_SecondFragment);
                        } else {
                            Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
