package com.example.moodsync;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private TextInputEditText fullnameInput, usernameInput, passwordInput, passwordInput2;
    private CheckBox promiseCheckbox;
    private MaterialButton signupButton;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.register, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link UI components by their ID
        // In onCreateView method
        fullnameInput= view.findViewById(R.id.fullnameInput);
        usernameInput = view.findViewById(R.id.usernameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        passwordInput2 = view.findViewById(R.id.passwordInput2);
        signupButton = view.findViewById(R.id.signupButton);


        // Set up click listener for the signup button
        signupButton.setOnClickListener(v -> {
            String fullname = fullnameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String password2 = passwordInput2.getText().toString().trim();

            if (fullname.isEmpty()) {
                fullnameInput.setError("Fullname is required");
                return;
            }

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            if (!password.equals(password2)) {
                passwordInput.setError("Passwords do not match. Please try again");
                return;
            }

            // Create user data map
            Map<String, Object> userData = new HashMap<>();
            userData.put("fullName", fullname);
            userData.put("userName", username);
            userData.put("password", password);
            userData.put("followerList", new ArrayList<String>());
            userData.put("followingList", new ArrayList<String>());
            userData.put("commentList", new ArrayList<Integer>());

            // Save data to Firestore with username as document ID
            db.collection("users").document(username)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to SecondFragment after successful registration
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_RegisterFragment_to_SecondFragment);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error registering user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        return view;
    }}
